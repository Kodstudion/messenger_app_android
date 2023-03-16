package com.example.messenger_app_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.adapters.MessageAdapter
import com.example.messenger_app_android.adapters.ProfileAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
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
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val TAG = "!!!"
        database = Firebase.database.reference
        auth = Firebase.auth
        profileAdapter = ProfileAdapter(mutableListOf())
        messageAdapter = MessageAdapter(mutableListOf())

        binding = FragmentChatBinding.inflate(layoutInflater,container,false)

        binding.horizontalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = profileAdapter

        binding.verticalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.verticalRecyclerview.adapter = messageAdapter

        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email
        val userID = auth.currentUser?.uid

        binding.signOut.setOnClickListener {
            auth.signOut()
            Log.d(TAG, "onCreateView: ADASd")
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
        


        if (userID != null && displayName != null && email != null) {
            saveUserToFb(userID, displayName, email)
        }


        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (user in snapshot.children) {
                    val users = user.getValue<User>()?.displayName
                    profileAdapter.addProfile(users ?: "")
                    messageAdapter.addMessage(users ?: "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "onCancelled: ${error.toException()}")
            }
        }
        database.child("users").addValueEventListener(userListener)

        return binding.root

    }

    private fun saveUserToFb(userId: String, displayName: String, email: String) {
        val user = User(displayName, email)
        database.child("users").child(userId).setValue(user)

    }

}

