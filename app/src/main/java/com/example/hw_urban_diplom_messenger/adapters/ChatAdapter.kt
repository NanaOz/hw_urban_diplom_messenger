package com.example.hw_urban_diplom_messenger.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.fragments.ChatsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatAdapter(private val chats: ArrayList<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        // Set the chat name and last message to the corresponding views in the ChatViewHolder
        holder.usernameChatTextView.text = chat.chatName
        holder.lastMessageChatTextView.text = "последнее сообщение"

        // Set the user image using Firebase
        val userId = if (chat.userId1 != FirebaseAuth.getInstance().currentUser!!.uid) {
            chat.userId1
        } else {
            chat.userId2
        }
        FirebaseDatabase.getInstance().reference.child("Users").child(userId)
            .child("profileImageUri").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val profileImageUri = task.result?.value.toString()
                    // Load the profile image using an image loading library like Glide or Picasso
                    // Example: Glide.with(holder.itemView.context).load(profileImageUri).into(holder.userChatImageView)
                }
            }

        // Set an item click listener for the chat item
        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(holder.itemView.context, ChatsFragment::class.java)
            intent.putExtra("chatId", chat.chatId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.person_chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userChatImageView: ImageView = itemView.findViewById(R.id.userChatImageView)
        val usernameChatTextView: TextView = itemView.findViewById(R.id.usernameChatTextView)
        val lastMessageChatTextView: TextView = itemView.findViewById(R.id.lastMessageChatTextView)
    }
}