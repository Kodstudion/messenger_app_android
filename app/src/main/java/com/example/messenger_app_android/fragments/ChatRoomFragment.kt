package com.example.messenger_app_android.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.adapters.PostType
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.utilities.Utilities
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModel
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat_room.*
import java.sql.Timestamp
import java.util.*

interface ChatroomFragmentChatroomView {
    fun setPost(post: MutableList<Post>)
}

class ChatRoomFragment(var chatroomTitle: String, var documentId: String) : Fragment(),
    ChatroomFragmentChatroomView {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentChatRoomBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var chatroomFragmentViewModel: ChatroomFragmentViewModel


    val TAG = "!!!"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        chatroomFragmentViewModel = ViewModelProvider(
            this,
            ChatroomFragmentViewModelFactory(documentId)
        )[ChatroomFragmentViewModel::class.java]
        chatroomFragmentViewModel.attachChatroom(this)


        binding = FragmentChatRoomBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
        auth = Firebase.auth

        postAdapter = PostAdapter()
        binding.chatConversationRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.chatConversationRecyclerview.adapter = postAdapter

        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager

        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageButton.setOnClickListener {
            val date = Date()
            val timestamp = Timestamp(date.time)
            val addPost = binding.sendMessageEditText.text.toString()
            val post = Post(
                auth.currentUser?.uid,
                addPost,
                auth.currentUser?.displayName,
                chatroomTitle,
                addPost,
                PostType.SENT,
                timestamp
            )
            if (addPost.isNotEmpty()) {
                writePost(post, documentId)
                binding.sendMessageEditText.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onStart() {
        super.onStart()
        binding.toolbarTitleChatroom.text = chatroomTitle
    }

    override fun setPost(post: MutableList<Post>) {
        postAdapter.submitList(post)
    }


    private fun writePost(post: Post, documentId: String) {
        val newPost = Post(
            auth.currentUser?.uid,
            post.postBody,
            post.fromUser,
            post.toUser,
            post.postBody,
            post.postType,
            post.timestamp
        )
        val postDocRef =
            db.collection("chatrooms").document(documentId).collection("posts").document()
        postDocRef.set(newPost).addOnSuccessListener {
            Log.d(TAG, "writePost: Success")
        }
            .addOnFailureListener {
                Log.d(TAG, "writePost: Failed")
            }
    }

}





