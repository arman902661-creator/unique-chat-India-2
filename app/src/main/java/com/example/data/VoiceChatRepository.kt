package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.AppConfig
import com.example.model.ChatRoom
import com.example.model.GameSession
import com.example.model.PrivateChat
import com.example.model.PrivateMessage
import com.example.model.Report
import com.example.model.RoomMessage
import com.example.model.User
import com.example.model.VirtualGift
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VoiceChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing database operations.")
    }

    private fun logError(operation: String, path: String, e: Exception) {
        Log.e("VoiceChatRepository", "Error during $operation on $path: ${e.message}", e)
    }

    // -------------------------------------------------------------
    // USER PROFILE & ROLES
    // -------------------------------------------------------------

    suspend fun getOrCreateUserProfile(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        val uid = firebaseUser.uid
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.copy(userId = uid) ?: User(userId = uid)
                // Set online
                docRef.update("isOnline", true, "updatedAt", FieldValue.serverTimestamp()).await()
                user
            } else {
                val shortId = "UCI-" + (10000 + (Math.random() * 89999).toInt())
                val isOwner = firebaseUser.email.equals("arman902661@gmail.com", ignoreCase = true)
                val newUser = User(
                    userId = uid,
                    uniqueUserId = shortId,
                    email = firebaseUser.email ?: "",
                    phoneNumber = firebaseUser.phoneNumber ?: "",
                    displayName = firebaseUser.displayName ?: "User $shortId",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    role = if (isOwner) "owner" else "user",
                    isOnline = true,
                    virtualCoins = 1500L,
                    bio = "Namaste! Welcome to UNIQUE CHAT INDIA."
                )
                val payload = mapOf(
                    "userId" to uid,
                    "uniqueUserId" to newUser.uniqueUserId,
                    "email" to newUser.email,
                    "phoneNumber" to newUser.phoneNumber,
                    "displayName" to newUser.displayName,
                    "photoUrl" to newUser.photoUrl,
                    "role" to newUser.role,
                    "isOnline" to true,
                    "isBanned" to false,
                    "virtualCoins" to newUser.virtualCoins,
                    "bio" to newUser.bio,
                    "blockedUsers" to emptyList<String>(),
                    "friendsList" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                newUser
            }
        } catch (e: Exception) {
            logError("getOrCreateUserProfile", "users/$uid", e)
            throw e
        }
    }

    fun observeUser(userId: String): Flow<User?> = flow {
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snap -> snap.toObject(User::class.java)?.copy(userId = snap.id) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeUser", "users/$userId", e as? Exception ?: Exception(e))
                    emit(null)
                }
        )
    }

    suspend fun updateProfile(userId: String, displayName: String, bio: String, phoneNumber: String) {
        val uid = requireUserId()
        if (uid != userId && !isCurrentUserOwner()) {
            throw IllegalAccessException("Cannot edit another user profile")
        }
        val docRef = db.collection("users").document(userId)
        docRef.update(
            "displayName", displayName,
            "bio", bio,
            "phoneNumber", phoneNumber,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun setUserOnline(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid).update(
                "isOnline", isOnline,
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            // Ignored on app shutdown
        }
    }

    fun observeAllUsers(): Flow<List<User>> = flow {
        emitAll(
            db.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(User::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeAllUsers", "users", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    suspend fun setUserBanned(userId: String, isBanned: Boolean) {
        db.collection("users").document(userId).update(
            "isBanned", isBanned,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun grantCoins(userId: String, amount: Long) {
        db.collection("users").document(userId).update(
            "virtualCoins", FieldValue.increment(amount),
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun toggleBlockUser(targetUserId: String, block: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        if (block) {
            docRef.update("blockedUsers", FieldValue.arrayUnion(targetUserId)).await()
        } else {
            docRef.update("blockedUsers", FieldValue.arrayRemove(targetUserId)).await()
        }
    }

    private fun isCurrentUserOwner(): Boolean {
        val email = auth.currentUser?.email
        return email.equals("arman902661@gmail.com", ignoreCase = true)
    }

    // -------------------------------------------------------------
    // LIVE VOICE CHAT ROOMS
    // -------------------------------------------------------------

    fun observeRooms(): Flow<List<ChatRoom>> = flow {
        emitAll(
            db.collection("rooms")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(ChatRoom::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeRooms", "rooms", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    fun observeRoom(roomId: String): Flow<ChatRoom?> = flow {
        emitAll(
            db.collection("rooms").document(roomId)
                .snapshots()
                .map { snap -> snap.toObject(ChatRoom::class.java)?.copy(roomId = snap.id) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeRoom", "rooms/$roomId", e as? Exception ?: Exception(e))
                    emit(null)
                }
        )
    }

    suspend fun createRoom(
        name: String,
        description: String,
        roomType: String,
        password: String
    ): String {
        val uid = requireUserId()
        val ownerName = auth.currentUser?.displayName ?: "Host"
        val docRef = db.collection("rooms").document()
        val roomId = docRef.id

        val payload = mapOf(
            "roomId" to roomId,
            "name" to name,
            "description" to description,
            "roomType" to roomType,
            "password" to password,
            "roomImageUrl" to "",
            "ownerId" to uid,
            "ownerName" to ownerName,
            "moderatorIds" to emptyList<String>(),
            "bannedUserIds" to emptyList<String>(),
            "mutedUserIds" to emptyList<String>(),
            "activeMicSeats" to mapOf("0" to uid), // Host automatically takes Seat 0
            "seatMuted" to mapOf("0" to false),
            "announcement" to "Welcome to $name! Respect all members.",
            "isLive" to true,
            "memberCount" to 1L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return roomId
    }

    suspend fun deleteRoom(roomId: String) {
        db.collection("rooms").document(roomId).delete().await()
    }

    suspend fun takeSeat(roomId: String, seatIndex: Int) {
        val uid = requireUserId()
        val seatKey = seatIndex.toString()
        val docRef = db.collection("rooms").document(roomId)
        docRef.update(
            "activeMicSeats.$seatKey", uid,
            "seatMuted.$seatKey", false,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun leaveSeat(roomId: String, seatIndex: Int) {
        val seatKey = seatIndex.toString()
        val docRef = db.collection("rooms").document(roomId)
        docRef.update(
            "activeMicSeats.$seatKey", FieldValue.delete(),
            "seatMuted.$seatKey", FieldValue.delete(),
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun toggleSeatMute(roomId: String, seatIndex: Int, isMuted: Boolean) {
        val seatKey = seatIndex.toString()
        val docRef = db.collection("rooms").document(roomId)
        docRef.update(
            "seatMuted.$seatKey", isMuted,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun removeUserFromSeat(roomId: String, seatIndex: Int) {
        leaveSeat(roomId, seatIndex)
    }

    suspend fun banUserFromRoom(roomId: String, targetUserId: String) {
        val docRef = db.collection("rooms").document(roomId)
        docRef.update(
            "bannedUserIds", FieldValue.arrayUnion(targetUserId),
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun updateRoomAnnouncement(roomId: String, announcement: String) {
        val docRef = db.collection("rooms").document(roomId)
        docRef.update(
            "announcement", announcement,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // Room In-Chat Messages
    fun observeRoomMessages(roomId: String): Flow<List<RoomMessage>> = flow {
        emitAll(
            db.collection("rooms").document(roomId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(50)
                .snapshots()
                .map { snap -> snap.toObjects(RoomMessage::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeRoomMessages", "rooms/$roomId/messages", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    suspend fun sendRoomMessage(roomId: String, text: String) {
        val uid = requireUserId()
        val user = auth.currentUser
        val docRef = db.collection("rooms").document(roomId).collection("messages").document()
        val payload = mapOf(
            "messageId" to docRef.id,
            "roomId" to roomId,
            "senderId" to uid,
            "senderName" to (user?.displayName ?: "Member"),
            "senderAvatar" to (user?.photoUrl?.toString() ?: ""),
            "text" to text,
            "type" to "text",
            "giftName" to "",
            "giftIcon" to "",
            "giftValue" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun sendVirtualGift(roomId: String, gift: VirtualGift): Result<Unit> {
        val uid = requireUserId()
        val user = auth.currentUser
        val userDoc = db.collection("users").document(uid)

        return try {
            val userSnap = userDoc.get().await()
            val currentCoins = userSnap.getLong("virtualCoins") ?: 0L
            if (currentCoins < gift.coinCost) {
                return Result.failure(IllegalStateException("Insufficient coins. You have $currentCoins coins."))
            }

            // Deduct coins
            userDoc.update("virtualCoins", FieldValue.increment(-gift.coinCost)).await()

            // Post Gift announcement in room chat
            val msgRef = db.collection("rooms").document(roomId).collection("messages").document()
            val payload = mapOf(
                "messageId" to msgRef.id,
                "roomId" to roomId,
                "senderId" to uid,
                "senderName" to (user?.displayName ?: "Member"),
                "senderAvatar" to (user?.photoUrl?.toString() ?: ""),
                "text" to "${gift.emoji} sent ${gift.name}! ${gift.animationDescription}",
                "type" to "gift",
                "giftName" to gift.name,
                "giftIcon" to gift.emoji,
                "giftValue" to gift.coinCost,
                "createdAt" to FieldValue.serverTimestamp()
            )
            msgRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            logError("sendVirtualGift", "rooms/$roomId", e)
            Result.failure(e)
        }
    }

    // -------------------------------------------------------------
    // PRIVATE 1-ON-1 CHATS
    // -------------------------------------------------------------

    fun observeUserChats(userId: String): Flow<List<PrivateChat>> = flow {
        emitAll(
            db.collection("chats")
                .whereArrayContains("participants", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(PrivateChat::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeUserChats", "chats", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    suspend fun getOrCreatePrivateChat(otherUserId: String, otherUserName: String): String {
        val uid = requireUserId()
        val currentUserName = auth.currentUser?.displayName ?: "User"
        val participants = listOf(uid, otherUserId).sorted()
        val chatId = "${participants[0]}_${participants[1]}"
        val docRef = db.collection("chats").document(chatId)

        val snap = docRef.get().await()
        if (!snap.exists()) {
            val payload = mapOf(
                "chatId" to chatId,
                "participants" to participants,
                "participantNames" to mapOf(uid to currentUserName, otherUserId to otherUserName),
                "lastMessage" to "Conversation started",
                "lastSenderId" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        }
        return chatId
    }

    fun observePrivateMessages(chatId: String): Flow<List<PrivateMessage>> = flow {
        emitAll(
            db.collection("chats").document(chatId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(100)
                .snapshots()
                .map { snap -> snap.toObjects(PrivateMessage::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observePrivateMessages", "chats/$chatId/messages", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    suspend fun sendPrivateMessage(chatId: String, receiverId: String, text: String, imageUrl: String = "") {
        val uid = requireUserId()
        val docRef = db.collection("chats").document(chatId).collection("messages").document()
        val payload = mapOf(
            "messageId" to docRef.id,
            "chatId" to chatId,
            "senderId" to uid,
            "receiverId" to receiverId,
            "text" to text,
            "imageUrl" to imageUrl,
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()

        // Update parent chat snippet
        db.collection("chats").document(chatId).update(
            "lastMessage", text,
            "lastSenderId", uid,
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // -------------------------------------------------------------
    // MULTIPLAYER GAMES
    // -------------------------------------------------------------

    fun observeGameSessions(gameType: String): Flow<List<GameSession>> = flow {
        emitAll(
            db.collection("games")
                .whereEqualTo("gameType", gameType)
                .whereEqualTo("status", "waiting")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(GameSession::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeGameSessions", "games", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    fun observeGame(gameId: String): Flow<GameSession?> = flow {
        emitAll(
            db.collection("games").document(gameId)
                .snapshots()
                .map { snap -> snap.toObject(GameSession::class.java)?.copy(gameId = snap.id) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeGame", "games/$gameId", e as? Exception ?: Exception(e))
                    emit(null)
                }
        )
    }

    suspend fun createGame(gameType: String, initialBoardJson: String): String {
        val uid = requireUserId()
        val userName = auth.currentUser?.displayName ?: "Player 1"
        val docRef = db.collection("games").document()
        val payload = mapOf(
            "gameId" to docRef.id,
            "gameType" to gameType,
            "players" to listOf(uid),
            "playerNames" to mapOf(uid to userName),
            "currentTurn" to uid,
            "status" to "waiting",
            "boardStateJson" to initialBoardJson,
            "winnerId" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun joinGame(gameId: String) {
        val uid = requireUserId()
        val userName = auth.currentUser?.displayName ?: "Player 2"
        val docRef = db.collection("games").document(gameId)
        docRef.update(
            "players", FieldValue.arrayUnion(uid),
            "playerNames.$uid", userName,
            "status", "playing",
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun makeGameMove(gameId: String, nextTurnUserId: String, updatedBoardJson: String, winnerId: String = "") {
        val isFinished = winnerId.isNotBlank()
        val docRef = db.collection("games").document(gameId)
        docRef.update(
            "boardStateJson", updatedBoardJson,
            "currentTurn", nextTurnUserId,
            "winnerId", winnerId,
            "status", if (isFinished) "completed" else "playing",
            "updatedAt", FieldValue.serverTimestamp()
        ).await()
    }

    // -------------------------------------------------------------
    // REPORTS & AUDIT
    // -------------------------------------------------------------

    suspend fun submitReport(targetType: String, targetId: String, targetName: String, reason: String) {
        val uid = requireUserId()
        val reporterName = auth.currentUser?.displayName ?: "User"
        val docRef = db.collection("reports").document()
        val payload = mapOf(
            "reportId" to docRef.id,
            "reporterId" to uid,
            "reporterName" to reporterName,
            "targetType" to targetType,
            "targetId" to targetId,
            "targetName" to targetName,
            "reason" to reason,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    fun observeReports(): Flow<List<Report>> = flow {
        emitAll(
            db.collection("reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snap -> snap.toObjects(Report::class.java) }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeReports", "reports", e as? Exception ?: Exception(e))
                    emit(emptyList())
                }
        )
    }

    suspend fun resolveReport(reportId: String, newStatus: String) {
        db.collection("reports").document(reportId).update(
            "status", newStatus
        ).await()
    }

    // -------------------------------------------------------------
    // GLOBAL APP CONFIG
    // -------------------------------------------------------------

    fun observeAppConfig(): Flow<AppConfig> = flow {
        emitAll(
            db.collection("app_config").document("global")
                .snapshots()
                .map { snap -> snap.toObject(AppConfig::class.java) ?: AppConfig() }
                .catch { e ->
                    if (e is CancellationException) throw e
                    logError("observeAppConfig", "app_config/global", e as? Exception ?: Exception(e))
                    emit(AppConfig())
                }
        )
    }

    suspend fun updateAppConfig(announcement: String, maintenanceMode: Boolean) {
        db.collection("app_config").document("global").set(
            mapOf(
                "customerSupportNumber" to "9219240590",
                "ownerEmail" to "arman902661@gmail.com",
                "announcement" to announcement,
                "maintenanceMode" to maintenanceMode,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}

// Extension to emit all from a Flow
private suspend fun <T> kotlinx.coroutines.flow.FlowCollector<T>.emitAll(flow: Flow<T>) {
    flow.collect { value -> emit(value) }
}
