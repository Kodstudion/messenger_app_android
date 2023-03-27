package com.example.messenger_app_android.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.fragments.ChatroomFragmentChatroomView
import com.example.messenger_app_android.models.Post
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatroomFragmentViewModel(private val documentId: String) : ViewModel() {

    private var chatroomView: ChatroomFragmentChatroomView? = null
    private val db = Firebase.firestore

    fun attachChatroom(chatroomFragmentChatroomView: ChatroomFragmentChatroomView) {
        chatroomView = chatroomFragmentChatroomView
        listenForPostUpdates()
    }

    private fun listenForPostUpdates() {
        val TAG = "!!!"
        db.collection("chatrooms").document(documentId).collection("posts").orderBy(
            "timestamp", Query.Direction.ASCENDING
        ).addSnapshotListener { snapshot, error ->
            snapshot?.let { querySnapshot ->
                try {
                    val newPost = mutableListOf<Post>()
                    querySnapshot.documents.forEach { document ->
                        val post = document.toObject(Post::class.java)
                        if (post != null) {
                            newPost.add(post)
                            chatroomView?.setPost(newPost)
                        }
                    }

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
