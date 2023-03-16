package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude

data class Message(
    val userID: String? = null,
    val body: String? = null,
    val displayName: String? = null,
    val fromUser: User? = null,
    val title: String? = null,
    val recentMessage: String? = null,
    val messagePicture: Int? = null,

    ) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return  mapOf(
            "userID" to userID,
            "body" to body,
            "displayName" to displayName,
            "fromUser" to fromUser,
            "title" to title,
            "recentMessage" to recentMessage,
            "messagePicture" to messagePicture
        )
    }
}