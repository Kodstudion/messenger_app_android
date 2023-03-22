package com.example.messenger_app_android.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatRoomFragment(var title: String) : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatRoomBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val TAG = "!!!"


        db = Firebase.firestore
        auth = Firebase.auth

        postAdapter = PostAdapter(mutableListOf())
        binding.chatConversationRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.chatConversationRecyclerview.adapter = postAdapter

        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager

        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageButton.setOnClickListener {
            val addPost = binding.sendMessageEditText.text.toString()
            val post = Post(null, addPost)
            binding.sendMessageEditText.text.clear()
            //writeNewPost(post, auth.currentUser?.displayName.toString())
        }

        createChatroom(Chatroom("", mutableListOf(auth.currentUser?.displayName.toString(), title), "", null,auth.currentUser?.displayName, title))

    }


    private fun createChatroom(chatroom: Chatroom) {
        db.collection("chatrooms").add(chatroom.toMap()).addOnSuccessListener { result ->
            Log.d("!!!", "DocumentSnapshot added with ID: ${result}")
        }


    }


    override fun onStart() {
        super.onStart()
        binding.toolbarTitleChatroom.text = title
    }


}





