package com.example.messenger_app_android.services.models.utilites

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.HomeActivity
import com.example.messenger_app_android.services.CHANNEL_ID
import com.example.messenger_app_android.services.ReplyBroadcastReceiver
import com.example.messenger_app_android.services.constants.StringConstants
import java.util.*

const val NOTIFICATION_ID = 123
const val KEY_TEXT_REPLY = "key_text_reply"

val TAG = "!!!"

object NotificationHelper {

    private val messages = mutableListOf<NotificationCompat.MessagingStyle.Message>()

    fun showMessage(
        context: Context,
        message: String,
        fromUser: String,
        documentId: String,
        chatroomTitle: String,
        currentUserToken: String,
        otherDeviceToken: String,
    ) {
        val notificationMessage = NotificationCompat.MessagingStyle.Message(
            message,
            System.currentTimeMillis(),
            fromUser

        )
        messages.add(notificationMessage)

        showNotification(context, documentId, chatroomTitle, currentUserToken, otherDeviceToken)
    }

    private fun showNotification(
        context: Context,
        documentId: String,
        chatroomTitle: String,
        currentUserToken: String,
        otherDeviceToken: String,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val messageStyle = NotificationCompat.MessagingStyle("Me")
        messages.forEach {
            messageStyle.addMessage(it)
        }

        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_email_24)
            .setAutoCancel(true)
            .setContentIntent(
                getPendingIntent(
                    context,
                    documentId,
                    chatroomTitle,
                    currentUserToken,
                    otherDeviceToken
                )
            )
            .addAction(
                getReplyPendingIntent(
                    context,
                    documentId,
                    chatroomTitle,
                    currentUserToken,
                    otherDeviceToken
                )
                    .addRemoteInput(remoteInput)
                    .build()
            )
            .setStyle(messageStyle)
            .setContentTitle("Messenger")
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getReplyPendingIntent(
        context: Context,
        documentId: String,
        chatroomTitle: String,
        currentUserToken: String,
        otherDeviceToken: String
    ): NotificationCompat.Action.Builder {
        val TAG = "!!!"
        val replyReceiver = Intent(context, ReplyBroadcastReceiver::class.java).apply {
            action = "Reply action"

            putExtra(StringConstants.DOCUMENT_ID, documentId)
            putExtra(StringConstants.CHATROOM_TITLE, chatroomTitle)
            putExtra(StringConstants.CURRENT_USER_TOKEN, currentUserToken)
            putExtra(StringConstants.OTHER_USER_TOKEN, otherDeviceToken)

            Log.d(TAG, "getReplyPendingIntent: $currentUserToken")
        }
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            replyReceiver,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_baseline_email_24,
            "Reply",
            replyPendingIntent,
        )
    }

    private fun getPendingIntent(
        context: Context,
        documentId: String,
        chatroomTitle: String,
        currentUserToken: String,
        otherDeviceToken: String
    ): PendingIntent? {
        val intent = Intent(context, HomeActivity::class.java)
        intent.putExtra(StringConstants.DOCUMENT_ID, documentId)
        intent.putExtra(StringConstants.CHATROOM_TITLE, chatroomTitle)
        intent.putExtra(StringConstants.OTHER_USER_TOKEN, otherDeviceToken)
//        intent.putExtra("otherParticipantDeviceToken", otherParticipantDeviceToken)
        Log.d(TAG, "PendingIntent: $currentUserToken")
//        intent.putExtra(StringConstants.FROM_USER, message.data["fromUser"])
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )
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