package com.example.messenger_app_android.adapters


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.ocpsoft.prettytime.PrettyTime
import java.sql.Date

private lateinit var user: User
val utilities = Utilities()

interface ChatroomAdapterCallback {
    fun getUsers(userId: String): User?
}

class ChatRoomAdapter(
    private val fragmentManager: FragmentManager? = null,
    private val callback: ChatroomAdapterCallback) :
    ListAdapter<Chatroom, ChatRoomAdapter.ItemViewHolder>(ChatroomDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_vertical_recyclerview, parent, false
            ),
            callback
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View, private val callback: ChatroomAdapterCallback) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatroom: Chatroom) {
            val auth = Firebase.auth

            itemView.apply {
                from_user.text = chatroom.chatroomTitle
                recent_message.text = chatroom.recentMessage

                chatroom.profilePictures?.forEach { entry ->
                    if (entry.key != auth.currentUser?.uid) {
                        Picasso.get()
                            .load(entry.value)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .transform(
                                RoundedTransformationBuilder()
                                    .cornerRadius(50f)
                                    .oval(false)
                                    .build()
                            )
                            .into(chatroom_picture)
                    }
                }

                chatroom.sender?.forEach { entry ->
                    if (entry.key != auth.currentUser?.uid) {
                        sender_textview.text = null
                    } else {
                        if (recent_message.text == "") {
                            sender_textview.text = "Start Chatting!"
                        } else {
                            sender_textview.text = "You:"
                        }
                    }
                }

                chatroom.participants?.find {
                    it != auth.currentUser?.uid
                }?.let {
                    callback.getUsers(it)
                }


                recentMessageElapsedTimeHandler(elapsed_time, chatroom)

                isPostSeen(
                    chatroom,
                    new_post,
                )

                chatroom_picture.setOnClickListener {
                    if (chatroom.chatroomTitle != null) {
                        utilities.loadFragment(
                            ChatRoomFragment().apply {
                                arguments = Bundle().apply {
                                    putString(
                                        StringConstants.CHATROOM_TITLE,
                                        chatroom.chatroomTitle
                                    )
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
) {
    val auth = Firebase.auth
    chatroom.postIsSeen?.forEach { entry ->
        if (entry.key == auth.currentUser?.uid) {
            if (!entry.value) {
                imageView.visibility = View.VISIBLE
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

private fun attachUser(currentUser: User, onlineStatusChatroomAdapter: ImageView) {
    user = currentUser
    isUserOnline(currentUser, onlineStatusChatroomAdapter)
}

private fun isUserOnline(
    user: User,
    imageView: ImageView,
) {
    val timeHandler = Handler(Looper.getMainLooper())
    val tenMinutes: Long = 10 * 60 * 1000
    val loggedIn = user.loggedIn
    timeHandler.post(object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis() - (loggedIn?.seconds?.times(1000) ?: 0)
            if (currentTime < tenMinutes) {
                Status.ONLINE
                imageView.visibility = View.VISIBLE
            } else {
                Status.OFFLINE
                imageView.visibility = View.GONE
            }
            timeHandler.postDelayed(this, tenMinutes)
        }
    })
}







