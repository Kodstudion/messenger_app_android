package com.example.messenger_app_android.activities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.messenger_app_android.R
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.example.messenger_app_android.fragments.ChatFragment
import com.example.messenger_app_android.fragments.ChatRoomFragment
import com.example.messenger_app_android.fragments.PersonsFragment
import com.example.messenger_app_android.fragments.SettingsFragment
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.utilities.Utilities


class HomeActivity : AppCompatActivity() {

    val TAG = "!!!"

    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        val utilities = Utilities();

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        utilities.loadFragment(ChatFragment(), supportFragmentManager)
        setContentView(binding.root)

        loadFragmentFromPushNotice(utilities)


        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chat_fragment -> {
                   utilities.loadFragment(ChatFragment(), supportFragmentManager)
                    true
                }
                R.id.persons_fragment -> {
                    utilities.loadFragment(PersonsFragment(), supportFragmentManager)
                    true
                }
                R.id.settings_fragment -> {
                    utilities.loadFragment(SettingsFragment(),supportFragmentManager)
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    private fun loadFragmentFromPushNotice(utilities: Utilities) {
        val documentId = intent.getStringExtra(StringConstants.DOCUMENT_ID)
        val chatroomTitle = intent.getStringExtra(StringConstants.CHATROOM_TITLE)
        val chatroomPicture = intent.getStringExtra(StringConstants.CHATROOM_PICTURE)
        if (documentId != null) {
            val chatroomFragment = ChatRoomFragment().apply {
                arguments = Bundle().apply {
                    putString(StringConstants.DOCUMENT_ID, documentId)
                    putString(StringConstants.CHATROOM_TITLE, chatroomTitle)
                    putString(StringConstants.CHATROOM_PICTURE, chatroomPicture)

                }
            }
            utilities.loadFragment(chatroomFragment, supportFragmentManager)
        } else {
            utilities.loadFragment(ChatFragment(), supportFragmentManager)
        }
    }
}