package com.example.messenger_app_android.services.models

data class NotificationData(
    val title: String,
    val body: String,
    val documentId: String,
    val chatroomTitle: String,
    val fromUser: String,
    val otherParticipantDeviceToken: String,
)