package com.example.messenger_app_android.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.messenger_app_android.R
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.fragments.ChatFragmentChatroomsView
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.services.constants.StringConstants

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow


class ChatFragmentViewModel() : ViewModel() {

    val TAG = "!!!"

    private var chatroomsView: ChatFragmentChatroomsView? = null
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private lateinit var userAdapter: UserAdapter
    private val users = mutableListOf<User>()
    internal var chatrooms = mutableListOf<Chatroom>()

    val dataLoaded = MutableStateFlow(false)


    fun attach(chatFragmentChatroomsView: ChatFragmentChatroomsView, context: Context?) {
        chatroomsView = chatFragmentChatroomsView
        listenForUsersUpdates(context)
        listenForChatroomUpdates()
    }

    private fun listenForChatroomUpdates() {
        userAdapter = UserAdapter(mutableListOf())
        db.collection("chatrooms")
            .whereArrayContains("participants", auth.currentUser?.uid.toString())
            .addSnapshotListener { snapshot, error ->
                snapshot?.let { querySnapshot ->
                    try {
                        val updatedChatrooms = mutableListOf<Chatroom>()
                        for (document in querySnapshot.documents) {
                            val chatroom = document.toObject<Chatroom>()
                            chatroom?.documentId = document.id
                            if (chatroom != null) {
                                showChatroomTitle(chatroom)
                                updatedChatrooms.add(chatroom)
                            }
                        }
                        chatroomsView?.setChatroom(updatedChatrooms)
                        dataLoaded.value = chatrooms.isNotEmpty() && users.isNotEmpty()

                    } catch (e: Exception) {
                        Log.d(TAG, "listenForItemUpdates: $e")
                    }
                }
                error?.let {
                    Log.d(TAG, "listenForItemUpdates: $it")
                }
            }
    }

    private fun listenForUsersUpdates(context: Context?) {
        val auth = Firebase.auth
        db.collection("users").addSnapshotListener { snapshot, _ ->
            snapshot?.let { querySnapshot ->
                try {
                    users.clear()
                    for (document in querySnapshot.documents) {
                        val user = document.toObject<User>()
                        if (user?.uid == auth.currentUser?.uid) {
                            storeProfilePictureUrl(user, context)
                            continue
                        } else {
                            userAdapter.users.add(user ?: return@addSnapshotListener)
                            users.add(user)
                        }
                    }
                    chatroomsView?.setUsers(users)
                    dataLoaded.value = chatrooms.isNotEmpty() && users.isNotEmpty()
                } catch (e: Exception) {
                    Log.d(TAG, "listenForItemUpdates: $e")
                }
            }
        }
    }
    private fun showChatroomTitle(chatroom: Chatroom) {
        chatroom.participantsNames?.forEach { entry ->
            if (entry.key != auth.currentUser?.uid) {
                chatroom.chatroomTitle = entry.value
            }
        }
    }
    fun getUser(userId: String): User? {
        return users.find { it.uid == userId }
    }
    private fun storeProfilePictureUrl(user: User?, context: Context?) {
        val sharedPreferences = context?.getSharedPreferences(R.string.sharedPreferences.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(StringConstants.PROFILE_PICTURE_URL, user?.profilePicture)
        editor?.apply()
    }
}

