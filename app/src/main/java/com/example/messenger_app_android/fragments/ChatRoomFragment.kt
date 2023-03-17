package com.example.messenger_app_android.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_app_android.adapters.PostAdapter
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.models.Post
import com.example.messenger_app_android.utilities.Utilities


class ChatRoomFragment(var title: String) : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var binding: FragmentChatRoomBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatRoomBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val TAG = "!!!"

        postAdapter = PostAdapter(mutableListOf())
        binding.chatConversationRecyclerview.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.chatConversationRecyclerview.adapter = postAdapter

        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager

        binding.arrowLeftBack.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        binding.sendMessageButton.setOnClickListener {
           val addPost = binding.sendMessageEditText.text.toString()
            val post = Post(null, addPost)
            postAdapter.addPost(post)
            binding.sendMessageEditText.text.clear()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.toolbarTitleChatroom.text = title

    }

}