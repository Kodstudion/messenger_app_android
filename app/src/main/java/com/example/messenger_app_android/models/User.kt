package com.example.messenger_app_android.models



import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    var profilePicture: String? = null,
    var loggedIn: com.google.firebase.Timestamp? = null,
    val deviceToken: String? = null,
    )