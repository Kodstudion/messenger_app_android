package com.example.messenger_app_android.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.models.Chatroom
import kotlinx.android.synthetic.main.item_horizontal_recyclerview.view.*
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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
            profile_picture.setOnClickListener {
              createChatroom(Chatroom("", mutableListOf(auth.currentUser?.uid.toString(), "x1bqJmyNPnPYzQ2ePi3p0hkGyK93"),
              "Hej", null,null,user.displayName.toString()),
                  user.displayName.toString())

            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    private fun createChatroom(chatroom: Chatroom, title: String) {
        val utilities = Utilities()
        val db = FirebaseFirestore.getInstance()
        val chatroomDocRef = db.collection("chatrooms").document()
        chatroomDocRef.set(chatroom)
            .addOnSuccessListener {
                val documentId = chatroomDocRef.id
                Log.d("!!!", "DocumentSnapshot successfully written!")
                utilities.loadFragment(ChatRoomFragment(title,documentId),fragmentManager)
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error writing document", e)
            }
    }
}






