package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude

data class User(
    var uid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val profilePicture: Int? = null,
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to uid,
            "displayName" to displayName,
            "email" to email,
            "profilePicture" to profilePicture
        )
    }
}
