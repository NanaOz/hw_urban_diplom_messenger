package com.example.hw_urban_diplom_messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.ChatActivity
import com.example.hw_urban_diplom_messenger.adapters.ChatAdapter
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.databinding.FragmentChatsBinding
import com.example.hw_urban_diplom_messenger.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var chatAdapter: ChatAdapter
    private val usersList: MutableList<User> = mutableListOf()
    private val chatsList: MutableList<Chat> = mutableListOf()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root

        chatAdapter = ChatAdapter(usersList)
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.chatsRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.chatsRecyclerView.adapter = chatAdapter

        retrieveChatsFromFirebase()

        chatAdapter.setOnItemClickListener { user ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val chatId = generateChatId(userId, user.userId)
                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("chatId", chatId)
                intent.putExtra("userName", user.name)
                intent.putExtra("userProfileImageUri", user.profileImageUri)
                intent.putExtra("userId", user.userId)
                startActivity(intent)
            }
        }

        return view
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        val users = listOf(userId1, userId2)
        val sortedUsers = users.sorted()
        return sortedUsers.joinToString("-")
    }

    private fun retrieveChatsFromFirebase() {
        val chatsRef = FirebaseDatabase.getInstance().getReference("Chats")

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatsList.clear()

                for (chatSnapshot in dataSnapshot.children) {
                    val chatId = chatSnapshot.key.toString()
                    val userIds = chatId.split("-")
                    val userId1 = userIds[0]
                    val userId2 = userIds[1]

                    if (userId1 == currentUserUid || userId2 == currentUserUid) {
                        val userIdToDisplay = if (userId1 != currentUserUid) userId1 else userId2
                        val existingChat = usersList.find { it.userId == userIdToDisplay }

                        if (existingChat == null) {
                            val userRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child(userIdToDisplay)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val userName = userSnapshot.child("name").value.toString()
                                    val userProfileImageUri = userSnapshot.child("profileImageUri").value.toString()
                                    val user = User(userName, userProfileImageUri, userIdToDisplay)

                                    user.isOnline = userSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false

                                    val messagesRef = FirebaseDatabase.getInstance().getReference("Chats/$chatId/messages")
                                    messagesRef.limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(messagesSnapshot: DataSnapshot) {
                                            if (messagesSnapshot.exists()) {
                                                val lastMessageSnapshot = messagesSnapshot.children.first()
                                                val lastMessageText = lastMessageSnapshot.child("text").value.toString()


                                                val limitedLastMessage = if (lastMessageText.length > 20) {
                                                    "${lastMessageText.substring(0, 25)}..."
                                                } else {
                                                    lastMessageText
                                                }

                                                user.lastMessage = limitedLastMessage

                                                usersList.add(user)
                                                chatAdapter.setUsers(usersList)
                                                Log.d("ChatAdapter", "Users retrieved from Firebase: ${user.name}")
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Log.e("ChatsFragment", "Failed to retrieve messages: $databaseError")
                                        }
                                    })
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e("ChatsFragment", "Failed to retrieve user data: $databaseError")
                                }
                            })
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ChatsFragment", "Failed to retrieve chats: $databaseError")
            }
        })
    }
}
//    private fun setupOnlineStatusListener() {
//        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
//        val currentUserListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val currentUser = usersList.find { it.userId == currentUserUid }
//                currentUser?.let {
//                    val isOnline = dataSnapshot.child("isOnline").getValue(Boolean::class.java) ?: false
//                    it.isOnline = isOnline
//                    Log.e("ChatStatus", "Current user is online: $currentUser")
//                    activity?.runOnUiThread {
//                        chatAdapter.notifyDataSetChanged() // Обновляем RecyclerView, чтобы отразить изменения в основном потоке
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.e("ChatsFragment", "Failed to retrieve current user data: $databaseError")
//            }
//        }
//
//        currentUserUid?.let {
//            usersRef.child(it).addValueEventListener(currentUserListener)
//        }
//    }
