package com.example.messenger_app_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.utilities.Utilities

val utilities = Utilities()

class ChatRoomAdapter(
    private val messages: MutableList<Post>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ChatRoomAdapter.MessageViewHolder>() {
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_vertical_recyclerview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val TAG = "!!!"
        val message = messages[position]
        holder.itemView.apply {
            message_displayname.text = message.displayName
            message_recent_message.text = message.recentMessage

            message.messagePicture?.let {
                message_picture.setImageResource(it) }
            message_picture.setOnClickListener {
                utilities.loadFragment(ChatRoomFragment(message.displayName.toString()),fragmentManager)
            }

          
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun addMessage(displayName: String, recentMessage: String) {
        messages.add(Post(null,null,displayName,null,null,recentMessage, null, PostType.SENT))
        notifyItemInserted(messages.size - 1)
    }

}

