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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("name")

        val chatId = intent.getStringExtra("chatId")
        loadMessages(chatId)
        binding.sendMessageImageButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Message field cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.messageEditText.setText("") //clearing the edit text
            sendMessage(chatId, message)
        }
    }

    private fun sendMessage(chatId: String?, message: String) {
        if (chatId == null) return
        val messageInfo = HashMap<String, String>()
        messageInfo["text"] = message
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ownerId = currentUser?.uid
        if (ownerId != null) {
            messageInfo["ownerId"] = ownerId
            FirebaseDatabase.getInstance().reference.child("Chats").child(chatId)
                .child("messages").push().setValue(messageInfo)
        } else {
            Log.e("sendMessage", "Error: currentUser or uid is null")
        }
    }

    private fun loadMessages(chatId: String?) {
        if (chatId == null) return
        FirebaseDatabase.getInstance().reference.child("Chats")
            .child(chatId).child("messages").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    val messages: MutableList<Message> = ArrayList()
                    for (messageSnapshot in snapshot.children) {
                        val messageId = messageSnapshot.key
                        val ownerId = messageSnapshot.child("ownerId").value.toString()
                        val text = messageSnapshot.child("text").value.toString()
                        messageId?.let { Message(it, ownerId, text,) }?.let { messages.add(it) }
                    }
                    binding.messagesRecyclerView.layoutManager = LinearLayoutManager(baseContext)
                    binding.messagesRecyclerView.adapter = MessagesAdapter(messages)
                }

                override fun onCancelled(error: DatabaseError) {}
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