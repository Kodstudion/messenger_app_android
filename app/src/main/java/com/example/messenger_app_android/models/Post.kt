package com.example.messenger_app_android.models

import com.example.messenger_app_android.adapters.PostType
import com.google.firebase.database.Exclude

data class Post(
    val userID: String? = null,
    var body: String? = null,
    val displayName: String? = null,
    val fromUser: String? = null,
    val title: String? = null,
    val recentMessage: String? = null,
    val messagePicture: Int? = null,
    val messageType: PostType? = null

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