package com.example.messenger_app_android.adapters

import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.models.Post
import kotlinx.android.synthetic.main.item_sent_post.view.*

enum class PostType {
    SENT, RECIVED
}

class PostAdapter(private val posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
//        when (viewType) {
//            1 -> {
//                return MessageViewHolder(
//                    LayoutInflater.from(parent.context).inflate(
//                        R.layout.item_sent_message, parent, false
//                    )
//                )
//            }
//            2 -> {
//                return MessageViewHolder(
//                    LayoutInflater.from(parent.context).inflate(
//                        R.layout.item_recived_message, parent, false
//                    )
//                )
//            }
//            else -> {
//                return MessageViewHolder(
//                    LayoutInflater.from(parent.context).inflate(
//                        R.layout.item_sent_message, parent, false
//                    )
//                )
//            }
//        }
        return PostViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_sent_post, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.itemView.apply {
//            recived_message_textview.text = message.body
            sent_post_textview.text = post.body

        }
    }

//    override fun getItemViewType(position: Int): Int {
//        return when (messages[position].messageType) {
//            MessageType.SENT -> 1
//            MessageType.RECIVED -> 2
//            else -> throw AssertionError()
//        }
//    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun addPost(postBody: Post) {
        val TAG = "!!!"
        posts.add(Post(null,postBody.body))
        notifyItemInserted(posts.size - 1)

        Log.d(TAG, "addPost: ${postBody.body}")
    }

}

