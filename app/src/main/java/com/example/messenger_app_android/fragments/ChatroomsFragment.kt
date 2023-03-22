package com.example.messenger_app_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.databinding.FragmentChatroomsBinding
import com.example.messenger_app_android.databinding.ItemChatroomBinding
import com.example.messenger_app_android.models.Chatroom
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatroomsFragment : Fragment() {

    private lateinit var binding: FragmentChatroomsBinding
    private val adapter = ChatroomAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatroomsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        Firebase.firestore.collection("chatrooms").document(Firebase.auth.uid!!).collection("chatrooms").get().addOnSuccessListener {
            val chatrooms = it.documents.map { snapshot ->
                val room = snapshot.toObject(NewChatRoom::class.java)!!
                room.id = snapshot.id
                room
            }

            adapter.submitList(chatrooms)
        }
    }
}

data class NewChatRoom(
    val participants: MutableList<String> = mutableListOf(),
    val messages: MutableList<String> = mutableListOf(),
) {
    var id: String? = null
}

val ChatroomDiffCallback = object : DiffUtil.ItemCallback<NewChatRoom>() {
    override fun areItemsTheSame(oldItem: NewChatRoom, newItem: NewChatRoom) = oldItem == newItem

    override fun areContentsTheSame(oldItem: NewChatRoom, newItem: NewChatRoom) = oldItem == newItem
}

class ChatroomAdapter :
    ListAdapter<NewChatRoom, ChatroomAdapter.ChatroomViewHolder>(ChatroomDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomViewHolder {
        return ChatroomViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ChatroomViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ChatroomViewHolder private constructor(private val binding: ItemChatroomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NewChatRoom) {
            binding.from.text = item.participants[0]
            binding.to.text = item.participants[1]
        }

        companion object {
            fun from(parent: ViewGroup): ChatroomViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChatroomBinding.inflate(layoutInflater, parent, false)
                return ChatroomViewHolder(binding)
            }
        }
    }
}