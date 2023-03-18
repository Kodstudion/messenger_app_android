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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class ChatRoomFragment(var title: String) : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


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

        database = Firebase.database.reference
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
            writeNewPost(post, auth.currentUser?.displayName.toString())
        }


        val postListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val body = snapshot.getValue<Post>()?.body ?: return
                val post = Post(null,body)
                postAdapter.posts.add(post)
                postAdapter.notifyItemInserted(postAdapter.posts.size - 1)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", error.toException())
            }
        }
        database.child("users").child(auth.uid.toString()).child("message").addChildEventListener(postListener)

    }

    private fun writeNewPost(postBody: Post, displayName: String) {
       val newPostRef = database.child("users").child(auth.uid.toString()).child("message").push()

        val newPost = Post(null, postBody.body, displayName)
        val postValues = newPost.toMap()

        newPostRef.setValue(postValues)
    }

    private fun createChatroom(firstUser: String, secondUser: String) {
        val chatroomRef =
            database.child("users").child(auth.uid.toString()).child("chatroom").push()

        val newChatroom = Chatroom(firstUser, secondUser)
        val chatValues = newChatroom.toMap()

        chatroomRef.setValue(chatValues)

    }




    override fun onStart() {
        createChatroom(auth.currentUser?.displayName.toString(), title)
        super.onStart()
        binding.toolbarTitleChatroom.text = title
    }
}



//private fun writePost(postBody: Post) {
//    postAdapter.posts.add(post)
//    database.child("users").child(auth.uid.toString()).child("message").setValue(post)
//
//
//    postAdapter.notifyItemInserted(postAdapter.posts.size - 1)

