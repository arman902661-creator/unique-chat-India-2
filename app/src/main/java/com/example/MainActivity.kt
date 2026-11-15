package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.data.VoiceChatRepository
import com.example.data.VoiceRtcService
import com.example.model.ChatRoom
import com.example.model.User
import com.example.ui.screens.admin.OwnerAdminScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.chat.PrivateChatDetailScreen
import com.example.ui.screens.chat.PrivateChatListScreen
import com.example.ui.screens.games.GamesHubScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.room.VoiceRoomScreen
import com.example.ui.theme.IndiaSaffron
import com.example.ui.theme.MidnightDeep
import com.example.ui.theme.PeacockCyan
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.UniqueChatIndiaTheme
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String, val icon: ImageVector) {
    ROOMS("Rooms", Icons.Default.Home),
    CHATS("Chats", Icons.Default.Chat),
    GAMES("Games", Icons.Default.Casino),
    PROFILE("Profile", Icons.Default.Person),
    ADMIN("Owner", Icons.Default.Security)
}

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var repository: VoiceChatRepository
    private lateinit var voiceRtcService: VoiceRtcService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager(this)
        repository = VoiceChatRepository(this)
        voiceRtcService = VoiceRtcService(this)

        setContent {
            UniqueChatIndiaTheme {
                AppContent(
                    authManager = authManager,
                    repository = repository,
                    voiceRtcService = voiceRtcService
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Mark online in Firestore if logged in
        if (authManager.getUserId() != null) {
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                repository.setUserOnline(true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Mark offline on app leave
        if (authManager.getUserId() != null) {
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                repository.setUserOnline(false)
            }
        }
    }
}

@Composable
fun AppContent(
    authManager: AuthManager,
    repository: VoiceChatRepository,
    voiceRtcService: VoiceRtcService
) {
    val firebaseUser by authManager.currentUserState.collectAsState()
    val scope = rememberCoroutineScope()

    if (firebaseUser == null) {
        AuthScreen(
            authManager = authManager,
            onAuthSuccess = {
                // Auth state listener handles transition
            }
        )
    } else {
        val uid = firebaseUser!!.uid
        key(uid) {
            AuthenticatedApp(
                uid = uid,
                firebaseUser = firebaseUser!!,
                authManager = authManager,
                repository = repository,
                voiceRtcService = voiceRtcService
            )
        }
    }
}

@Composable
fun AuthenticatedApp(
    uid: String,
    firebaseUser: com.google.firebase.auth.FirebaseUser,
    authManager: AuthManager,
    repository: VoiceChatRepository,
    voiceRtcService: VoiceRtcService
) {
    val scope = rememberCoroutineScope()
    var currentUserProfile by remember { mutableStateOf<User?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    // Navigation and subscreen states
    var selectedTab by remember { mutableStateOf(MainNavTab.ROOMS) }
    var activeVoiceRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var activePrivateChat by remember { mutableStateOf<Pair<String, String>?>(null) } // (otherUserId, otherUserName)
    var isShowingAdmin by remember { mutableStateOf(false) }

    // Password Prompt for Protected Rooms
    var pendingPasswordRoom by remember { mutableStateOf<ChatRoom?>(null) }
    var enteredPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Data streams
    val rooms by repository.observeRooms().collectAsState(initial = emptyList())
    val allUsers by repository.observeAllUsers().collectAsState(initial = emptyList())

    // Load or create Firestore user profile
    LaunchedEffect(uid) {
        try {
            val profile = repository.getOrCreateUserProfile(firebaseUser)
            currentUserProfile = profile
            isLoadingProfile = false
        } catch (e: Exception) {
            isLoadingProfile = false
        }
    }

    if (isLoadingProfile || currentUserProfile == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightDeep),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PeacockCyan)
        }
        return
    }

    val user = currentUserProfile!!

    // If user is banned by owner
    if (user.isBanned) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightDeep)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = SurfaceElevated)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️ Account Suspended", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your account has been suspended by the Unique Chat India administration for policy violation.\nFor inquiries contact customer support at 9219240590.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authManager.signOut(scope) }
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
        return
    }

    // Full screen sub-destinations
    when {
        activeVoiceRoom != null -> {
            VoiceRoomScreen(
                room = activeVoiceRoom!!,
                currentUser = user,
                allUsers = allUsers,
                repository = repository,
                voiceRtcService = voiceRtcService,
                onLeaveRoom = { activeVoiceRoom = null }
            )
        }
        activePrivateChat != null -> {
            PrivateChatDetailScreen(
                currentUserId = user.userId,
                otherUserId = activePrivateChat!!.first,
                otherUserName = activePrivateChat!!.second,
                repository = repository,
                onBack = { activePrivateChat = null }
            )
        }
        isShowingAdmin -> {
            OwnerAdminScreen(
                currentUser = user,
                rooms = rooms,
                allUsers = allUsers,
                repository = repository,
                onBack = { isShowingAdmin = false }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceElevated,
                        contentColor = Color.White
                    ) {
                        val navTabs = mutableListOf(
                            MainNavTab.ROOMS,
                            MainNavTab.CHATS,
                            MainNavTab.GAMES,
                            MainNavTab.PROFILE
                        )
                        if (user.isOwnerUser) {
                            navTabs.add(MainNavTab.ADMIN)
                        }

                        navTabs.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (tab == MainNavTab.ADMIN) {
                                        isShowingAdmin = true
                                    } else {
                                        selectedTab = tab
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PeacockCyan,
                                    selectedTextColor = PeacockCyan,
                                    unselectedIconColor = Color.White.copy(alpha = 0.45f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.45f),
                                    indicatorColor = RoyalPurple.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (selectedTab) {
                        MainNavTab.ROOMS -> {
                            HomeScreen(
                                currentUser = user,
                                rooms = rooms,
                                allUsers = allUsers,
                                onRoomClick = { room ->
                                    if (room.roomType == "password") {
                                        pendingPasswordRoom = room
                                        enteredPassword = ""
                                        passwordError = null
                                    } else {
                                        activeVoiceRoom = room
                                    }
                                },
                                onCreateRoom = { name, desc, type, pass ->
                                    scope.launch {
                                        val newRoomId = repository.createRoom(name, desc, type, pass)
                                        // Open immediately
                                        val newRoom = ChatRoom(
                                            roomId = newRoomId,
                                            name = name,
                                            description = desc,
                                            roomType = type,
                                            password = pass,
                                            ownerId = user.userId,
                                            ownerName = user.displayName,
                                            activeMicSeats = mapOf("0" to user.userId)
                                        )
                                        activeVoiceRoom = newRoom
                                    }
                                },
                                onNavigateToAdmin = { isShowingAdmin = true },
                                onNavigateToSupport = {
                                    // Switch to profile or open dialer
                                    selectedTab = MainNavTab.PROFILE
                                },
                                onUserClick = { clickedUser ->
                                    activePrivateChat = Pair(clickedUser.userId, clickedUser.displayName)
                                }
                            )
                        }
                        MainNavTab.CHATS -> {
                            PrivateChatListScreen(
                                currentUser = user,
                                allUsers = allUsers,
                                repository = repository,
                                onOpenChat = { otherUid, otherName ->
                                    activePrivateChat = Pair(otherUid, otherName)
                                }
                            )
                        }
                        MainNavTab.GAMES -> {
                            GamesHubScreen(
                                currentUser = user,
                                repository = repository
                            )
                        }
                        MainNavTab.PROFILE -> {
                            ProfileScreen(
                                currentUser = user,
                                repository = repository,
                                authManager = authManager,
                                onSignOut = {
                                    // Handled by Auth state listener
                                }
                            )
                        }
                        MainNavTab.ADMIN -> {
                            isShowingAdmin = true
                        }
                    }
                }
            }
        }
    }

    // Password Protected Room Verification Dialog
    if (pendingPasswordRoom != null) {
        val room = pendingPasswordRoom!!
        AlertDialog(
            onDismissRequest = { pendingPasswordRoom = null },
            title = { Text("Enter Room Password") },
            text = {
                Column {
                    Text("This room is password protected. Please enter password:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPassword,
                        onValueChange = { enteredPassword = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredPassword == room.password || user.isOwnerUser || room.ownerId == user.userId) {
                            activeVoiceRoom = room
                            pendingPasswordRoom = null
                        } else {
                            passwordError = "Incorrect room password. Try again."
                        }
                    }
                ) {
                    Text("Join Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPasswordRoom = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
