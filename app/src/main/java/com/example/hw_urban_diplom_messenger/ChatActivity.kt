package com.example.hw_urban_diplom_messenger

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.adapters.MessagesAdapter
import com.example.hw_urban_diplom_messenger.chats.Message
import com.example.hw_urban_diplom_messenger.databinding.ActivityChatBinding
import com.example.hw_urban_diplom_messenger.push.ApiService
import com.example.hw_urban_diplom_messenger.push.NotificationBody
import com.example.hw_urban_diplom_messenger.push.SendMessageDto
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.lang.Exception
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import android.Manifest
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var chatId: String
    private var REQUEST_SELECT_FILE = 1

    private var selectedFileUri: Uri? = null
    private var isFileSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val userName = intent.getStringExtra("userName")
        val profileImageUri = intent.getStringExtra("userProfileImageUri")
        chatId = intent.getStringExtra("chatId") ?: ""

        messagesAdapter =
            MessagesAdapter(mutableListOf(), object : MessagesAdapter.MessageLongClickListener {
                override fun onMessageLongClick(message: Message, hasImage: Boolean) {
                    if (hasImage) {
                        val builder = AlertDialog.Builder(this@ChatActivity)
                        builder.setTitle("Options")
                        val options = arrayOf("View Image", "Delete Message")
                        builder.setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    message.imageUri?.let { showImageDialog(it) }
                                }

                                1 -> {
                                    deleteMessage(message)
                                }
                            }
                        }
                        builder.show()
                    } else {
                        showDeleteConfirmationDialog(message)
                    }
                }
            })

        val layoutManager = LinearLayoutManager(this)

        layoutManager.stackFromEnd = true
        binding.messagesRecyclerView.layoutManager = layoutManager
//        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = messagesAdapter

        binding.messagesRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.messagesRecyclerView.postDelayed({
                    binding.messagesRecyclerView.scrollToPosition(messagesAdapter.itemCount - 1)
                }, 100)
            }
        }

        loadMessages()

        binding.sendMessageImageButton.setOnClickListener {

            if (isFileSelected) {
                selectedFileUri?.let {
                    sendMessageWithAttachment(it, binding.messageEditText.text.toString())
                    binding.attachFileImageButton.setImageResource(R.drawable.attach_file)
                    selectedFileUri = null
                    isFileSelected = false
                    binding.messageEditText.text.clear()
                }
            } else {
                val message = binding.messageEditText.text.toString()
                if (message.isNotEmpty()) {
                    sendMessage(message)
                    binding.messageEditText.text.clear()
                } else {
                    Toast.makeText(this, "Message field cannot be empty", Toast.LENGTH_SHORT).show()
                }
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

        binding.attachFileImageButton.setOnClickListener {
            openGalleryForFile()
        }
    }

    private fun showImageDialog(imageUri: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_photo, null)
        val flagImageView = dialogView.findViewById<ImageView>(R.id.flagImageView)
        val applyButton = dialogView.findViewById<Button>(R.id.applyButton)

        Picasso.get().load(imageUri).into(flagImageView)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .show()

        applyButton.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun openGalleryForFile() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_FILE)
    }

    private fun sendMessageWithAttachment(fileUri: Uri, message: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ownerId = currentUser?.uid

        val fileName = "attachments/${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val fileRef = storageRef.child(fileName)

        val uploadTask = fileRef.putFile(fileUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                val messageInfo = mapOf(
                    "text" to if (message.isNotEmpty()) message else "",
                    "fileUri" to imageUrl,
                    "ownerId" to ownerId
                )

                FirebaseDatabase.getInstance().reference.child("Chats").child(chatId)
                    .child("messages").push().setValue(messageInfo)
            }.addOnFailureListener { exception ->
                Log.e("getImageUrl", "Error getting image URL: $exception")
            }
        }.addOnFailureListener { exception ->
            Log.e("uploadFileToFirebase", "Error uploading file: $exception")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedFileUri = data.data
            isFileSelected = true
            binding.attachFileImageButton.setImageResource(R.drawable.attach_yes)
        }
    }

    private fun showDeleteConfirmationDialog(message: Message) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Message")
        builder.setMessage("Are you sure you want to delete this message?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteMessage(message)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun deleteMessage(message: Message) {
        FirebaseDatabase.getInstance().reference.child("Chats").child(chatId)
            .child("messages").child(message.id)
            .removeValue()
    }

    fun getInterlocutorId(currentUserId: String, chatId: String): String {
        val userIds = chatId.split("-")
        return if (userIds[0] == currentUserId) {
            userIds[1]
        } else {
            userIds[0]
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

            val userId = getInterlocutorId(ownerId.toString(), chatId)
            val userRef = userId?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it) }
            userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val token = dataSnapshot.child("token").getValue(String::class.java)
                    Log.e("pushNotification", "token: $token")
                    if (token != null) {
                        pushNotification(message, token)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("pushNotification", "User data token could not be uploaded: $error")
                }
            })


        } else {
            Log.e("sendMessage", "Error: currentUser or uid is null")
        }
    }

    private fun pushNotification(message: String, token: String) {
        val notification = NotificationBody(
            title = "Новое сообщение",
            body = message
        )

        val sendMessageDto = SendMessageDto(
            to = token,
            notification = notification
        )

        val apiService = Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/v1/projects/hwurbandiplommessenger/messages:send/")
//            .baseUrl("https://fcm.googleapis.com/fcm/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.sendMessage(sendMessageDto)
            } catch (e: Exception) {
                Log.e("pushNotification", "Ошибка отправки уведомления: ${e.message}")
            }
        }
    }

    private fun loadMessages() {
        FirebaseDatabase.getInstance().reference.child("Chats")
            .child(chatId).child("messages").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages: MutableList<Message> = mutableListOf()
                    for (messageSnapshot in snapshot.children) {
                        val messageId = messageSnapshot.key
                        val ownerId = messageSnapshot.child("ownerId").value.toString()
                        val text = messageSnapshot.child("text").value.toString()
                        val imageUri = messageSnapshot.child("fileUri").value?.toString()

                        val message = messageId?.let { Message(it, ownerId, text, imageUri) }
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
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.action_profile -> {
                val userId = intent.getStringExtra("userId")
                val intent = Intent(this, ProfileInfoActivity::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
                return true
            }

            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                val sharedPreferences =
                    getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
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
