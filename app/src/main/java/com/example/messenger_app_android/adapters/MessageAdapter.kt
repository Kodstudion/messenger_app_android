package com.example.messenger_app_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import kotlinx.android.synthetic.main.item_vertical_recyclerview.view.*
import com.example.messenger_app_android.models.Message

class MessageAdapter(private val messages: MutableList<Message>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_vertical_recyclerview, parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
       val message = messages[position]
        holder.itemView.apply {
            message.messagePicture?.let { message_picture.setImageResource(it) }
            message_title.text = message.title
            message_recent_message.text = message.recentMessage
        }
    }

    override fun getItemCount(): Int {
       return messages.size
    }

    fun addMessage(message: String) {
        messages.add(Message(message))
        notifyItemInserted(messages.size - 1)
    }

}