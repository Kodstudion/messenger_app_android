package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude

data class Chatroom(
    val userOne: String = "",
    val userTwo: String = "",
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return  mapOf(
           "userOne" to  userOne,
            "userTwo" to userTwo,
        )
    }
}