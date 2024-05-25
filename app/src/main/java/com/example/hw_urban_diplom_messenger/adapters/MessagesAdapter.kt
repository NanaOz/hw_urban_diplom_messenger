package com.example.hw_urban_diplom_messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.chats.Message
import com.google.firebase.auth.FirebaseAuth
import java.util.Objects

class MessagesAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTv.text = message.text
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].ownerId == FirebaseAuth.getInstance().currentUser?.uid)
            R.layout.message_from_user_item
        else
            R.layout.message_to_user_item
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var messageTv: TextView

        init {
            messageTv = itemView.findViewById(R.id.messageTextView)

        }
    }
}
