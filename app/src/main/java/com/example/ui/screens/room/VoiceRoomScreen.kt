package com.example.ui.screens.room

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.VoiceChatRepository
import com.example.data.VoiceRtcService
import com.example.model.ChatRoom
import com.example.model.RoomMessage
import com.example.model.User
import com.example.ui.components.GiftDialog
import com.example.ui.components.MicSeatItem
import com.example.ui.theme.AccentGreenOnline
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
fun VoiceRoomScreen(
    room: ChatRoom,
    currentUser: User,
    allUsers: List<User>,
    repository: VoiceChatRepository,
    voiceRtcService: VoiceRtcService,
    onLeaveRoom: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var roomState by remember { mutableStateOf(room) }
    val messages by repository.observeRoomMessages(room.roomId).collectAsState(initial = emptyList())
    val rtcMuted by voiceRtcService.isMicrophoneMuted.collectAsState()
    val rtcSpeakerOn by voiceRtcService.isSpeakerphoneOn.collectAsState()
    val localAudioAmplitude by voiceRtcService.localAudioAmplitude.collectAsState()

    var showGiftDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }
    var selectedSeatForModeration by remember { mutableStateOf<Pair<Int, String>?>(null) } // (seatIndex, userId)

    val listState = rememberLazyListState()

    // Check Audio Permission
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            voiceRtcService.setMicrophoneMuted(false, true)
        }
    }

    // Join RTC Channel
    LaunchedEffect(room.roomId) {
        voiceRtcService.joinVoiceChannel(room.roomId, currentUser.userId)
        // Listen to room updates
        repository.observeRoom(room.roomId).collect { updated ->
            if (updated != null) {
                roomState = updated
            }
        }
    }

    // Leave on exit
    DisposableEffect(room.roomId) {
        onDispose {
            voiceRtcService.leaveVoiceChannel()
        }
    }

    BackHandler {
        onLeaveRoom()
    }

    // Scroll chat to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Identify user's current seat
    val currentUserSeatIndex = roomState.activeMicSeats.entries
        .firstOrNull { it.value == currentUser.userId }?.key?.toIntOrNull()

    val isUserOnMic = currentUserSeatIndex != null
    val isOwner = roomState.ownerId == currentUser.userId || currentUser.isOwnerUser
    val isModerator = isOwner || roomState.moderatorIds.contains(currentUser.userId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MidnightDeep, Color(0xFF140E2C), MidnightDeep)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLeaveRoom) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = roomState.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentRedLive)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Host: ${roomState.ownerName} • ID: ${roomState.roomId.take(8)}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Speakerphone Toggle
                    IconButton(onClick = { voiceRtcService.toggleSpeakerphone() }) {
                        Icon(
                            imageVector = if (rtcSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Speaker",
                            tint = PeacockCyan
                        )
                    }

                    // Room Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Room") },
                                onClick = {
                                    showMenu = false
                                    showReportDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Report, contentDescription = null, tint = AccentRedLive) }
                            )
                            DropdownMenuItem(
                                text = { Text("Leave Room") },
                                onClick = {
                                    showMenu = false
                                    onLeaveRoom()
                                },
                                leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                            )
                        }
                    }
                }
            }

            // Announcement Banner
            if (roomState.announcement.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceCard)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = IndiaAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = roomState.announcement,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // 8-Seat Voice Stage (4 seats per row)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Row 1: Seats 0..3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (seatIndex in 0..3) {
                            val seatKey = seatIndex.toString()
                            val occupantUid = roomState.activeMicSeats[seatKey]
                            val occupantUser = allUsers.firstOrNull { it.userId == occupantUid }
                            val isOccupantMuted = roomState.seatMuted[seatKey] ?: false
                            val isThisUserSeat = occupantUid == currentUser.userId
                            val isSpeaking = isThisUserSeat && !rtcMuted && localAudioAmplitude > 0.08f

                            MicSeatItem(
                                seatIndex = seatIndex,
                                occupantUser = occupantUser,
                                isMuted = isOccupantMuted,
                                isSpeaking = isSpeaking,
                                isCurrentUserSeat = isThisUserSeat,
                                onSeatClick = {
                                    if (occupantUid == null) {
                                        // Empty seat -> take it!
                                        if (!hasAudioPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        scope.launch {
                                            repository.takeSeat(roomState.roomId, seatIndex)
                                            voiceRtcService.setMicrophoneMuted(false, hasAudioPermission)
                                        }
                                    } else if (occupantUid == currentUser.userId) {
                                        // Tapping own seat -> toggle mute
                                        val newMuted = !rtcMuted
                                        voiceRtcService.setMicrophoneMuted(newMuted, hasAudioPermission)
                                        scope.launch {
                                            repository.toggleSeatMute(roomState.roomId, seatIndex, newMuted)
                                        }
                                    } else if (isModerator) {
                                        // Moderator tapping on someone -> open moderation
                                        selectedSeatForModeration = Pair(seatIndex, occupantUid)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Seats 4..7
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (seatIndex in 4..7) {
                            val seatKey = seatIndex.toString()
                            val occupantUid = roomState.activeMicSeats[seatKey]
                            val occupantUser = allUsers.firstOrNull { it.userId == occupantUid }
                            val isOccupantMuted = roomState.seatMuted[seatKey] ?: false
                            val isThisUserSeat = occupantUid == currentUser.userId
                            val isSpeaking = isThisUserSeat && !rtcMuted && localAudioAmplitude > 0.08f

                            MicSeatItem(
                                seatIndex = seatIndex,
                                occupantUser = occupantUser,
                                isMuted = isOccupantMuted,
                                isSpeaking = isSpeaking,
                                isCurrentUserSeat = isThisUserSeat,
                                onSeatClick = {
                                    if (occupantUid == null) {
                                        if (!hasAudioPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        scope.launch {
                                            repository.takeSeat(roomState.roomId, seatIndex)
                                            voiceRtcService.setMicrophoneMuted(false, hasAudioPermission)
                                        }
                                    } else if (occupantUid == currentUser.userId) {
                                        val newMuted = !rtcMuted
                                        voiceRtcService.setMicrophoneMuted(newMuted, hasAudioPermission)
                                        scope.launch {
                                            repository.toggleSeatMute(roomState.roomId, seatIndex, newMuted)
                                        }
                                    } else if (isModerator) {
                                        selectedSeatForModeration = Pair(seatIndex, occupantUid)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Live Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                items(messages) { msg ->
                    if (msg.type == "gift") {
                        // Gift Announcement Pill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(RoyalPurple.copy(alpha = 0.4f), IndiaSaffron.copy(alpha = 0.4f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✨ ${msg.senderName}: ${msg.text}",
                                color = GoldenBadge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        // Standard chat message
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${msg.senderName}: ",
                                color = PeacockCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = msg.text,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Bottom Audio & Chat Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If on mic, show mic toggle button
                if (isUserOnMic) {
                    IconButton(
                        onClick = {
                            if (!hasAudioPermission) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                val newMuted = !rtcMuted
                                voiceRtcService.setMicrophoneMuted(newMuted, hasAudioPermission)
                                currentUserSeatIndex?.let { seat ->
                                    scope.launch {
                                        repository.toggleSeatMute(roomState.roomId, seat, newMuted)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (rtcMuted) AccentRedLive else AccentGreenOnline)
                    ) {
                        Icon(
                            imageVector = if (rtcMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Leave Seat Button
                    IconButton(
                        onClick = {
                            currentUserSeatIndex?.let { seat ->
                                scope.launch {
                                    repository.leaveSeat(roomState.roomId, seat)
                                    voiceRtcService.setMicrophoneMuted(true, false)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Leave Seat",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Chat Input Field
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Send a friendly message...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PeacockCyan,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Send Message Button
                IconButton(
                    onClick = {
                        val text = chatInput.trim()
                        if (text.isNotBlank()) {
                            chatInput = ""
                            scope.launch {
                                repository.sendRoomMessage(roomState.roomId, text)
                            }
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RoyalPurple)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Gift Button
                IconButton(
                    onClick = { showGiftDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(IndiaSaffron, GoldenBadge))
                        )
                        .testTag("gift_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Gift",
                        tint = MidnightDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Virtual Gift Dialog
        if (showGiftDialog) {
            GiftDialog(
                userCoins = currentUser.virtualCoins,
                onDismiss = { showGiftDialog = false },
                onSendGift = { gift ->
                    scope.launch {
                        repository.sendVirtualGift(roomState.roomId, gift)
                    }
                }
            )
        }

        // Report Room Dialog
        if (showReportDialog) {
            AlertDialog(
                onDismissRequest = { showReportDialog = false },
                title = { Text("Report Room") },
                text = {
                    Column {
                        Text("Why are you reporting '${roomState.name}'?")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reportReason,
                            onValueChange = { reportReason = it },
                            placeholder = { Text("Abusive language, harassment, etc.") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reportReason.isNotBlank()) {
                                scope.launch {
                                    repository.submitReport("room", roomState.roomId, roomState.name, reportReason.trim())
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

        // Moderator Seat Action Dialog
        if (selectedSeatForModeration != null) {
            val seatIdx = selectedSeatForModeration!!.first
            val targetUid = selectedSeatForModeration!!.second
            val targetUser = allUsers.firstOrNull { it.userId == targetUid }

            AlertDialog(
                onDismissRequest = { selectedSeatForModeration = null },
                title = { Text("Moderate Seat ${seatIdx + 1}") },
                text = {
                    Text("Action for ${targetUser?.displayName ?: "User"}:")
                },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.removeUserFromSeat(roomState.roomId, seatIdx)
                                }
                                selectedSeatForModeration = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndiaAmber),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove from Mic Seat")
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.removeUserFromSeat(roomState.roomId, seatIdx)
                                    repository.banUserFromRoom(roomState.roomId, targetUid)
                                }
                                selectedSeatForModeration = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRedLive),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ban User from Room")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedSeatForModeration = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
