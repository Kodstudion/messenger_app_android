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
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.services.MessagingServices.Companion.token
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.services.models.NotificationData
import com.example.messenger_app_android.services.models.PushNotification
import com.example.messenger_app_android.services.models.utilites.NotificationHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NotificationHelper.showMessage(
            this,
            message.data["body"] ?: "",
            message.data["fromUser"] ?: "",
            message.data["documentId"] ?: "",
            message.data["chatroomTitle"] ?: "",
            message.data["currentUserToken"] ?: "",
            message.data["otherUserToken"] ?: ""
        )
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(StringConstants.DOCUMENT_ID, message.data["documentId"])
        intent.putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])
        intent.putExtra(StringConstants.FROM_USER, message.data["fromUser"])
        intent.putExtra(StringConstants.CURRENT_USER_TOKEN, message.data["currentUserToken"])
        intent.putExtra(StringConstants.OTHER_USER_TOKEN, message.data["otherUserToken"])

        Log.d(TAG, "onMessageReceived: currentUserToken: ${message.data["currentUserToken"]} ")
        Log.d(TAG, "onMessageReceived: otherUserToken: ${message.data["otherUserToken"]}")

    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }
}

class ReplyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val chatroomTitle = intent?.getStringExtra(StringConstants.CHATROOM_TITLE) ?: ""
        val documentId = intent?.getStringExtra(StringConstants.DOCUMENT_ID) ?: ""
        val currentUserToken = intent?.getStringExtra(StringConstants.CURRENT_USER_TOKEN) ?: ""
        val otherDeviceToken = intent?.getStringExtra(StringConstants.OTHER_USER_TOKEN) ?: ""
        val auth = FirebaseAuth.getInstance()

        val remoteInputResult = getMessageText(intent ?: return)

        NotificationHelper.showMessage(
            context,
            remoteInputResult.toString(),
            chatroomTitle,
            documentId,
            chatroomTitle,
            currentUserToken,
            otherDeviceToken
        )

        val timestamp = Timestamp.now()
        val pushNotice = Post(
            auth.currentUser?.uid,
            remoteInputResult.toString(),
            auth.currentUser?.displayName,
            chatroomTitle,
            remoteInputResult.toString(),
            timestamp
        )
        setSentPushNotice(pushNotice, documentId ?: return, remoteInputResult.toString())

        sendPush(
            PushNotification(
                NotificationData(
                    chatroomTitle,
                    remoteInputResult.toString(),
                    documentId,
                    chatroomTitle,
                    auth.currentUser?.displayName ?: "",
                    currentUserToken,
                    otherDeviceToken
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
                Log.d(TAG, "sendPush: ${pushNotification.data.otherUserToken}")

            }
            if (pushNotification.data.otherUserToken == token) {
                pushNotification.to = pushNotification.data.currentUserToken
                Log.d(TAG, "sendPush: ${pushNotification.data.currentUserToken}")

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
        post.timestamp
    )

    val pushNoticeDocRef =
        db.collection("chatrooms").document(documentId).collection("posts").document()
    pushNoticeDocRef.set(sent)
}

private fun getMessageText(intent: Intent): CharSequence? {
    val remoteInput = RemoteInput.getResultsFromIntent(intent)
    return remoteInput?.getCharSequence(KEY_TEXT_REPLY)
}


