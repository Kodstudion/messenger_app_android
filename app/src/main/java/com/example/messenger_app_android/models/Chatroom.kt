package com.example.messenger_app_android.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.DocumentId

@IgnoreExtraProperties
data class Chatroom(
    @DocumentId var documentId: String = "",
    var participants: MutableList<String>? = null,
    val recentMessage: String? = null,
    var chatroomTitle: String? = null,
    val chatroomPicture: Int? = null,
)