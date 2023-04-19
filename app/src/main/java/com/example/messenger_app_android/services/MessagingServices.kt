package com.example.messenger_app_android.services

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.app.RemoteInput
import com.example.messenger_app_android.adapters.PostType
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.services.constants.StringConstants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(StringConstants.DOCUMENT_ID, message.data["documentId"])
        intent.putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = UUID.randomUUID().hashCode()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )

        val replyLabel = "Reply"
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }

        val replyReceiver = Intent(this, ReplyBroadcastReceiver::class.java).apply {
            action = "Reply action"
            putExtra(StringConstants.NOTICICATION_ID, notificationID)
            putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])
            putExtra(StringConstants.DOCUMENT_ID, message.data["documentId"])
        }

        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            replyReceiver,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_email_24,
            "Reply",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.ic_baseline_email_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(action)
            .build()

        notificationManager.notify(1234, notification)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        token = newToken
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "ChannelName"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel Description"
            enableLights(true)
            lightColor = R.color.purple_200
        }
        notificationManager.createNotificationChannel(channel)
    }
}


class ReplyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(StringConstants.NOTICICATION_ID, 0)
        val chatroomTitle = intent?.getStringExtra(StringConstants.CHATROOM_TITLE)
        val documentId = intent?.getStringExtra(StringConstants.DOCUMENT_ID)
        val notificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val repliedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_email_24)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).apply {

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(notificationId ?: return, repliedNotification)



            val auth = FirebaseAuth.getInstance()
            val messageText = getMessageText(intent)
            val timestamp = Timestamp.now()
            val pushNotice = Post(
                auth.currentUser?.uid,
                messageText.toString(),
                auth.currentUser?.displayName,
                chatroomTitle,
                messageText.toString(),
                PostType.SENT,
                timestamp
            )
            setSentPushNotice(pushNotice, documentId ?: return, messageText ?: return)
            notificationManager.cancelAll()
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
            PostType.SENT,
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
}
