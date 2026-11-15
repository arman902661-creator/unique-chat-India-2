package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class User(
    @DocumentId val id: String = "",
    val userId: String = "",
    val uniqueUserId: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val displayName: String = "Chat Member",
    val photoUrl: String = "",
    val role: String = "user", // "user", "moderator", "admin", "owner"
    val isOnline: Boolean = true,
    val isBanned: Boolean = false,
    val virtualCoins: Long = 1000L, // Free starter coins for gifts
    val bio: String = "Namaste! Welcome to Unique Chat India",
    val blockedUsers: List<String> = emptyList(),
    val friendsList: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isOwnerUser: Boolean
        get() = email.equals("arman902661@gmail.com", ignoreCase = true) || role == "owner" || role == "admin"
}
