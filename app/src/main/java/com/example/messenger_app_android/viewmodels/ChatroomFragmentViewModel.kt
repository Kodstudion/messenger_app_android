package com.example.messenger_app_android.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.messenger_app_android.adapters.PostType
import com.example.messenger_app_android.fragments.ChatroomFragmentChatroomView
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatroomFragmentViewModel(private val documentId: String) : ViewModel() {
    val TAG = "!!!"
    private var chatroomView: ChatroomFragmentChatroomView? = null
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun attachChatroom(chatroomFragmentChatroomView: ChatroomFragmentChatroomView) {
        chatroomView = chatroomFragmentChatroomView
        listenForPostUpdates()
    }

    private fun listenForPostUpdates() {
        db.collection("chatrooms").document(documentId).collection("posts").orderBy(
            "timestamp", Query.Direction.ASCENDING
        ).addSnapshotListener { snapshot, error ->

            snapshot?.let { querySnapshot ->
                try {
                    val posts = mutableListOf<Post>()
                    querySnapshot.documents.forEach { document ->
                        val post = document.toObject(Post::class.java)
                        if (post?.userId == auth.currentUser?.uid && post?.postType == PostType.SENT) {
                            posts.add(post)
                        }
                        if (post?.userId != auth.currentUser?.uid && post?.postType == PostType.RECEIVED) {
                            posts.add(post)
                        }
                    }
                    chatroomView?.setPost(posts)
                } catch (e: Exception) {
                    Log.d(TAG, "listenForPostUpdates: $e")
                }
            }

            error?.let {
                Log.d(TAG, "listenForPostUpdates: $it")
            }
        }
    }
}


class ChatroomFragmentViewModelFactory(private val documentId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatroomFragmentViewModel::class.java)) {
            return ChatroomFragmentViewModel(documentId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
