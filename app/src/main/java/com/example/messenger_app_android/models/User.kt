package com.example.messenger_app_android.models



import com.example.messenger_app_android.adapters.Status
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val profilePicture: Int? = null,
    var loggedIn: com.google.firebase.Timestamp? = null,
    var status: Status? = null,
    )