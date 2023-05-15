package com.example.messenger_app_android.utilities

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.messenger_app_android.R

class Utilities {

    fun loadFragment(fragment: Fragment, supportFragmentManager: FragmentManager?) {
        val transaction = supportFragmentManager?.beginTransaction()
        transaction?.replace(R.id.fragment_container, fragment)
        transaction?.commit()
    }
}