package com.example.messenger_app_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.HomeActivity
import com.example.messenger_app_android.databinding.FragmentLoginWithEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginWithEmailFragment : Fragment() {

    val TAG = "!!!"

    private lateinit var binding: FragmentLoginWithEmailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginWithEmailBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.passwordLoginIv.setOnClickListener {
            auth.signInWithEmailAndPassword(
                binding.emailEt.text.toString(),
                binding.passwordEt.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val displayName = UserProfileChangeRequest.Builder()
                                .setDisplayName(binding.emailEt.text.toString())
                                .build()
                            user.updateProfile(displayName)
                            updateUI(user)
                        }
                    } else {
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        user?.let {
            val intent = Intent(context, HomeActivity::class.java)
            startActivity((intent))
        }
    }
}