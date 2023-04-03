package com.example.messenger_app_android.fragments

import android.content.Intent

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.viewmodels.ChatFragmentViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


val TAG = "!!!"

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
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        chatFragmentViewModel = ViewModelProvider(this)[ChatFragmentViewModel::class.java]
        chatFragmentViewModel.attachChatrooms(this)
        chatFragmentViewModel.attachUsers(this)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        binding.drawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        if (userID != null && displayName != null && email != null) {
//            val timestamp = Timestamp.now()
            saveUser(userID, displayName, email)
        }
    }

    override fun setChatrooms(chatroom: MutableList<Chatroom>) {
        chatroomAdapter.submitList(chatroom)
    }

    override fun setUsers(user: User) {
        userAdapter.users.add(user)
        userAdapter.notifyDataSetChanged()
//        isUserOnline(user.uid ?: "")

    }

    private fun saveUser(
        uid: String,
        displayName: String,
        email: String,
//        timestamp: Timestamp,
    ) {
        val user = User(uid, displayName, email, null)
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("!!!", "DocumentSnapshot successfully written!")
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error writing document", e)
            }
    }

//    private fun isUserOnline(uid: String) {
//        val minute: Long = 60 * 1000
//        val timeHandler = Handler(Looper.getMainLooper())
//        timeHandler.post(object : Runnable {
//            override fun run() {
//                db.collection("users").addSnapshotListener { snapshot, error ->
//                    snapshot?.documents?.forEach { document ->
//                        val user = document.toObject<User>()
//                        val online = Timestamp.now().toDate().time
//                        val time = user?.timestamp?.toDate()?.time ?: 0
//                        val isOnline = (online - time) < minute
//                        db.collection("users").document(uid)
//                            .update("online", !false)
//                    }
//                }
//
//            }
//        })
//    }

}




