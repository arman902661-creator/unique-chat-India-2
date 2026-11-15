package com.example.ui.screens.profile

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AuthManager
import com.example.data.VoiceChatRepository
import com.example.model.User
import com.example.ui.theme.AccentRedLive
import com.example.ui.theme.GoldenBadge
import com.example.ui.theme.IndiaAmber
import com.example.ui.theme.IndiaSaffron
import com.example.ui.theme.MidnightDeep
import com.example.ui.theme.PeacockCyan
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    currentUser: User,
    repository: VoiceChatRepository,
    authManager: AuthManager,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentUser.displayName) }
    var editBio by remember { mutableStateOf(currentUser.bio) }
    var editPhone by remember { mutableStateOf(currentUser.phoneNumber) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDeep)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Profile Avatar Card
        Box(contentAlignment = Alignment.Center) {
            if (currentUser.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = currentUser.photoUrl,
                    contentDescription = currentUser.displayName,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(2.dp, PeacockCyan, CircleShape)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(RoyalPurple, IndiaSaffron))
                        )
                ) {
                    Text(
                        text = currentUser.displayName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(RoyalPurple)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = currentUser.displayName,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 20.sp
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Unique ID: ${currentUser.uniqueUserId}",
                color = PeacockCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (currentUser.isOwnerUser) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IndiaSaffron)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("OWNER", color = MidnightDeep, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = currentUser.bio,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Virtual Coin Balance Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Virtual Wallet", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🪙 ${currentUser.virtualCoins} Coins", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = GoldenBadge)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(RoyalPurple.copy(alpha = 0.3f))
                        .border(1.dp, PeacockCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Active Member", color = PeacockCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Actions Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile & Bio",
                    subtitle = "Update display name and bio",
                    onClick = { showEditDialog = true }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Call,
                    title = "Help & Customer Support",
                    subtitle = "Helpline: 9219240590",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:9219240590")
                        }
                        context.startActivity(intent)
                    }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Block,
                    title = "Blocked Members",
                    subtitle = "${currentUser.blockedUsers.size} users blocked",
                    onClick = {}
                )
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About UNIQUE CHAT INDIA",
                    subtitle = "Version 1.0 • Owner: arman902661@gmail.com",
                    onClick = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sign Out and Delete Account
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                ProfileMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Sign Out",
                    subtitle = "Log out of this device",
                    iconTint = IndiaAmber,
                    onClick = {
                        authManager.signOut(scope) {
                            onSignOut()
                        }
                    }
                )
                ProfileMenuItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    subtitle = "Permanently remove your account and data",
                    iconTint = AccentRedLive,
                    onClick = { showDeleteConfirm = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit User Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Mobile Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.updateProfile(currentUser.userId, editName.trim(), editBio.trim(), editPhone.trim())
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?") },
            text = {
                Text("Are you sure you want to delete your account? All your chats, rooms, and coins will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            authManager.deleteAccount()
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRedLive)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = PeacockCyan,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(12.dp)
        )
    }
}
