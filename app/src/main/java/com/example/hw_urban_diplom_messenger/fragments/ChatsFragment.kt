package com.example.hw_urban_diplom_messenger.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.ChatActivity
import com.example.hw_urban_diplom_messenger.R
import com.example.hw_urban_diplom_messenger.adapters.ChatAdapter
import com.example.hw_urban_diplom_messenger.adapters.UserAdapter
import com.example.hw_urban_diplom_messenger.chats.Chat
import com.example.hw_urban_diplom_messenger.databinding.FragmentChatsBinding
import com.example.hw_urban_diplom_messenger.users.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsFragment : Fragment() {

    private lateinit var binding: FragmentChatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        val view = binding.root
        loadUsers()

        return view
    }

    private fun loadUsers() {
        val users = ArrayList<User>()
        FirebaseDatabase.getInstance().reference.child("Users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnapshot in snapshot.children) {
                        if (userSnapshot.key == FirebaseAuth.getInstance().currentUser!!.uid) {
                            continue
                        }
                        val uid = userSnapshot.key
                        val username = userSnapshot.child("name").value.toString()
                        val profileImage = userSnapshot.child("profileImageUri").value.toString()
                        uid?.let { User( username, profileImage, it) }?.let { users.add(it) }
                    }
                    binding.chatsRecyclerView.layoutManager = LinearLayoutManager(context)
                    binding.chatsRecyclerView.addItemDecoration(
                        DividerItemDecoration(
                            context,
                            DividerItemDecoration.VERTICAL
                        )
                    )
                    binding.chatsRecyclerView.adapter = UserAdapter(users)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun openChatActivity(chatId: String, userId: String) {
        val intent = Intent(activity, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun getChatsList(): ArrayList<Chat> {
        val chatsList = ArrayList<Chat>()

        // Retrieve the chats of the logged-in user from the Firebase database
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chats")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (chatSnapshot in dataSnapshot.children) {
                        val chat = chatSnapshot.getValue(Chat::class.java)
                        if (chat != null) {
                            chatsList.add(chat)
                        }
                    }
                    // Notify the adapter that the data has changed
//                    chatAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })

        return chatsList
    }
}