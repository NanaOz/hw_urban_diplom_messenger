package com.example.hw_urban_diplom_messenger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.chats.Message
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class MessagesAdapter(private val messages: MutableList<Message>, private val messageLongClickListener: MessageLongClickListener) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    interface MessageLongClickListener {
        fun onMessageLongClick(message: Message, hasImage: Boolean)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTv.text = message.text

        if (message.imageUri != null) {
            holder.imageView.visibility = View.VISIBLE
            Picasso.get().load(message.imageUri).into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return MessageViewHolder(view, messages, messageLongClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].ownerId == FirebaseAuth.getInstance().currentUser?.uid)
            R.layout.message_from_user_item
        else
            R.layout.message_to_user_item
    }

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
    }

    class MessageViewHolder(itemView: View, private val messages: List<Message>, private val messageLongClickListener: MessageLongClickListener) : RecyclerView.ViewHolder(itemView) {
        var messageTv: TextView
        var imageView: ImageView

        init {
            messageTv = itemView.findViewById(R.id.messageTextView)
            imageView = itemView.findViewById(R.id.messageImageView)

            itemView.setOnLongClickListener {
                val hasImage = !messages[adapterPosition].imageUri.isNullOrEmpty()
                messageLongClickListener.onMessageLongClick(messages[adapterPosition], hasImage)
                true
            }
        }
    }
}
