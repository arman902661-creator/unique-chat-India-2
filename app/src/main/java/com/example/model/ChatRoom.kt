package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatRoom(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val name: String = "",
    val description: String = "",
    val roomType: String = "public", // "public", "private", "password"
    val password: String = "",
    val roomImageUrl: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val moderatorIds: List<String> = emptyList(),
    val bannedUserIds: List<String> = emptyList(),
    val mutedUserIds: List<String> = emptyList(),
    val activeMicSeats: Map<String, String> = emptyMap(), // seatIndex (0..7) -> userId
    val seatMuted: Map<String, Boolean> = emptyMap(), // seatIndex -> isMuted
    val announcement: String = "Welcome to our room! Please respect everyone.",
    val isLive: Boolean = true,
    val memberCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RoomMessage(
    @DocumentId val id: String = "",
    val messageId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val text: String = "",
    val type: String = "text", // "text", "gift", "system"
    val giftName: String = "",
    val giftIcon: String = "",
    val giftValue: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class VirtualGift(
    val id: String,
    val name: String,
    val emoji: String,
    val coinCost: Long,
    val animationDescription: String
)

object VirtualGiftCatalog {
    val gifts = listOf(
        VirtualGift("rose", "Rose", "🌹", 10L, "Sent a romantic red rose!"),
        VirtualGift("heart", "Heart", "❤️", 25L, "Sent glowing love hearts!"),
        VirtualGift("chai", "Masala Chai", "☕", 50L, "Offered hot aromatic Indian Chai!"),
        VirtualGift("crown", "Golden Crown", "👑", 200L, "Crowned the speaker in royal gold!"),
        VirtualGift("diya", "Festival Diya", "🪔", 100L, "Lit up the room with festive light!"),
        VirtualGift("car", "Supercar", "🏎️", 500L, "Drove in with a luxury supercar!"),
        VirtualGift("fireworks", "India Fireworks", "🎆", 1000L, "Launched spectacular nationwide fireworks!"),
        VirtualGift("diamond", "Koh-i-Noor Diamond", "💎", 2000L, "Presented a sparkling legendary diamond!")
    )
}
