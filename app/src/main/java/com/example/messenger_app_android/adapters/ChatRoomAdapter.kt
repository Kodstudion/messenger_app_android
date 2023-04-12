package com.example.messenger_app_android.adapters


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
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
            itemView.apply {
                from_user.text = chatroom.nameOfChat
                recent_message.text = chatroom.recentMessage
                sender_textview.text = chatroom.sender

                if (sender_textview.text == "") {
                    sender_textview.visibility = View.GONE
                    val layoutParams = recent_message.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.marginStart = 50
                    recent_message.layoutParams = layoutParams

                } else {
                    sender_textview.visibility = View.VISIBLE
                }

                recentMessageElapsedTimeHandler(elapsed_time, chatroom)
                chatroom.chatroomPicture?.let { chatroom_picture.setImageResource(it) }

                circleColor(
                    chatroom,
                    chatroom_picture,
                    R.drawable.round_green_circle,
                    R.drawable.round_blue_circle
                )

                chatroom_picture.setOnClickListener {
                    if (chatroom.nameOfChat != null) {
                        utilities.loadFragment(
                            ChatRoomFragment(chatroom.nameOfChat ?: "", chatroom.documentId),
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
        return oldItem.nameOfChat == newItem.nameOfChat
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

private fun circleColor(
    chatroom: Chatroom,
    imageView: ImageView,
    imageResIsSeen: Int,
    imageResIsNotSeen: Int
) {
    chatroom.postIsSeen?.forEach { entry ->
        if (entry.key == Firebase.auth.currentUser?.uid) {
            if (!entry.value) {
                imageView.setImageResource(imageResIsSeen)
            } else {
                imageView.setImageResource(imageResIsNotSeen)
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
                        entry.key to true)
                ), SetOptions.merge()
            )
        }
    }
}







