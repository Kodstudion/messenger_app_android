package com.example.messenger_app_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentChatBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var chatroomAdapter: ChatRoomAdapter


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val TAG = "!!!"
        db = Firebase.firestore
        auth = Firebase.auth

        val fragmentManager = requireActivity().supportFragmentManager
        userAdapter = UserAdapter(mutableListOf(), fragmentManager)
        chatroomAdapter = ChatRoomAdapter(mutableListOf(), fragmentManager)


        binding.horizontalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = userAdapter

        binding.verticalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.verticalRecyclerview.adapter = chatroomAdapter

        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email
        val userID = auth.currentUser?.uid

        binding.signOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }


        if (userID != null && displayName != null && email != null) {
            saveUser(userID, displayName, email)

        }

        db.collection("users").get().addOnSuccessListener { result ->
            for (document in result) {
                Log.d(TAG, "${document.id} => ${document.data}")
                val user = document.toObject(User::class.java)
                val newUser = User(document.id, user.displayName, user.email)
                userAdapter.users.add(newUser)
                userAdapter.users.add(User(null, "Janne", null))
                userAdapter.users.add(User(null, "Berra", null))
                userAdapter.notifyDataSetChanged()
            }
        }

        db.collection("chatrooms").addSnapshotListener { snapshot, error ->
            snapshot?.let { querySnapshot ->
                try {
                    for (document in querySnapshot.documents) {
                        val chatroom = document.toObject<Chatroom>()
                        chatroom?.documentId = document.id
                        if (chatroom != null) {
                            chatroomAdapter.chatrooms.add(Chatroom(chatroom.documentId,null,null,null,chatroom.fromUser,chatroom.toUser))
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

    private fun saveUser(uid: String, displayName: String, email: String) {
        val user = User(uid, displayName, email)
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("!!!", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error writing document", e)
            }
    }


}

