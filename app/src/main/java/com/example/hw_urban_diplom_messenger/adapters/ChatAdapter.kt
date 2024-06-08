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

class ChatAdapter(private var users: MutableList<User> = mutableListOf()) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var onItemClickListener: ((User) -> Unit)? = null

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.person_chat_item, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
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
        val newUsersList = mutableListOf<User>()
        newUsersList.addAll(newUsers)
        users.clear()
        users.addAll(newUsersList)
        Log.d("UserAdapter", "New users set, count: ${users.size}")
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val userImageView: ImageView = itemView.findViewById(R.id.userChatImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameChatTextView)
        private val lastMessageChatTextView: TextView = itemView.findViewById(R.id.lastMessageChatTextView)

        fun bind(user: User) {
            usernameTextView.text = user.name
            lastMessageChatTextView.text = user.lastMessage
            if (user.profileImageUri.isNotEmpty()) {
                Picasso.get().load(user.profileImageUri).placeholder(R.drawable.person)
                    .error(R.drawable.person).into(userImageView)
            } else {
                userImageView.setImageResource(R.drawable.person)
            }
        }
    }
}
