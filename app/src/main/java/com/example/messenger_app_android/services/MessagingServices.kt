package com.example.messenger_app_android.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.RemoteInput
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.services.MessagingServices.Companion.token
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.services.models.NotificationData
import com.example.messenger_app_android.services.models.PushNotification
import com.example.messenger_app_android.services.models.utilites.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


val TAG = "!!!"
const val CHANNEL_ID = "messenger_app_android"
private const val KEY_TEXT_REPLY = "key_text_reply"



class MessagingServices : FirebaseMessagingService() {
    companion object {
        var sharedPreferences: SharedPreferences? = null
        var token: String?
            get() {
                return sharedPreferences?.getString(R.string.token.toString(), "")
            }
            set(value) {
                sharedPreferences?.edit()?.putString(R.string.token.toString(), value)?.apply()
            }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.showMessage(
            this,
            message.data["body"] ?: "",
            message.data["fromUser"] ?: "",
            message.data["documentId"] ?: "",
            message.data["chatroomTitle"] ?: "",
            message.data["currentUserToken"] ?: "",
            message.data["otherUserToken"] ?: "",
            message.data["profilePicture"] ?: "",
        )
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(StringConstants.DOCUMENT_ID, message.data["documentId"])
        intent.putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])
        intent.putExtra(StringConstants.FROM_USER, message.data["fromUser"])
        intent.putExtra(StringConstants.CURRENT_USER_TOKEN, message.data["currentUserToken"])
        intent.putExtra(StringConstants.OTHER_USER_TOKEN, message.data["otherUserToken"])
        intent.putExtra(StringConstants.PROFILE_PICTURE, message.data["profilePicture"])

    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }
}

class ReplyBroadcastReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onReceive(context: Context, intent: Intent?) {
        val chatroomTitle = intent?.getStringExtra(StringConstants.CHATROOM_TITLE) ?: ""
        val documentId = intent?.getStringExtra(StringConstants.DOCUMENT_ID) ?: ""
        val currentUserToken = intent?.getStringExtra(StringConstants.CURRENT_USER_TOKEN) ?: ""
        val otherDeviceToken = intent?.getStringExtra(StringConstants.OTHER_USER_TOKEN) ?: ""
        val auth = FirebaseAuth.getInstance()

        val remoteInputResult = getMessageText(intent ?: return)

        CoroutineScope(Dispatchers.IO).launch {
            NotificationHelper.showMessage(
                context,
                remoteInputResult.toString(),
                chatroomTitle,
                documentId,
                chatroomTitle,
                currentUserToken,
                otherDeviceToken,
                auth.currentUser?.photoUrl.toString()
            )
        }

        val timestamp = Timestamp.now()
        val pushNotice = Post(
            auth.currentUser?.uid,
            remoteInputResult.toString(),
            auth.currentUser?.displayName,
            chatroomTitle,
            remoteInputResult.toString(),
            timestamp,
            auth.currentUser?.photoUrl.toString()
        )

        setSentPushNotice(pushNotice, documentId, remoteInputResult.toString())

        sendPush(
            PushNotification(
                NotificationData(
                    chatroomTitle,
                    remoteInputResult.toString(),
                    documentId,
                    chatroomTitle,
                    auth.currentUser?.displayName ?: "",
                    currentUserToken,
                    otherDeviceToken,
                    auth.currentUser?.photoUrl.toString()
                ), ""
            )
        )
    }
}

private fun sendPush(pushNotification: PushNotification) =
    CoroutineScope(Dispatchers.IO).launch {
        try {
            if (pushNotification.data.currentUserToken == token) {
                pushNotification.to = pushNotification.data.otherUserToken

            }
            if (pushNotification.data.otherUserToken == token) {
                pushNotification.to = pushNotification.data.currentUserToken

            }

            val response = RetrofitInstance.api.postNotification(pushNotification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }

        } catch (e: Exception) {
            Log.e(TAG, "sendPush: $e")
        }
    }

private fun setSentPushNotice(post: Post, documentId: String, messageText: CharSequence) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val sent = Post(
        auth.currentUser?.uid,
        messageText.toString(),
        post.fromUser,
        post.toUser,
        post.postBody,
        post.timestamp,
        post.postPicture
    )

    val pushNoticeDocRef =
        db.collection("chatrooms").document(documentId).collection("posts").document()
    pushNoticeDocRef.set(sent).addOnSuccessListener {
        updateRecentMessage(documentId, messageText.toString())
        updatePostIsSeen(documentId)
        updateSender(documentId, messageText.toString())
    }
}

private fun updateRecentMessage(documentId: String, recentMessage: String) {
    val db = FirebaseFirestore.getInstance()
    val recentMessageDocRef = db.collection("chatrooms").document(documentId)
    recentMessageDocRef.get().addOnSuccessListener { document ->
        if (document != null) {
            recentMessageDocRef.update("recentMessage", recentMessage)
        }
    }
}

private fun updateSender(documentId: String, recentMessage: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val senderDocRef = db.collection("chatrooms").document(documentId)
    senderDocRef.get().addOnSuccessListener { document ->
        if (document != null) {
            val sender = document.data?.get("sender") as? HashMap<*, *>
            val keys = sender?.keys
            if (keys != null) {
                for (key in keys) {
                    if (key != auth.currentUser?.uid) {
                        senderDocRef.update("sender", hashMapOf(auth.currentUser?.uid to recentMessage))
                    }
                }
            }
        }
    }
//    chatroom.sender?.forEach { entry ->
//        if (entry.key == auth.currentUser?.uid) {
//            senderDocRef.set(
//                hashMapOf(
//                    "sender" to hashMapOf(entry.key to recentMessage)
//                ), SetOptions.merge()
//            )
//        } else {
//            senderDocRef.update("sender", hashMapOf(auth.currentUser?.uid to recentMessage))
//        }
//    }
}

private fun updatePostIsSeen(documentId: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val postIsSeenDocRef = db.collection("chatrooms").document(documentId)
    postIsSeenDocRef.get().addOnSuccessListener { document ->
        if (document != null) {
            val postIsSeen = document.data?.get("postIsSeen") as? HashMap<*, *>
            val keys = postIsSeen?.keys
            val map = hashMapOf<String, MutableMap<String, Boolean>>(
                "postIsSeen" to mutableMapOf(),
            )

            if (keys != null) {
                for (key in keys) {
                    if (key == auth.currentUser?.uid) {
                        map["postIsSeen"]?.put(key.toString(), true)
                    } else {
                        map["postIsSeen"]?.put(key.toString(), false)
                    }
                }
            }

            postIsSeenDocRef.set(
                map, SetOptions.merge()
            )
        }
    }
}

private fun getMessageText(intent: Intent): CharSequence? {
    val remoteInput = RemoteInput.getResultsFromIntent(intent)
    return remoteInput?.getCharSequence(KEY_TEXT_REPLY)
}


