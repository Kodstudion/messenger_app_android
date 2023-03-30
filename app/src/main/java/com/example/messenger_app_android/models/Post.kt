package com.example.messenger_app_android.models

import com.example.messenger_app_android.adapters.PostType


data class Post(
    val userId: String? = null,
    var postBody: String? = null,
    val fromUser: String? = null,
    val toUser: String? = null,
    val recentMessage: String? = null,
    val postType: PostType? = null,
//    val timestamp: java.util.Date? = null,
    val timestamp: com.google.firebase.Timestamp? = null,

    )
