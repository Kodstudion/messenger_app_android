package com.example.messenger_app_android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.adapters.PostType
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.services.MessagingServices
import com.example.messenger_app_android.services.RetrofitInstance
import com.example.messenger_app_android.services.models.NotificationData
import com.example.messenger_app_android.services.models.PushNotification
import com.example.messenger_app_android.utilities.Utilities
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModel
import com.example.messenger_app_android.viewmodels.ChatroomFragmentViewModelFactory
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.*

interface ChatroomFragmentChatroomView {
    fun setPost(post: MutableList<Post>)
}

const val TOPIC = "/topics/myTopic"
const val TO_DEVICE = "/to/eIRQBONFTGaAo9hwCBh-1n:APA91bFAQp1Ew-4MdIe-E9rbttM1H09eKVMLrFCKQI356m10WTqI1JIuOYcXy2pkw_IAA17VfxTvmPQ2Lge0zbENO_NEyKMlORAhIrJQBPQ_BYdqD7BcAfmL_QOpSchRumVMCNgLbUNK"

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
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        db = Firebase.firestore
        auth = Firebase.auth

        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager


        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageButton.setOnClickListener {
            val timestamp = Timestamp.now()
            val postBody = binding.sendMessageEditText.text.toString()
            val post = Post(
                auth.currentUser?.uid,
                postBody,
                auth.currentUser?.displayName,
                chatroomTitle,
                postBody,
                PostType.SENT,
                timestamp,
            )
            if (postBody.isNotEmpty()) {
                sendAndReceivePost(post)
                getAndSetPostIsSeen()
                updateUserStatus(timestamp)
                updateChatroomLastUpdate(timestamp)
                PushNotification(
                    NotificationData(auth.currentUser?.displayName ?: "", postBody),
                    TO_DEVICE
                ).also {
                    sendPushNotification(it)
                }
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

    private fun sendAndReceivePost(post: Post) {
        val sent = Post(
            auth.currentUser?.uid,
            post.postBody,
            post.fromUser,
            post.toUser,
            post.postBody,
            PostType.SENT,
            post.timestamp,
        )
        val postDocRef =
            db.collection("chatrooms").document(documentId).collection("posts").document()
        postDocRef.set(sent).addOnSuccessListener {
            setReceivedPost(post)
        }
            .addOnFailureListener {
                Log.d(TAG, "writePost: Failed")
            }
    }

    private fun setReceivedPost(post: Post) {
        val received = Post(
            auth.currentUser?.uid,
            post.postBody,
            post.fromUser,
            post.toUser,
            post.postBody,
            PostType.RECEIVED,
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

    private fun getAndSetPostIsSeen() {
        val postIsSeenDocRef = db.collection("chatrooms").document(documentId)
        postIsSeenDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val postIsSeen = document.data?.get("postIsSeen") as? HashMap<*, *>
                val keys = postIsSeen?.keys
                if (keys != null) {
                    for (key in keys) {
                        if (key != auth.currentUser?.uid) {
                            postIsSeenDocRef.set(
                                hashMapOf(
                                    "postIsSeen" to hashMapOf(key to false)
                                ), SetOptions.merge()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateUserStatus(timestamp: Timestamp) {
        val userDocRef = db.collection("users").document(auth.currentUser?.uid.toString())
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                userDocRef.update("loggedIn", timestamp)
            } else {
                Log.d(TAG, "No such document")
            }
        }
    }

    private fun updateChatroomLastUpdate(timestamp: Timestamp) {
        val lastUpdatedDocRef = db.collection("chatrooms").document(documentId)
        lastUpdatedDocRef.get().addOnSuccessListener { document ->
            if (document != null) {
                lastUpdatedDocRef.update("lastUpdated", timestamp)
            } else {
                Log.d(TAG, "No such document")
            }
        }
    }

    private fun sendPushNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}





