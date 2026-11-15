package com.example.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VoiceChatRepository
import com.example.model.AppConfig
import com.example.model.ChatRoom
import com.example.model.Report
import com.example.model.User
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
fun OwnerAdminScreen(
    currentUser: User,
    rooms: List<ChatRoom>,
    allUsers: List<User>,
    repository: VoiceChatRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Users", "Rooms", "Reports", "Broadcast")

    val reports by repository.observeReports().collectAsState(initial = emptyList())
    val appConfig by repository.observeAppConfig().collectAsState(initial = AppConfig())

    var userSearchQuery by remember { mutableStateOf("") }
    var announcementText by remember { mutableStateOf(appConfig.announcement) }
    var selectedUserForCoins by remember { mutableStateOf<User?>(null) }
    var coinsAmountInput by remember { mutableStateOf("500") }

    BackHandler {
        onBack()
    }

    // Security Gate check
    if (!currentUser.isOwnerUser) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightDeep)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentRedLive, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Access Restricted",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Only the authorized owner (arman902661@gmail.com) can access this dashboard.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = RoyalPurple)) {
                    Text("Return to App")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDeep)
            .padding(16.dp)
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Owner Admin Panel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndiaSaffron, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "Authorized Owner: arman902661@gmail.com",
                        color = IndiaSaffron,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceElevated,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = IndiaSaffron
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AdminOverview(allUsers, rooms, reports)
            1 -> AdminUserManagement(
                users = allUsers.filter {
                    it.displayName.contains(userSearchQuery, ignoreCase = true) ||
                            it.uniqueUserId.contains(userSearchQuery, ignoreCase = true) ||
                            it.email.contains(userSearchQuery, ignoreCase = true)
                },
                searchQuery = userSearchQuery,
                onSearchChange = { userSearchQuery = it },
                onToggleBan = { uid, ban ->
                    scope.launch { repository.setUserBanned(uid, ban) }
                },
                onGrantCoinsClick = { user -> selectedUserForCoins = user }
            )
            2 -> AdminRoomManagement(
                rooms = rooms,
                onDeleteRoom = { roomId ->
                    scope.launch { repository.deleteRoom(roomId) }
                }
            )
            3 -> AdminReportsManagement(
                reports = reports,
                onResolveReport = { reportId, status ->
                    scope.launch { repository.resolveReport(reportId, status) }
                }
            )
            4 -> AdminBroadcastSettings(
                announcement = announcementText,
                onAnnouncementChange = { announcementText = it },
                onSaveAnnouncement = {
                    scope.launch { repository.updateAppConfig(announcementText, appConfig.maintenanceMode) }
                }
            )
        }
    }

    // Grant Coins Dialog
    if (selectedUserForCoins != null) {
        val targetUser = selectedUserForCoins!!
        AlertDialog(
            onDismissRequest = { selectedUserForCoins = null },
            title = { Text("Grant Virtual Coins") },
            text = {
                Column {
                    Text("Add coins to ${targetUser.displayName} (${targetUser.uniqueUserId}):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = coinsAmountInput,
                        onValueChange = { coinsAmountInput = it },
                        label = { Text("Coins Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = coinsAmountInput.toLongOrNull() ?: 500L
                        scope.launch {
                            repository.grantCoins(targetUser.userId, amount)
                        }
                        selectedUserForCoins = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenBadge)
                ) {
                    Text("Grant Coins", color = MidnightDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForCoins = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverview(users: List<User>, rooms: List<ChatRoom>, reports: List<Report>) {
    val onlineCount = users.count { it.isOnline }
    val bannedCount = users.count { it.isBanned }
    val pendingReports = reports.count { it.status == "pending" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Users",
                value = "${users.size}",
                icon = Icons.Default.People,
                color = PeacockCyan,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Online Now",
                value = "$onlineCount",
                icon = Icons.Default.Done,
                color = AccentGreenOnline,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Active Rooms",
                value = "${rooms.size}",
                icon = Icons.Default.VolumeUp,
                color = RoyalPurple,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Pending Reports",
                value = "$pendingReports",
                icon = Icons.Default.Report,
                color = if (pendingReports > 0) AccentRedLive else GoldenBadge,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "System Information",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Customer Support Line: 9219240590", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text("• Owner Master Email: arman902661@gmail.com", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text("• Banned Users: $bannedCount accounts", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text("• Database: Cloud Firestore Enterprise Edition", color = PeacockCyan, fontSize = 13.sp)
                Text("• Security: Role-Based Access Control + Security Rules", color = AccentGreenOnline, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun AdminUserManagement(
    users: List<User>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleBan: (userId: String, ban: Boolean) -> Unit,
    onGrantCoinsClick: (User) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by ID (e.g. UCI-10928) or Name", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PeacockCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = PeacockCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(users) { user ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (user.isBanned) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(AccentRedLive)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("BANNED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(
                                text = "ID: ${user.uniqueUserId} • 🪙 ${user.virtualCoins} Coins",
                                color = PeacockCyan,
                                fontSize = 12.sp
                            )
                            Text(
                                text = user.email.ifBlank { "No email registered" },
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onGrantCoinsClick(user) }) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = "Grant Coins", tint = GoldenBadge)
                            }
                            Button(
                                onClick = { onToggleBan(user.userId, !user.isBanned) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isBanned) AccentGreenOnline else AccentRedLive
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (user.isBanned) "Unban" else "Ban",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
fun AdminRoomManagement(rooms: List<ChatRoom>, onDeleteRoom: (roomId: String) -> Unit) {
    if (rooms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active rooms", color = Color.White.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rooms) { room ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = room.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Host: ${room.ownerName} • Type: ${room.roomType}", color = PeacockCyan, fontSize = 12.sp)
                            Text(text = "${room.activeMicSeats.size}/8 on mic", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { onDeleteRoom(room.roomId) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = AccentRedLive)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportsManagement(reports: List<Report>, onResolveReport: (String, String) -> Unit) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No reports filed. Community is safe! 🛡️", color = Color.White.copy(alpha = 0.6f))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(reports) { r ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Reported ${r.targetType.uppercase()}: ${r.targetName}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (r.status == "pending") AccentRedLive else AccentGreenOnline)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = r.status.uppercase(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Reason: ${r.reason}", color = IndiaAmber, fontSize = 12.sp)
                        Text(text = "Reporter: ${r.reporterName}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onResolveReport(r.reportId, "resolved") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreenOnline),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Resolve", fontSize = 11.sp, color = MidnightDeep, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onResolveReport(r.reportId, "dismissed") },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Dismiss", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBroadcastSettings(announcement: String, onAnnouncementChange: (String) -> Unit, onSaveAnnouncement: () -> Unit) {
    Column {
        Text("App-Wide Announcement Banner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = announcement,
            onValueChange = onAnnouncementChange,
            placeholder = { Text("Welcome message shown across app rooms") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = IndiaSaffron
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSaveAnnouncement,
            colors = ButtonDefaults.buttonColors(containerColor = IndiaSaffron),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Broadcast Announcement 📢", color = MidnightDeep, fontWeight = FontWeight.Bold)
        }
    }
}
