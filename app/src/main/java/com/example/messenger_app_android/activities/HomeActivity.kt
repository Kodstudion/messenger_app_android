package com.example.messenger_app_android.activities

import com.example.messenger_app_android.adapters.MessageAdapter
import com.example.messenger_app_android.adapters.ProfileAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.R
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.fragments.ChatFragment
import com.example.messenger_app_android.fragments.PersonsFragment
import com.example.messenger_app_android.fragments.SettingsFragment
import com.example.messenger_app_android.models.Message
import com.example.messenger_app_android.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.NonCancellable.key

class HomeActivity : AppCompatActivity() {

    val TAG = "!!!"

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        loadFragment(ChatFragment())
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chat_fragment -> {
                    loadFragment(ChatFragment())
                    true
                }
                R.id.persons_fragment -> {
                    loadFragment(PersonsFragment())
                    true
                }
                R.id.settings_fragment -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}