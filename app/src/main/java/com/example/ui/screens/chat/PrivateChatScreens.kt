package com.example.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.VoiceChatRepository
import com.example.model.PrivateChat
import com.example.model.PrivateMessage
import com.example.model.User
import com.example.ui.theme.AccentGreenOnline
import com.example.ui.theme.AccentRedLive
import com.example.ui.theme.IndiaSaffron
import com.example.ui.theme.MidnightDeep
import com.example.ui.theme.PeacockCyan
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun PrivateChatListScreen(
    currentUser: User,
    allUsers: List<User>,
    repository: VoiceChatRepository,
    onOpenChat: (otherUserId: String, otherUserName: String) -> Unit
) {
    val chats by repository.observeUserChats(currentUser.userId).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDeep)
            .padding(16.dp)
    ) {
        Text(
            text = "Private Messages",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Direct 1-on-1 end-to-end synchronized chats",
            fontSize = 12.sp,
            color = PeacockCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No private chats yet",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap on any member from Home or Rooms to start chatting!",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chats) { chat ->
                    val otherUid = chat.participants.firstOrNull { it != currentUser.userId } ?: ""
                    val otherUser = allUsers.firstOrNull { it.userId == otherUid }
                    val otherName = otherUser?.displayName
                        ?: chat.participantNames[otherUid]
                        ?: "Chat User"

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChat(otherUid, otherName) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (otherUser?.photoUrl?.isNotBlank() == true) {
                                    AsyncImage(
                                        model = otherUser.photoUrl,
                                        contentDescription = otherName,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(RoyalPurple, IndiaSaffron))
                                            )
                                    ) {
                                        Text(
                                            text = otherName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                if (otherUser?.isOnline == true) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(AccentGreenOnline)
                                            .border(1.5.dp, MidnightDeep, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = otherName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = if (chat.lastMessage.isNotBlank()) chat.lastMessage else "Tap to chat",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateChatDetailScreen(
    currentUserId: String,
    otherUserId: String,
    otherUserName: String,
    repository: VoiceChatRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var chatId by remember { mutableStateOf<String?>(null) }
    var chatInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    val otherUser by repository.observeUser(otherUserId).collectAsState(initial = null)
    val listState = rememberLazyListState()

    LaunchedEffect(otherUserId) {
        chatId = repository.getOrCreatePrivateChat(otherUserId, otherUserName)
    }

    val messages = if (chatId != null) {
        repository.observePrivateMessages(chatId!!).collectAsState(initial = emptyList()).value
    } else emptyList()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDeep)
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Text(
                        text = otherUser?.displayName ?: otherUserName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (otherUser?.isOnline == true) "Online • Active Now" else "Offline",
                        color = if (otherUser?.isOnline == true) AccentGreenOnline else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Report User") },
                        onClick = {
                            showMenu = false
                            showReportDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = AccentRedLive) }
                    )
                    DropdownMenuItem(
                        text = { Text("Block User") },
                        onClick = {
                            showMenu = false
                            scope.launch {
                                repository.toggleBlockUser(otherUserId, true)
                                onBack()
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) }
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUserId
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isMe) 14.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 14.dp
                                )
                            )
                            .background(if (isMe) RoyalPurple else SurfaceCard)
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (isMe) {
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (msg.isRead) Icons.Default.DoneAll else Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (msg.isRead) PeacockCyan else Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input Field Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Type a private message...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = PeacockCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("private_chat_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val text = chatInput.trim()
                    if (text.isNotBlank() && chatId != null) {
                        chatInput = ""
                        scope.launch {
                            repository.sendPrivateMessage(chatId!!, otherUserId, text)
                        }
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(RoyalPurple)
                    .testTag("send_private_msg_btn")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User") },
            text = {
                Column {
                    Text("State reason for reporting this user:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Spam, harassment, inappropriate behavior") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reportReason.isNotBlank()) {
                            scope.launch {
                                repository.submitReport("user", otherUserId, otherUserName, reportReason.trim())
                            }
                            showReportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRedLive)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
