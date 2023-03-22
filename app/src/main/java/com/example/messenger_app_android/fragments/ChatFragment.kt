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
import com.example.messenger_app_android.adapters.ProfileAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentChatBinding
    private lateinit var profileAdapter: ProfileAdapter
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

        database = Firebase.database.reference
        auth = Firebase.auth

        val fragmentManager = requireActivity().supportFragmentManager
        profileAdapter = ProfileAdapter(mutableListOf(), fragmentManager)
        chatroomAdapter = ChatRoomAdapter(mutableListOf(), fragmentManager)


        binding.horizontalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = profileAdapter

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
            //chatRoom(userID,displayName)
        }


        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val users = snapshot.getValue<User>()?.displayName ?: return
                profileAdapter.addProfile(User(null,users))
                profileAdapter.profiles.add(User(null, displayName))
                Log.d(TAG, "onDataChange: $users")


            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "onCancelled: ${error.toException()}")
            }
        }
        database.child("users").addValueEventListener(userListener)

        val chatRoomListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue<Post>()?.displayName

                chatroomAdapter.addMessage(name ?: "", "Bajen är bäst")
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
        database.child("users").addValueEventListener(chatRoomListener)

    }


    //    private fun createChatroom(firstUser: String, secondUser: String) {
//        val chatroomRef = database.child("users").child(auth.uid.toString()).child("chatroom").push()
//
//        val newChatroom = Chatroom(firstUser, secondUser)
//        val chatValues = newChatroom.toMap()
//
//        chatroomRef.setValue(chatValues)
//
//    }
//
    private fun saveUser(uid: String, displayName: String, email: String) {
        val userRef = database.child("users")
        val newUser = User(uid, displayName, email)

        val userValue = newUser.toMap()
        userRef.setValue(userValue)


    }
//
//    private fun chatRoom(userId: String, displayName: String) {
//        val chatRoom = Post(userId, null,displayName,null,null, null, null, PostType.SENT)
//
//        database.child("users").child(userId).child("message").setValue(chatRoom)
//    }


}

