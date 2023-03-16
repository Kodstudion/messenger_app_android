package adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.messenger_app_android.R
import kotlinx.android.synthetic.main.item_horizontal_recyclerview.view.*
import models.User


class ProfileAdapter(private val profiles: MutableList<User>) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
       return ProfileViewHolder(
           LayoutInflater.from(parent.context).inflate(
               R.layout.item_horizontal_recyclerview, parent, false
           )
       )
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
       val profile = profiles[position]
        holder.itemView.apply {
            profile.profilePicture?.let { profile_picture.setImageResource(it) }
            display_name.text = profile.displayName
        }
    }

    override fun getItemCount(): Int {
      return profiles.size
    }

    fun addProfile(profile: String) {
        profiles.add(User(profile))
        notifyItemChanged(profiles.size - 1)
    }
}






