package com.example.messenger_app_android.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import com.example.messenger_app_android.utilities.Utilities


val utilities = Utilities()

class ChatRoomAdapter(private val fragmentManager: FragmentManager? = null) : ListAdapter<Chatroom, ChatRoomAdapter.ItemViewHolder>(ChatroomDiffCallBack()) {
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
                from_user.text = chatroom.chatroomTitle
                recent_message.text = chatroom.recentMessage
                chatroom.chatroomPicture?.let { message_picture.setImageResource(it) }

                message_picture.setOnClickListener {
                    if (chatroom.chatroomTitle != null) {
                        utilities.loadFragment(
                            ChatRoomFragment(chatroom.chatroomTitle ?: "", chatroom.documentId),
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
        return oldItem.chatroomTitle == newItem.chatroomTitle
    }

}


