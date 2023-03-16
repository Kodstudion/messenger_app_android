package com.example.messenger_app_android.activities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.messenger_app_android.R
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.example.messenger_app_android.fragments.ChatFragment
import com.example.messenger_app_android.fragments.PersonsFragment
import com.example.messenger_app_android.fragments.SettingsFragment
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

}