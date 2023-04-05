package com.example.messenger_app_android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.adapters.PostType
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.utilities.Utilities
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModel
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.ocpsoft.prettytime.PrettyTime
import java.util.TimerTask


interface ChatroomFragmentChatroomView {
    fun setPost(post: MutableList<Post>)
}

class ChatRoomFragment(private var chatroomTitle: String, var documentId: String) : Fragment(),
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
        binding = FragmentChatRoomBinding.inflate(layoutInflater, container, false)
        chatroomFragmentViewModel = ViewModelProvider(
            this,
            ChatroomFragmentViewModelFactory(documentId)
        )[ChatroomFragmentViewModel::class.java]
        chatroomFragmentViewModel.attachChatroom(this)

        postAdapter = PostAdapter()
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        layoutManager.stackFromEnd = true
        binding.chatConversationListAdapter.layoutManager = layoutManager

        binding.chatConversationListAdapter.adapter = postAdapter

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
        auth = Firebase.auth

        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager

        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageButton.setOnClickListener {
            val timestamp = Timestamp.now()
           // val chatroom = Chatroom(timestamp.toString())
            val postBody = binding.sendMessageEditText.text.toString()
            val post = Post(
                auth.currentUser?.uid,
                postBody,
                auth.currentUser?.displayName,
                chatroomTitle,
                postBody,
                PostType.SENT,
                timestamp
            )
            if (postBody.isNotEmpty()) {
                sendAndReceivePost(post, documentId)
                updateChatroomLastUpdate(timestamp)
                binding.sendMessageEditText.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
            chatroomFragmentViewModel.updateResentMessageText(postBody)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.toolbarTitleChatroom.text = chatroomTitle
    }

    override fun setPost(post: MutableList<Post>) {
        postAdapter.submitList(post)
        binding.chatConversationListAdapter.scrollToPosition(post.size - 1)

    }

    private fun sendAndReceivePost(post: Post, documentId: String) {
        val sent = Post(
            auth.currentUser?.uid,
            post.postBody,
            post.fromUser,
            post.toUser,
            post.postBody,
            postType = PostType.SENT,
            post.timestamp
        )
        val postDocRef =
            db.collection("chatrooms").document(documentId).collection("posts").document()
        postDocRef.set(sent).addOnSuccessListener {
            setReceivedPost(post, documentId)
        }
            .addOnFailureListener {
                Log.d(TAG, "writePost: Failed")
            }
    }
    
    private fun updateChatroomLastUpdate(timestamp: Timestamp) {
       val lastUpdatedDocRef =  db.collection("chatrooms").document(documentId)
       lastUpdatedDocRef.get().addOnSuccessListener { document ->
           if (document != null) {
               lastUpdatedDocRef.update("lastUpdated", timestamp)
              } else {
                Log.d(TAG, "No such document")
           }
       }
    }

    private fun setReceivedPost(post: Post, documentId: String) {
        val received = Post(
            auth.currentUser?.uid,
            post.postBody,
            post.fromUser,
            post.toUser,
            post.postBody,
            postType = PostType.RECEIVED,
            post.timestamp,
        )
        val postDocRef =
            db.collection("chatrooms").document(documentId).collection("posts").document()
        postDocRef.set(received).addOnSuccessListener {
        }
            .addOnFailureListener {
                Log.d(TAG, "received: Failed")
            }
    }
}





