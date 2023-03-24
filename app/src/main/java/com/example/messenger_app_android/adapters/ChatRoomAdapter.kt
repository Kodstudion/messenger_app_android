package com.example.messenger_app_android.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import com.example.messenger_app_android.utilities.Utilities


val utilities = Utilities()

class ChatRoomAdapter(
    val chatrooms: MutableList<Chatroom>,
    private val fragmentManager: FragmentManager? = null,
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
        val chatroom = chatrooms[position]
        holder.itemView.apply {
            from_user.text = chatroom.toUser
            recent_message.text = chatroom.text
            chatroom.chatroomPicture?.let { message_picture.setImageResource(it) }

            message_picture.setOnClickListener {
                utilities.loadFragment(ChatRoomFragment(chatroom.toUser.toString(),""),fragmentManager)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatrooms.size
    }

}

