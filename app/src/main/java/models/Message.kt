package models

import com.google.firebase.database.Exclude

data class Message(
    val message: String? = null,
    val fromUser: User? = null,
    val title: String? = null,
    val recentMessage: String? = null,
    val messagePicture: Int? = null,

    ) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return  mapOf(
            "message" to message,
            "fromUser" to fromUser,
            "title" to title,
            "recentMessage" to recentMessage,
            "messagePicture" to message
        )
    }
}