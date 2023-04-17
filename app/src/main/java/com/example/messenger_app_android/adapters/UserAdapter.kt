package com.example.messenger_app_android.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper.getMainLooper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import kotlinx.android.synthetic.main.item_horizontal_recyclerview.view.*
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


const val TAG = "!!!"

enum class Status {
    ONLINE, OFFLINE
}

class UserAdapter(
    var users: MutableList<User>,
    private val fragmentManager: FragmentManager? = null,
) : RecyclerView.Adapter<UserAdapter.ProfileViewHolder>() {
    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_horizontal_recyclerview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val auth = FirebaseAuth.getInstance()
        val user = users[position]
        holder.itemView.apply {
            display_name.text = user.displayName

            user.profilePicture?.let { profile_picture.setImageResource(it) }

            isUserOnline(user, profile_picture, R.drawable.round_green_circle, R.drawable.round_blue_circle)
            
            profile_picture.setOnClickListener {
                val sharedPreferences = context.getSharedPreferences(R.string.sharedPreferences.toString(), Context.MODE_PRIVATE)
                val authCurrentUserDeviceToken = sharedPreferences.getString(R.string.token.toString(), null)
                chatroomHandler(
                    Chatroom(
                        "",
                        mutableListOf(
                            auth.currentUser?.uid.toString(),
                            user.uid.toString()
                        ),
                        null,
                        user.displayName.toString(),
                        null,
                        hashMapOf(
                            auth.currentUser?.uid.toString() to auth.currentUser?.displayName.toString(),
                            user.uid.toString() to user.displayName.toString()
                        ),
                        null,
                        null,
                        hashMapOf(auth.currentUser?.uid.toString() to true, user.uid.toString() to true),
                        hashMapOf(auth.currentUser?.uid.toString() to authCurrentUserDeviceToken.toString(),
                        user.uid.toString() to user.deviceToken.toString())
                    ),
                    user.displayName.toString(),
                    position
                )
            }
        }
    }
    override fun getItemCount(): Int {
        return users.size
    }
    private fun chatroomHandler(chatroom: Chatroom, titleOfChat: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        db.collection("chatrooms")
            .whereArrayContains("participants", auth.currentUser?.uid ?: return).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.documents.isNotEmpty()) {
                    for (document in snapshot.documents) {
                        val participants = document.get("participants") as List<*>
                        if (participants.contains(users[position].uid)) {
                            chatroom.documentId = document.id
                            joinChatroom(chatroom.documentId, titleOfChat)
                            return@addOnSuccessListener
                        }
                    }
                    createOrJoinChatroom(chatroom, titleOfChat)
                } else {
                    createOrJoinChatroom(chatroom, titleOfChat)
                }
            }
    }

    private fun createOrJoinChatroom(chatroom: Chatroom, titleOfChat: String) {
        val db = FirebaseFirestore.getInstance()
        val chatroomDocRef = db.collection("chatrooms").document()
        chatroomDocRef.set(chatroom)
            .addOnSuccessListener {
                chatroom.documentId = chatroomDocRef.id
                joinChatroom(chatroom.documentId, titleOfChat)
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error writing document", e)
            }
    }

    private fun joinChatroom(chatroomId: String, title: String) {
        val utilities = Utilities()
        val db = FirebaseFirestore.getInstance()

        db.collection("chatrooms").document(chatroomId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val documentId = snapshot.id
                utilities.loadFragment(
                    ChatRoomFragment(title, documentId), fragmentManager
                )
            } else {
                Log.d("!!!", "No such document")
            }
        }
    }

    private fun isUserOnline(user: User, imageView: ImageView, imageResOnline: Int, imageResOffline: Int) {
        val timeHandler = Handler(getMainLooper())
        val tenMinutes: Long = 10 * 60 * 1000
        val loggedIn = user.loggedIn
        timeHandler.post(object: Runnable{
            override fun run() {
                val currentTime = System.currentTimeMillis() - (loggedIn?.seconds?.times(1000) ?: 0)
                if (currentTime < tenMinutes) {
                    Status.ONLINE
                    imageView.setImageResource(imageResOnline)
                } else {
                    Status.OFFLINE
                    imageView.setImageResource(imageResOffline)
                }
                timeHandler.postDelayed(this, tenMinutes)
            }
        })
    }
}











