package com.example.hw_urban_diplom_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.adapters.ChatAdapter
import com.example.hw_urban_diplom_messenger.adapters.MessagesAdapter
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.chats.Message
import com.example.hw_urban_diplom_messenger.databinding.ActivityChatBinding
import com.example.hw_urban_diplom_messenger.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("name")
        chatId = intent.getStringExtra("chatId") ?: ""

        messagesAdapter = MessagesAdapter(mutableListOf()) // Initialize the adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = messagesAdapter

        loadMessages() // Load existing messages

        binding.sendMessageImageButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(this, "Message field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(message: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ownerId = currentUser?.uid
        if (ownerId != null) {
            val messageInfo = mapOf(
                "text" to message,
                "ownerId" to ownerId
            )
            FirebaseDatabase.getInstance().reference.child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo)
        } else {
            Log.e("sendMessage", "Error: currentUser or uid is null")
        }
    }

    private fun loadMessages() {
        FirebaseDatabase.getInstance().reference.child("Chats")
            .child(chatId).child("messages").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages: MutableList<Message> = mutableListOf()
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    for (messageSnapshot in snapshot.children) {
                        val messageId = messageSnapshot.key
                        val ownerId = messageSnapshot.child("ownerId").value.toString()
                        val text = messageSnapshot.child("text").value.toString()
                        val message = messageId?.let { Message(it, ownerId, text) }
                        if (message != null) {
                            messages.add(message)
                        }
                    }
                    messagesAdapter.updateMessages(messages)
                    messagesAdapter.notifyDataSetChanged()

                    binding.messagesRecyclerView.post {
                        binding.messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("loadMessages", "Failed to load messages: $error")
                }
            })
    }
}

//        chatAdapter = ChatAdapter(ArrayList(), FirebaseAuth.getInstance().currentUser?.uid ?: "")
//
//        val userId = intent.getStringExtra("userId")
//        val userName = intent.getStringExtra("userName")
//
//        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
//
//        binding.sendMessageImageButton.setOnClickListener {
//            val message = binding.messageEditText.text.toString().trim()
//
//            if (message.isNotEmpty() && currentUserUid != null) {
//                // Отправка сообщения в Firebase Database
//                val database = FirebaseDatabase.getInstance()
//                val chatRef = database.getReference("chats")
//
//                val newChat = userName?.let { it1 ->
//                    Chat("your_chat_id", "your_chat_name", currentUserUid,
//                        it1
//                    )
//                }
//                val newChatRef = chatRef.push()
//                newChatRef.setValue(newChat)
//
//                // Очистить поле ввода сообщения
//                binding.messageEditText.text.clear()
//            }
//        }
//        // Отобразить сообщения в RecyclerView
//        val layoutManager = LinearLayoutManager(this)
//        binding.messagesRecyclerView.layoutManager = layoutManager
//        binding.messagesRecyclerView.adapter = chatAdapter
//
//        // Слушатель изменений в документе БД
//        val chatRef = FirebaseDatabase.getInstance().getReference("chats")
//        chatRef.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val chat = snapshot.getValue(Chat::class.java)
//                if (chat != null && (chat.chatId == "your_chat_id")) {
//                    chatAdapter.addChat(chat)
//                    layoutManager.scrollToPosition(chatAdapter.itemCount - 1)
//                }
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//            override fun onCancelled(error: DatabaseError) {}
//        })
//    }
//
//
//}