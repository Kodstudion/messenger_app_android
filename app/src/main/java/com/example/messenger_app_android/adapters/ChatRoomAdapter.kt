package com.example.messenger_app_android.adapters


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import org.ocpsoft.prettytime.PrettyTime
import java.sql.Date


val utilities = Utilities()

class ChatRoomAdapter(private val fragmentManager: FragmentManager? = null) :
    ListAdapter<Chatroom, ChatRoomAdapter.ItemViewHolder>(ChatroomDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_vertical_recyclerview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatroom: Chatroom) {
            val auth = Firebase.auth

            itemView.apply {
                from_user.text = chatroom.chatroomTitle
                recent_message.text = chatroom.recentMessage
                sender_textview.text = chatroom.sender

                chatroom.profilePictures?.forEach {entry ->
                    if (entry.key != auth.currentUser?.uid) {
                        Picasso.get()
                            .load(entry.value)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .transform(
                                RoundedTransformationBuilder()
                                    .cornerRadius(50f)
                                    .oval(false)
                                    .build())
                            .into(chatroom_picture)
                    }
                }

                if (sender_textview.text == "") {
                    sender_textview.visibility = View.GONE
                    val layoutParams = recent_message.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = 50
                    recent_message.layoutParams = layoutParams

                } else {
                    sender_textview.visibility = View.VISIBLE
                }

                recentMessageElapsedTimeHandler(elapsed_time, chatroom)

                isPostSeen(
                    chatroom,
                    new_post,
                    R.drawable.ic_baseline_chat_bubble_24,
                )

                chatroom_picture.setOnClickListener {
                    if (chatroom.chatroomTitle != null) {
                        utilities.loadFragment(
                            ChatRoomFragment().apply {
                                arguments = Bundle().apply {
                                    putString(StringConstants.CHATROOM_TITLE, chatroom.chatroomTitle)
                                    putString(StringConstants.DOCUMENT_ID, chatroom.documentId)
                                    chatroom.profilePictures?.forEach { entry ->
                                        if (entry.key != auth.currentUser?.uid) {
                                            putString(StringConstants.CHATROOM_PICTURE, entry.value)
                                        }
                                    }
                                }
                            },
                            fragmentManager
                        )
                    }
                    updatePostIsSeen(chatroom)
                }
            }
        }
    }
}

class ChatroomDiffCallBack : DiffUtil.ItemCallback<Chatroom>() {
    override fun areItemsTheSame(oldItem: Chatroom, newItem: Chatroom): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Chatroom, newItem: Chatroom): Boolean {
        return oldItem.chatroomTitle == newItem.chatroomTitle
    }
}

private fun recentMessageElapsedTimeHandler(elapsedTimeTextView: TextView, chatroom: Chatroom) {
    val timeHandler = Handler(Looper.getMainLooper())
    val minute: Long = 60 * 1000
    val lastUpdated = chatroom.lastUpdated ?: return
    timeHandler.post(object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis() - lastUpdated.seconds * 1000
            updateElapsedTimeText(elapsedTimeTextView, currentTime)
            timeHandler.postDelayed(this, minute)
        }
    })
}

private fun updateElapsedTimeText(
    elapsedTimeTextView: TextView,
    currentTime: Long,
) {
    val elapsedTime = PrettyTime().format(Date(System.currentTimeMillis() - currentTime))
    elapsedTimeTextView.text = elapsedTime
}

private fun isPostSeen(
    chatroom: Chatroom,
    imageView: ImageView,
    imageResIsSeen: Int,
) {
    val auth = Firebase.auth
    chatroom.postIsSeen?.forEach { entry ->
        if (entry.key == auth.currentUser?.uid) {
            if (!entry.value) {
                imageView.setImageResource(imageResIsSeen)
            } else {
                imageView.visibility = View.GONE
            }
        }
    }
}

private fun updatePostIsSeen(chatroom: Chatroom) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    chatroom.postIsSeen?.forEach { entry ->
        if (entry.key == auth.currentUser?.uid) {
            val postIsSeenDocRef = db.collection("chatrooms").document(chatroom.documentId)
            postIsSeenDocRef.set(
                hashMapOf(
                    "postIsSeen" to hashMapOf(
                        entry.key to true
                    )
                ), SetOptions.merge()
            )
        }
    }
}







