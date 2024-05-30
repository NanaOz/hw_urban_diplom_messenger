package com.example.hw_urban_diplom_messenger.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hw_urban_diplom_messenger.ChatActivity
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.fragments.ChatsFragment
import com.example.hw_urban_diplom_messenger.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ChatAdapter(private val chats: MutableList<Chat>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userChatImageView: ImageView = itemView.findViewById(R.id.userChatImageView)
        val usernameChatTextView: TextView = itemView.findViewById(R.id.usernameChatTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        holder.usernameChatTextView.text = chat.chatName

        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(chat.userId1)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUri = snapshot.child("profileImageUri").getValue(String::class.java)
                if (!profileImageUri.isNullOrEmpty()) {
                    Picasso.get()
                        .load(profileImageUri)
                        .placeholder(R.drawable.person)
                        .error(R.drawable.person)
                        .into(holder.userChatImageView)
                } else {
                    holder.userChatImageView.setImageResource(R.drawable.person)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatAdapter", "Failed to retrieve user data: $error")
            }
        })

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

}