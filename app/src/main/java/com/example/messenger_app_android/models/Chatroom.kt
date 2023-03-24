package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.DocumentId

@IgnoreExtraProperties
data class Chatroom(
    @DocumentId var documentId: String = "",
    var participants: MutableList<String>? = null,
    val text: String? = null,
    val timestamp: Long? = null,
    var fromUser: String? = null,
    var toUser: String? = null,
    val chatroomPicture: Int? = null,
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return  mapOf(
            "title" to documentId,
            "participants" to participants,
            "text" to text,
            "timestamp" to timestamp,
            "fromUser" to fromUser,
            "toUser" to toUser,
            "profilePicture" to chatroomPicture

        )
    }
}