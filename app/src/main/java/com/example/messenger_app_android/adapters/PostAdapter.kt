package com.example.messenger_app_android.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.models.Post
import kotlinx.android.synthetic.main.item_recived_post.view.*
import kotlinx.android.synthetic.main.item_sent_post.view.*

enum class PostType {
    SENT, RECEIVED
}

class PostAdapter() : ListAdapter<Post, RecyclerView.ViewHolder> (PostDiffCallback()) {

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindItem(post: Post)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                SentPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_sent_post, parent, false
                    )
                )
            }
            2 -> {
                ReceivedPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_recived_post, parent, false
                    )
                )
            }
            else -> {
                SentPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_sent_post, parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder).bindItem(getItem(position))

    }


    inner class SentPostHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindItem(post: Post) {
            itemView.apply {
                sent_post_textview.text = post.postBody
            }
        }

    }

    inner class ReceivedPostHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindItem(post: Post) {
            itemView.apply {
                received_post_textview.text = post.postBody
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).getMessageType()) {
            PostType.SENT -> 1
            PostType.RECEIVED -> 2
        }
    }
}

class PostDiffCallback: DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.postBody == newItem.postBody
    }

}





