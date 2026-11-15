package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PrivateChat(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val participants: List<String> = emptyList(), // [userId1, userId2]
    val participantNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val unreadCount: Map<String, Long> = emptyMap(),
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PrivateMessage(
    @DocumentId val id: String = "",
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class GameSession(
    @DocumentId val id: String = "",
    val gameId: String = "",
    val gameType: String = "tictactoe", // "tictactoe", "ludo", "chess", "quiz"
    val players: List<String> = emptyList(),
    val playerNames: Map<String, String> = emptyMap(),
    val currentTurn: String = "",
    val status: String = "waiting", // "waiting", "playing", "completed"
    val boardStateJson: String = "", // serialized game grid / tokens
    val winnerId: String = "",
    val score: Map<String, Long> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Report(
    @DocumentId val id: String = "",
    val reportId: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val targetType: String = "user", // "user", "room"
    val targetId: String = "",
    val targetName: String = "",
    val reason: String = "",
    val status: String = "pending", // "pending", "resolved", "dismissed"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AppConfig(
    @DocumentId val id: String = "global",
    val customerSupportNumber: String = "9219240590",
    val ownerEmail: String = "arman902661@gmail.com",
    val announcement: String = "Welcome to UNIQUE CHAT INDIA! Connect, chat, talk and play safely.",
    val maintenanceMode: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
