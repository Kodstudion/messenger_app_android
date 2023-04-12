package com.example.messenger_app_android.models


import com.google.firebase.Timestamp
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val profilePicture: Int? = null,
//    val timestamp: Timestamp? = null,
//    var online: Boolean = false,
    )