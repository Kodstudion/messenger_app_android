package com.example.messenger_app_android.models


import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.firestore.DocumentId

@IgnoreExtraProperties
data class Chatroom(
    @DocumentId var documentId: String = "",
    var participants: MutableList<String>? = null,
    var recentMessage: String? = null,
    var chatroomTitle: String? = null,
    val chatroomPicture: String? = null,
    var participantsNames: HashMap<String, String>? = null,
    var lastUpdated: com.google.firebase.Timestamp? = null,
    var sender: HashMap<String, String>? = null,
    var postIsSeen: HashMap<String, Boolean>? = null,
    val deviceTokens: HashMap<String, String>? = null,
    var typing: HashMap<String, Boolean>? = null,
    val profilePictures: HashMap<String, String>? = null,
)