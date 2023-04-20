package com.example.messenger_app_android.services.models.utilites

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

object NotificationHelper {

    private val messages = mutableListOf<NotificationCompat.MessagingStyle.Message>()

    fun showMessage(context: Context, message: String, from: String, chatroomId: String) {
        val notificationMessage = NotificationCompat.MessagingStyle.Message(
            message,
            System.currentTimeMillis(),
            from
        )
        messages.add(notificationMessage)

        showNotification(context, chatroomId)
    }

    private fun showNotification(context: Context, chatroomId: String) {
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
            .setContentIntent(getPendingIntent(context, chatroomId))
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_baseline_email_24,
                    "Reply",
                    getReplyPendingIntent(context, chatroomId)

                )
                    .addRemoteInput(remoteInput)
                    .build()
            )
            .setStyle(messageStyle)
            .setContentTitle("Messenger")
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getReplyPendingIntent(context: Context, documentId: String): PendingIntent? {
        val replyReceiver = Intent(context, ReplyBroadcastReceiver::class.java).apply {
            action = "Reply action"
            //putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])
            putExtra(StringConstants.DOCUMENT_ID, documentId)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            replyReceiver,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun getPendingIntent(context: Context, documentId: String): PendingIntent? {
        val intent = Intent(context, HomeActivity::class.java)
        intent.putExtra(StringConstants.DOCUMENT_ID, documentId)
//        intent.putExtra(StringConstants.CHATROOM_TITLE, message.data["chatroomTitle"])
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