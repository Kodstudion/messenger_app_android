package com.example.messenger_app_android.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.R
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.services.RetrofitInstance
import com.example.messenger_app_android.services.constants.StringConstants
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
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap

interface ChatroomFragmentChatroomView {
    fun setPost(post: MutableList<Post>)
}

class ChatRoomFragment : Fragment(),
    ChatroomFragmentChatroomView {

          private lateinit var auth: FirebaseAuth
        private lateinit var db: FirebaseFirestore
        private lateinit var binding: FragmentChatRoomBinding
        private lateinit var postAdapter: PostAdapter
        private lateinit var chatroomFragmentViewModel: ChatroomFragmentViewModel
        private lateinit var chatroomTitle: String
        private lateinit var chatroomPicture: String
        private lateinit var documentId: String
        private lateinit var chatroom: Chatroom
        private lateinit var user: User

        val TAG = "!!!"
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            binding = FragmentChatRoomBinding.inflate(layoutInflater, container, false)
            chatroomFragmentViewModel = ViewModelProvider(
                this,
                ChatroomFragmentViewModelFactory()
            )[ChatroomFragmentViewModel::class.java]

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

            documentId = arguments?.getString(StringConstants.DOCUMENT_ID).toString()
            chatroomTitle = arguments?.getString(StringConstants.CHATROOM_TITLE).toString()
            chatroomPicture = arguments?.getString(StringConstants.CHATROOM_PICTURE).toString()
            chatroomFragmentViewModel.documentId = documentId
            chatroomFragmentViewModel.attachChatroom(this)

            val utilities = Utilities();
            val fragmentManager = requireActivity().supportFragmentManager

            getChatroom(documentId) { chatroomCallbackResult ->
                if (chatroomCallbackResult != null) {
                    chatroom = chatroomCallbackResult
                    chatroom.typing?.forEach { entry ->
                        if (entry.key != auth.currentUser?.uid && entry.value) {
                            chatroom.participantsNames?.get(entry.key)?.let { otherParticipantName ->
                                binding.userIsTypingTextView.text =
                                    "$otherParticipantName is typing ..."
                            }
                        } else if (entry.key != auth.currentUser?.uid && !entry.value) {
                            binding.userIsTypingTextView.text = ""
                        }
                    }
                }
            }

            getUser(auth.currentUser?.uid.toString()) { userCallbackResult ->
                if (userCallbackResult != null) {
                    user = userCallbackResult
                }
            }


        binding.toolbarTitleChatroom.text = chatroomTitle
        Picasso.get()
            .load(chatroomPicture)
            .transform(
                RoundedTransformationBuilder()
                    .cornerRadius(50f)
                    .oval(false)
                    .build()
            )
            .into(binding.profilePictureChatroom)

        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageEditText.addTextChangedListener(object : TextWatcher {
            private var isTyping = false
            private var handler = Handler(Looper.getMainLooper())
            private var isTypingRunnable: Runnable? = null
            private val fiveSeconds: Long = 5000
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                startTyping()
            }

            private fun startTyping() {
                if (!isTyping) {
                    isTyping = true
                    updateIsTyping(chatroom, documentId, true)
                } else {
                    resetTimer()
                }
            }

            private fun stopTyping() {
                isTyping = false
                isTypingRunnable?.let {
                    handler.removeCallbacks(it)
                    isTypingRunnable = null
                }
                updateIsTyping(chatroom, documentId, false)
            }

            private fun startTimer() {
                isTypingRunnable = Runnable { stopTyping() }
                handler.postDelayed(isTypingRunnable ?: return, fiveSeconds)
            }

            private fun resetTimer() {
                isTypingRunnable?.let {
                    handler.removeCallbacks(it)
                    isTypingRunnable = null
                }
                startTimer()
            }
        })


        binding.sendMessageButton.setOnClickListener {
            val timestamp = Timestamp.now()
            val postBody = binding.sendMessageEditText.text.toString()
            chatroom.profilePictures?.forEach { entry ->
                if (entry.key == auth.currentUser?.uid) {
                    user.profilePicture = entry.value
                }
            }


            val post = Post(
                auth.currentUser?.uid,
                postBody,
                auth.currentUser?.displayName,
                chatroomTitle,
                postBody,
                timestamp,
                user.profilePicture
            )
            if (postBody.isNotEmpty()) {
                sendPost(post)
                getAndSetPostIsSeen()
                updateUserStatus(timestamp)
                updateChatroomLastUpdate(timestamp)
                updateSender(chatroom, documentId, postBody)
                PushNotification(
                    NotificationData(
                        auth.currentUser?.displayName ?: "",
                        postBody,
                        documentId,
                        chatroomTitle,
                        auth.currentUser?.displayName ?: "",
                        currentUserToken(chatroom),
                        otherDeviceToken(chatroom),
                        user.profilePicture ?: ""
                    ),
                    ""
                ).also {
                    sendPushNotification(it, chatroom)
                }
                binding.sendMessageEditText.text.clear()
            } else {
                Toast.makeText(activity, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
            chatroomFragmentViewModel.updateResentMessageText(postBody)
        }
    }

    override fun setPost(post: MutableList<Post>) {
        postAdapter.submitList(post)
        binding.chatConversationListAdapter.scrollToPosition(post.size - 1)

    }

    private fun sendPost(post: Post) {
        val postDocRef =
            db.collection("chatrooms").document(documentId).collection("posts").document()
        postDocRef.set(post).addOnSuccessListener {

        }
            .addOnFailureListener {
                Log.d(TAG, "writePost: Failed")
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

    private fun sendPushNotification(notification: PushNotification, chatroom: Chatroom) =
        CoroutineScope(Dispatchers.IO).launch {
            checkIfDeviceTokenIsValid(chatroom)
            try {
                chatroom.deviceTokens?.forEach { entry ->
                    if (entry.key != auth.currentUser?.uid) {
                        notification.to = entry.value
                    }
                }
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

    private fun checkIfDeviceTokenIsValid(chatroom: Chatroom) {
        val sharedPreferences = requireActivity().getSharedPreferences(
            R.string.sharedPreferences.toString(),
            Context.MODE_PRIVATE
        )
        val authCurrentUserDeviceToken =
            sharedPreferences.getString(R.string.token.toString(), null)
        chatroom.deviceTokens?.forEach { entry ->
            if (entry.key == auth.currentUser?.uid && entry.value != authCurrentUserDeviceToken) {
                val deviceTokenRef = db.collection("chatrooms").document(documentId)
                deviceTokenRef.get().addOnSuccessListener { document ->
                    if (document != null) {
                        deviceTokenRef.set(
                            hashMapOf("deviceTokens" to hashMapOf(entry.key to authCurrentUserDeviceToken)),
                            SetOptions.merge()
                        )
                    }
                }
            } else {
                return
            }
        }
    }

    private fun currentUserToken(chatroom: Chatroom): String {
        var currentUserToken = ""
        chatroom.deviceTokens?.forEach { entry ->
            if (entry.key == auth.currentUser?.uid) {
                currentUserToken = entry.value
            }
        }
        return currentUserToken
    }

    private fun otherDeviceToken(chatroom: Chatroom): String {
        var otherDeviceToken = ""
        chatroom.deviceTokens?.forEach { entry ->
            if (entry.key != auth.currentUser?.uid) {
                otherDeviceToken = entry.value
            }
        }
        return otherDeviceToken
    }

    private fun getChatroom(documentId: String, callback: (Chatroom?) -> Unit) {
        val chatroomDocRef = db.collection("chatrooms").document(documentId)
        chatroomDocRef.addSnapshotListener { snapshot, _ ->
            snapshot?.let { querySnapshot ->
                try {
                    val chatroom = querySnapshot.toObject(Chatroom::class.java)
                    callback(chatroom)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    private fun getUser(documentId: String, callback: (User?) -> Unit) {
        val userDocRef = db.collection("users").document(documentId)
        userDocRef.addSnapshotListener { snapshot, _ ->
            snapshot?.let { querySnapshot ->
                try {
                    val user = querySnapshot.toObject(User::class.java)
                    callback(user)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    private fun updateIsTyping(chatroom: Chatroom, documentId: String, isTyping: Boolean) {
        val isTypingDocRef = db.collection("chatrooms").document(documentId)
        chatroom.typing?.forEach { entry ->
            if (entry.key == auth.currentUser?.uid) {
                isTypingDocRef.set(
                    hashMapOf(
                        "typing" to hashMapOf(entry.key to isTyping)
                    ), SetOptions.merge()
                )
            }
        }
    }

    private fun updateSender(chatroom: Chatroom, documentId: String, recentMessage: String) {
        val senderDocRef = db.collection("chatrooms").document(documentId)
        chatroom.sender?.forEach { entry ->
            if (entry.key == auth.currentUser?.uid) {
                senderDocRef.set(
                    hashMapOf(
                        "sender" to hashMapOf(entry.key to recentMessage)
                    ), SetOptions.merge()
                )
            } else {
                senderDocRef.update("sender", hashMapOf(auth.currentUser?.uid to recentMessage))
            }
        }
    }
}









