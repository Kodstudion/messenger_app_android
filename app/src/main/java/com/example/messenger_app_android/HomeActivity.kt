package com.example.messenger_app_android
import adapters.ProfileAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_horizontal_recyclerview.*
import models.User

class HomeActivity : AppCompatActivity() {

    val TAG = "!!!"

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var profileAdapter: ProfileAdapter


    override fun onCreate(savedInstanceState: Bundle?) {

        database = Firebase.database.reference

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        profileAdapter = ProfileAdapter(mutableListOf())

        binding.toolbar
        binding.toolbarTitle.text
        binding.horizontalRecyclerview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = profileAdapter

        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email
        val userID = auth.currentUser?.uid


        binding.signOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.drawer.setOnClickListener {
           profileAdapter.addProfile(User(displayName))
        }


        if (userID != null && displayName != null && email != null) {
            saveUserToFb(userID, displayName, email)
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.getValue<User>()?.displayName
                binding.textView.text = post as String
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "onCancelled: ${error.toException()}", )
            }
        }

            database.child("users").child(userID as String).addValueEventListener(postListener)






//        val database = Firebase.database
//        val myRef = database.getReference("message")
//
//
//
//        myRef.setValue("Hello, World!")
//
//
//        // Read from the database
//        myRef.addValueEventListener(object: ValueEventListener {
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = snapshot.getValue<String>()
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//
//        })
    }

    private fun saveUserToFb(userId: String, displayName: String, email: String) {
        val user = User(displayName, email)
        database.child("users").child(userId).setValue(user)

    }
}