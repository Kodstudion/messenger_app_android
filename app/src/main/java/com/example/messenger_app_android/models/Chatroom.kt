package com.example.messenger_app_android.models


import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.DocumentId

@IgnoreExtraProperties
data class Chatroom(
    @DocumentId var documentId: String = "",
    var participants: MutableList<String>? = null,
    var recentMessage: String? = null,
    var nameOfChat: String? = null,
    val chatroomPicture: Int? = null,
    var participantsNames: HashMap<String, String>? = null,
)