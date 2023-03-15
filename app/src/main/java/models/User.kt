package models

import com.google.firebase.database.Exclude

data class User(
    val displayName: String? = null,
    val email: String? = null,
    val profilePicture: Int? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "displayName" to displayName,
            "email" to email,
            "profilePicture" to profilePicture
        )
    }
}
