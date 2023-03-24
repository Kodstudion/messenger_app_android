package com.example.messenger_app_android.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.fragments.ChatFragmentChatroomsView
import com.example.messenger_app_android.fragments.ChatFragmentUsersView
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ChatFragmentViewModel : ViewModel() {

    private var chatroomsView: ChatFragmentChatroomsView? = null
    private var usersView: ChatFragmentUsersView? = null
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var chatroomAdapter: ChatRoomAdapter
    private lateinit var userAdapter: UserAdapter

    fun attachChatrooms(chatroomsChatFragmentView: ChatFragmentChatroomsView) {
        chatroomsView = chatroomsChatFragmentView
        listenForChatroomUpdates()
    }

    fun attachUsers(usersChatFragmentView: ChatFragmentUsersView) {
        usersView = usersChatFragmentView
        getUsers()

    }

    private fun listenForChatroomUpdates() {
        val TAG = "!!!"
        chatroomAdapter = ChatRoomAdapter(mutableListOf(), null)
        userAdapter = UserAdapter(mutableListOf(), null)

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
                                chatroomsView?.setChatrooms(chatroom)
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

    private fun getUsers() {
        db.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                val user = document.toObject(User::class.java)
                val newUser = User(document.id, user.displayName, user.email)
                userAdapter.users.add(newUser)
                usersView?.setUsers(user)
                usersView?.setUsers(User("x1bqJmyNPnPYzQ2ePi3p0hkGyK93", "Janne", null))
                usersView?.setUsers(User("x1bqJmyNPnPYzQ2ePi3p0hkGyK93", "Berra", null))

                userAdapter.notifyDataSetChanged()
            }
        }
    }
}