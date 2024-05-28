package com.example.hw_urban_diplom_messenger

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.adapters.ChatAdapter
import com.example.hw_urban_diplom_messenger.adapters.MessagesAdapter
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.chats.Message
import com.example.hw_urban_diplom_messenger.databinding.ActivityChatBinding
import com.example.hw_urban_diplom_messenger.databinding.FragmentChatsBinding
import com.google.android.play.integrity.internal.al
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.lang.Exception
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

        setSupportActionBar(binding.toolbar)

        val userName = intent.getStringExtra("userName")
        val profileImageUri = intent.getStringExtra("userProfileImageUri")
        chatId = intent.getStringExtra("chatId") ?: ""

        messagesAdapter = MessagesAdapter(mutableListOf())
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = messagesAdapter

        loadMessages()

        binding.sendMessageImageButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageEditText.text.clear()
            } else {
                Toast.makeText(this, "Message field cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.titleTextView.text = userName
        Picasso.get()
            .load(profileImageUri)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(
                    bitmap: Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    val drawable = BitmapDrawable(resources, bitmap)
                    binding.photoImageView.setImageDrawable(drawable)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.e("Picasso", "Failed to load image: $e")
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
                return true
            }

            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            }

            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_exit -> {
                finishAffinity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
