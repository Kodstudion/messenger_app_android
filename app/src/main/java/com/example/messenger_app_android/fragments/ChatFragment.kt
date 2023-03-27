package com.example.messenger_app_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.viewmodels.ChatFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

interface ChatFragmentChatroomsView {
    fun setChatrooms(chatroom: MutableList<Chatroom>)
}

interface ChatFragmentUsersView {
    fun setUsers(user: User)
}

class ChatFragment : Fragment(), ChatFragmentChatroomsView, ChatFragmentUsersView {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentChatBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var chatroomAdapter: ChatRoomAdapter
    private lateinit var chatFragmentViewModel: ChatFragmentViewModel


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        chatFragmentViewModel = ViewModelProvider(this)[ChatFragmentViewModel::class.java]
        chatFragmentViewModel.attachChatrooms(this)
        chatFragmentViewModel.attachUsers(this)

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
        chatroomAdapter = ChatRoomAdapter(fragmentManager)


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
    }

    override fun setChatrooms(chatroom: MutableList<Chatroom>) {
       chatroomAdapter.submitList(chatroom)
    }

    override fun setUsers(user: User) {
        userAdapter.users.add(user)
        userAdapter.notifyDataSetChanged()
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



