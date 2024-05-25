package com.example.hw_urban_diplom_messenger.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.users.User
import com.squareup.picasso.Picasso

class UserAdapter (private var users: MutableList<User> = mutableListOf()) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var onItemClickListener: ((User) -> Unit)? = null

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.person_profile_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]
        holder.bind(currentUser)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    fun setUsers(newUsers: List<User>) {
//        Log.d("UserAdapter", "users, count AFTER: ${users.size}")
//        Log.d("UserAdapter", "newUsers, count AFTER: ${newUsers.size}")
////        users.clear()
////        users.addAll(newUsers)
//        Log.d("UserAdapter", "newUsers, count BEFORE: ${newUsers.size}")
//        Log.d("UserAdapter", "users, count BEFORE: ${users.size}")
//        notifyDataSetChanged()

        val newUsersList = mutableListOf<User>()
        newUsersList.addAll(newUsers)
        users.clear()
        users.addAll(newUsersList)
        Log.d("UserAdapter", "New users set, count: ${users.size}")
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImageView: ImageView = itemView.findViewById(R.id.userImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)

        fun bind(user: User) {
            usernameTextView.text = user.name

            // Load user image using Picasso or show a placeholder image if image URL is not available
            if (user.profileImageUri.isNotEmpty()) {
                Picasso.get().load(user.profileImageUri).placeholder(R.drawable.person).error(R.drawable.person).into(userImageView)
            } else {
                userImageView.setImageResource(R.drawable.person)
            }
        }
    }
}