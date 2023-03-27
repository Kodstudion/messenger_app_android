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

//class PostAdapter : ListAdapter<Post, RecyclerView.ViewHolder>(DiffCallback()) {
//
//    class SentPostHolder(private val binding: ItemSentPostBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(post: Post) {
//            binding.apply {
//                sentPostTextview.text = post.postBody
//            }
//        }
//    }
//
//    class ReceivedPostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun bind(post: Post) {
//            itemView.apply {
//                recived_post_textview.text = post.postBody
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            1 -> {
//                SentPostHolder(
//                    ItemSentPostBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    )
//                )
//            }
//            2 -> {
//                ReceivedPostHolder(
//                    LayoutInflater.from(parent.context).inflate(
//                        R.layout.item_recived_post,
//                        parent,
//                        false
//                    )
//                )
//            }
//            else -> {
//                SentPostHolder(
//                    ItemSentPostBinding.inflate(
//                        LayoutInflater.from(parent.context),
//                        parent,
//                        false
//                    )
//                )
//            }
//        }
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val post = getItem(position)
//        return when (getItem(position).postType) {
//            PostType.SENT -> {
//                (holder as SentPostHolder).bind(post)
//            }
//            PostType.RECIVED -> {
//                (holder as ReceivedPostHolder).bind(post)
//            }
//            else -> {
//                (holder as SentPostHolder).bind(post)
//            }
//        }
//    }
//}
//
//
//
//
//class DiffCallback : DiffUtil.ItemCallback<Post>() {
//    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
//        return oldItem == newItem
//    }
//
//    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
//       return oldItem.postBody == newItem.postBody
//    }
//
//
//}

class PostAdapter() : ListAdapter<Post, RecyclerView.ViewHolder> (DiffCallback()) {

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
                recived_post_textview.text = post.postBody
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).postType) {
            PostType.SENT -> 1
            PostType.RECEIVED -> 2
            else -> 1
        }
    }

}

class DiffCallback: DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.postBody == newItem.postBody
    }

}





