package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude

data class Chatroom(
   var participants: MutableList<User>? = mutableListOf(),
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return  mapOf(
            "participants" to participants,
        )
    }
}