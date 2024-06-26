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

    fun setUsers(newUsers: MutableList<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val userImageView: ImageView = itemView.findViewById(R.id.userChatImageView)
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameChatTextView)
        val lastMessageChatTextView: TextView = itemView.findViewById(R.id.lastMessageChatTextView)
        val statusImageView: ImageView = itemView.findViewById(R.id.statusImageView)

        fun bind(user: User) {

            Log.d("ChatStatus", "Binding user: ${user.name}, isOnline: ${user.isOnline}")
            usernameTextView.text = user.name
            lastMessageChatTextView.text = user.lastMessage
            if (user.profileImageUri.isNotEmpty()) {
                Picasso.get().load(user.profileImageUri).placeholder(R.drawable.person)
                    .error(R.drawable.person).into(userImageView)
            } else {
                userImageView.setImageResource(R.drawable.person)
            }

            if (user.isOnline) {
                Log.d("ChatStatus", "статус: ${user.name}, isOnline: ${user.isOnline}")
                statusImageView.visibility = View.VISIBLE
            } else {
                statusImageView.visibility = View.INVISIBLE
            }
        }
    }
}
