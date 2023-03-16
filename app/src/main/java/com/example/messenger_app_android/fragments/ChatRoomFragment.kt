package com.example.messenger_app_android.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.messenger_app_android.databinding.ActivityHomeBinding
import com.example.messenger_app_android.databinding.FragmentChatRoomBinding
import com.example.messenger_app_android.utilities.Utilities


class ChatRoomFragment(var title: String) : Fragment() {

    private lateinit var binding: FragmentChatRoomBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChatRoomBinding.inflate(layoutInflater)


        val utilities = Utilities();
        val fragmentManager = requireActivity().supportFragmentManager
        binding = FragmentChatRoomBinding.inflate(layoutInflater,container,false)

        binding.arrowLeft.setOnClickListener {
            utilities.loadFragment(ChatFragment(), fragmentManager)
        }

        return binding.root
    }

    override fun onStart() {
        binding.toolbarTitleChatroom.text = title
        super.onStart()
    }


}