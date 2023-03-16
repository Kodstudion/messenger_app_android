package com.example.messenger_app_android

import adapters.MessageAdapter
import adapters.ProfileAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.childEvents
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.view.*
import models.User

class HomeActivity : AppCompatActivity() {

    val TAG = "!!!"

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var messageAdapter: MessageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {

        database = Firebase.database.reference

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        profileAdapter = ProfileAdapter(mutableListOf())
        messageAdapter = MessageAdapter(mutableListOf())

        binding.toolbar
        binding.toolbarTitle.text
        binding.horizontalRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = profileAdapter

        binding.verticalRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.verticalRecyclerview.adapter = messageAdapter

        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email
        val userID = auth.currentUser?.uid


        binding.signOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
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

    }

    private fun saveUserToFb(userId: String, displayName: String, email: String) {
        val user = User(displayName, email)
        database.child("users").child(userId).setValue(user)

    }
}