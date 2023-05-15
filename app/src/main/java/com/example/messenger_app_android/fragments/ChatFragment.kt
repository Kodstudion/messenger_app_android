package com.example.messenger_app_android.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.R
import com.example.messenger_app_android.activities.LoginActivity
import com.example.messenger_app_android.adapters.ChatRoomAdapter
import com.example.messenger_app_android.adapters.ChatroomAdapterCallback
import com.example.messenger_app_android.adapters.UserAdapter
import com.example.messenger_app_android.databinding.FragmentChatBinding
import com.example.messenger_app_android.models.Chatroom
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.services.MessagingServices
import com.example.messenger_app_android.services.constants.StringConstants
import com.example.messenger_app_android.viewmodels.ChatFragmentViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest


val TAG = "!!!"

interface ChatFragmentChatroomsView {
    fun setChatroom(chatroom: MutableList<Chatroom>)
    fun setUsers(user: MutableList<User>)
}

class ChatFragment : Fragment(), ChatFragmentChatroomsView {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentChatBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var chatroomAdapter: ChatRoomAdapter
    private lateinit var chatFragmentViewModel: ChatFragmentViewModel

    private val PICK_IMAGE_REQUEST = 1

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(TAG, "Permission granted")
            } else {
                Log.d(TAG, "Permission denied")
            }
        }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        chatFragmentViewModel = ViewModelProvider(this)[ChatFragmentViewModel::class.java]
        chatFragmentViewModel.attach(this, requireContext())

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = Firebase.firestore
        auth = Firebase.auth

        lifecycleScope.launchWhenCreated {
            chatFragmentViewModel.dataLoaded.collect { dataLoaded ->
                if (dataLoaded) {
                    chatroomAdapter.submitList(chatFragmentViewModel.chatrooms)
                }
            }
        }

        askNotificationPermission()

        val fragmentManager = requireActivity().supportFragmentManager
        userAdapter = UserAdapter(mutableListOf(), fragmentManager)
        chatroomAdapter = ChatRoomAdapter(fragmentManager, object : ChatroomAdapterCallback {
            override fun getUsers(userId: String): User? {
                return chatFragmentViewModel.getUser(userId)
            }
        })


        binding.horizontalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.horizontalRecyclerview.adapter = userAdapter

        binding.verticalRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.verticalRecyclerview.adapter = chatroomAdapter

        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email
        val userID = auth.currentUser?.uid
        val profilePicture = auth.currentUser?.photoUrl

        binding.signOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.drawer.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.toolBarProfilePicture.setOnClickListener {
            openImagePicker()
        }

        if (userID != null && displayName != null && email != null) {
            val timestamp = Timestamp.now()
            db.collection("users").document(userID).get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    saveUser(
                        userID,
                        displayName,
                        email,
                        profilePicture.toString(),
                        timestamp
                    )
                } else {
                    val user = document.toObject<User>()
                    Picasso.get()
                        .load(user?.profilePicture)
                        .fit()
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .transform(
                            RoundedTransformationBuilder()
                                .cornerRadiusDp(50f)
                                .oval(false)
                                .build()
                        ).into(binding.toolBarProfilePicture)
                    updateDeviceToken()
                    storeProfilePictureUrl(user)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            handleSelectedImage(imageUri)
        }
    }

    override fun setChatroom(chatroom: MutableList<Chatroom>) {
        chatroomAdapter.submitList(chatroom)
    }

    override fun setUsers(user: MutableList<User>) {
        userAdapter.users.clear()
        userAdapter.users.addAll(user)
        userAdapter.notifyDataSetChanged()
    }

    private fun saveUser(
        uid: String,
        displayName: String,
        email: String,
        profilePicture: String,
        timestamp: Timestamp,

        ) {
        val user = User(uid, displayName, email, profilePicture, timestamp)
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("!!!", "DocumentSnapshot successfully written!")
                updateDeviceToken()

            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error writing document", e)
            }
    }

    private fun updateDeviceToken() {
        val db = Firebase.firestore
        val auth = Firebase.auth
        MessagingServices.sharedPreferences = context?.getSharedPreferences(
            R.string.sharedPreferences.toString(),
            Context.MODE_PRIVATE
        )
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            MessagingServices.token = task.result
            db.collection("users").document(auth.currentUser?.uid ?: "")
                .update("deviceToken", MessagingServices.token)
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun handleSelectedImage(imageUri: Uri?) {
        imageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val db = Firebase.firestore
            val imageFileName = "images/profile_pictures/${auth.currentUser?.uid}.jpg"
            val imageRef = storageRef.child(imageFileName)
            imageRef.putFile(uri).addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "handleSelectedImage: ${taskSnapshot.metadata?.path}")
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri).fit().transform(
                        RoundedTransformationBuilder()
                            .cornerRadius(100f)
                            .oval(true)
                            .build()
                    ).into(binding.toolBarProfilePicture)

                    db.collection("users").document(auth.currentUser?.uid ?: "").update(
                        "profilePicture", uri
                    )

                    db.collection("chatrooms").get().addOnSuccessListener { documents ->
                        documents?.forEach { document ->
                            val chatroom = document.toObject(Chatroom::class.java)
                            if (chatroom.documentId == document.id) {
                                chatroom.profilePictures?.forEach { entry ->
                                    if (entry.key == auth.currentUser?.uid) {
                                        val profilePictureDocRef =
                                            db.collection("chatrooms").document(document.id)
                                        profilePictureDocRef.set(
                                            hashMapOf(
                                                "profilePictures" to hashMapOf(
                                                    entry.key to uri
                                                )
                                            ), SetOptions.merge()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "handleSelectedImage: $e")
            }
        }
    }

    private fun storeProfilePictureUrl(user: User?) {
        val sharedPreferences = context?.getSharedPreferences(R.string.sharedPreferences.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString(StringConstants.PROFILE_PICTURE_URL, user?.profilePicture)
        editor?.apply()
    }

}




