package com.example.messenger_app_android.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.fragments.ChatFragmentView
import com.example.messenger_app_android.models.Chatroom
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ChatFragmentViewModel : ViewModel() {

    private var view: ChatFragmentView? = null
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var chatroomAdapter: ChatRoomAdapter

    fun attach(chatFragmentView: ChatFragmentView) {
        view = chatFragmentView
        listenForChatroomUpdates()
    }


    private fun listenForChatroomUpdates() {
        val TAG = "!!!"
        chatroomAdapter = ChatRoomAdapter(mutableListOf(), null)

        db.collection("chatrooms")
            .whereArrayContains("participants", auth.currentUser?.uid.toString())
            .addSnapshotListener { snapshot, error ->
                snapshot?.let { querySnapshot ->
                    try {
                        for (document in querySnapshot.documents) {
                            val chatroom = document.toObject<Chatroom>()
                            chatroom?.documentId = document.id
                            if (chatroom != null) {
                                chatroomAdapter.chatrooms.add(
                                    Chatroom(
                                        chatroom.documentId,
                                        null,
                                        chatroom.text,
                                        null,
                                        chatroom.fromUser,
                                        chatroom.toUser
                                    )
                                )
                                view?.setChatroom(chatroom)
                            }
                        }
                        chatroomAdapter.notifyDataSetChanged()
                    } catch (e: Exception) {
                        Log.d(TAG, "listenForItemUpdates: $e")
                    }
                }
                error?.let {
                    Log.d(TAG, "listenForItemUpdates: $it")
                }
            }


    }
}