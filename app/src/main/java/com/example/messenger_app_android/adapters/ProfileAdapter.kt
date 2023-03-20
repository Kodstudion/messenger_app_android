package com.example.messenger_app_android.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import com.example.messenger_app_android.fragments.ChatRoomFragment
import kotlinx.android.synthetic.main.item_horizontal_recyclerview.view.*
import com.example.messenger_app_android.models.User
import com.example.messenger_app_android.utilities.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class ProfileAdapter(
    val profiles: MutableList<User>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_horizontal_recyclerview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val utilities = Utilities()
        val profile = profiles[position]
        holder.itemView.apply {
            display_name.text = profile.displayName

            profile.profilePicture?.let { profile_picture.setImageResource(it) }
            profile_picture.setOnClickListener {
                utilities.loadFragment(
                    ChatRoomFragment(profile.displayName.toString()), fragmentManager)

            }
        }
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun addProfile(user: User) {
        profiles.add(User(user.displayName))
        notifyItemInserted(profiles.size - 1)
    }
}






