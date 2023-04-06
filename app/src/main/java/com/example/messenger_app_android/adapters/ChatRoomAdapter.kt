package com.example.messenger_app_android.adapters


import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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

                recentMessageElapsedTimeHandler(elapsed_time, chatroom)
                chatroom.chatroomPicture?.let { chatroom_picture.setImageResource(it) }

                chatroom_picture.setOnClickListener {
                    if (chatroom.nameOfChat != null) {
                        utilities.loadFragment(
                            ChatRoomFragment(chatroom.nameOfChat ?: "", chatroom.documentId),
                            fragmentManager
                        )
                    }
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

private var lastFetchedTimestamp: Timestamp? = null
private fun getLastUpdatedTime(chatroom: Chatroom): Timestamp? {
    val db = FirebaseFirestore.getInstance()
    val chatroomRef = db.collection("chatrooms").document(chatroom.documentId)
    if (lastFetchedTimestamp != null && chatroom.lastUpdated == lastFetchedTimestamp) {
        return lastFetchedTimestamp
    }
    chatroomRef.get().addOnSuccessListener { document ->
        if (document != null) {
            val chatroomLastUpdated = document.toObject<Chatroom>()
            lastFetchedTimestamp = chatroomLastUpdated?.lastUpdated
        }
    }
    return chatroom.lastUpdated
}

private fun recentMessageElapsedTimeHandler(elapsedTimeTextView: TextView, chatroom: Chatroom) {
    val timeHandler = Handler(Looper.getMainLooper())
    val minute: Long = 60 * 1000
    val lastUpdated = getLastUpdatedTime(chatroom) ?: return
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





