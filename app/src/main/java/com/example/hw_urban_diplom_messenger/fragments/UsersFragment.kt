package com.example.hw_urban_diplom_messenger.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hw_urban_diplom_messenger.ChatActivity
import com.example.hw_urban_diplom_messenger.databinding.FragmentUsersBinding
import com.example.hw_urban_diplom_messenger.users.User
import com.example.hw_urban_diplom_messenger.adapters.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment() {

    private lateinit var binding: FragmentUsersBinding
    private lateinit var usersAdapter: UserAdapter
    private val usersList: MutableList<User> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUsersBinding.inflate(inflater, container, false)
        val view = binding.root

        usersAdapter = UserAdapter(usersList)
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewUsers.adapter = usersAdapter

        retrieveUsersFromFirebase()

        usersAdapter.setOnItemClickListener { user ->
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            if (userId != null) {
                val chatId = generateChatId(userId, user.userId)

                val intent = Intent(activity, ChatActivity::class.java)
                intent.putExtra("chatId", chatId)
                intent.putExtra("userName", user.name)
                intent.putExtra("userProfileImageUri", user.profileImageUri)
                startActivity(intent)
            }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString()
                if (searchQuery.isNotEmpty()) {
                    val filteredUsers =
                        usersList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    usersAdapter.setUsers(filteredUsers.toMutableList())
                } else {

                    retrieveUsersFromFirebase()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    private fun generateChatId(userId1: String, userId2: String): String {
        val users = listOf(userId1, userId2)
        val sortedUsers = users.sorted()
        return sortedUsers.joinToString("-")
    }

    private fun retrieveUsersFromFirebase() {
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList.clear()
                val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key.toString()
                    if (userId == currentUserUid) {
                        continue
                    }
                    val userName = userSnapshot.child("name").value.toString()
                    val userProfileImageUri = userSnapshot.child("profileImageUri").value.toString()

                    Log.d(
                        "UserAdapter",
                        "Adding user - Id: $userId, Name: $userName, ProfileImageUri: $userProfileImageUri"
                    )

                    val user = User(userName, userProfileImageUri, userId)
                    usersList.add(user)
                }
                usersAdapter.setUsers(usersList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("UsersFragment", "Failed to retrieve data: $databaseError")
            }
        })
    }
}