package com.example.messenger_app_android.models

import com.example.messenger_app_android.adapters.PostType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class Post(
    val userId: String? = null,
    var postBody: String? = null,
    val fromUser: String? = null,
    val toUser: String? = null,
    val recentMessage: String? = null,
    val timestamp: com.google.firebase.Timestamp? = null,
    ) {

    fun getMessageType(): PostType {
        val auth = FirebaseAuth.getInstance()
        return if (userId == auth.currentUser?.uid) {
            PostType.SENT
        } else {
            PostType.RECEIVED
        }
    }


}

