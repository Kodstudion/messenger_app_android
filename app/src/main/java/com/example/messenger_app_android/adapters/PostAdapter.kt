package com.example.messenger_app_android.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_recived_post.view.*
import kotlinx.android.synthetic.main.item_sent_post.view.*

enum class PostType {
    SENT, RECIVED
}

class PostAdapter(var posts: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.BaseViewHolder>() {

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindItem(post: Post)
    }


    inner class SentPostHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindItem(post: Post) {
            itemView.apply {
                sent_post_textview.text = post.body
            }
        }

    }

    inner class ReceivedPostHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindItem(post: Post) {
            itemView.apply {
                recived_post_textview.text = post.body
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        when (viewType) {
            1 -> {
                return SentPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_sent_post, parent, false
                    )
                )
            }
            2 -> {
                return ReceivedPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_recived_post, parent, false
                    )
                )
            }
            else -> {
                return SentPostHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_sent_post, parent, false
                    )
                )
            }
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bindItem(posts[position])

    }

    override fun getItemViewType(position: Int): Int {
        return when (posts[position].messageType) {
            PostType.SENT -> 1
            PostType.RECIVED -> 2
            else -> return 0
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

}





