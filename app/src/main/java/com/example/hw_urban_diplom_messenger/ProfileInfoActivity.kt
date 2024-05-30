package com.example.hw_urban_diplom_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.hw_urban_diplom_messenger.databinding.ActivityProfileInfoBinding
import com.example.hw_urban_diplom_messenger.users.User
import com.google.android.play.integrity.internal.al
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("userId")

        val userRef = userId?.let { FirebaseDatabase.getInstance().reference.child("Users").child(it) }
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("name").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val occupation = dataSnapshot.child("occupation").getValue(String::class.java)
                val address = dataSnapshot.child("address").getValue(String::class.java)
                val age = dataSnapshot.child("age").getValue(String::class.java)
                val profileImageUri = dataSnapshot.child("profileImageUri").getValue(String::class.java)
                val phone = dataSnapshot.child("phoneNumber").getValue(String::class.java)

                binding.usernameTextView.text = name
                binding.nameTextView.text = name
                binding.lastNameTextView.text = lastName
                binding.occupationTextView.text = occupation
                binding.adressTextView.text = address
                binding.ageTextView.text = age
                binding.phoneTextView.text = phone

                Picasso.get()
                    .load(profileImageUri)
                    .placeholder(R.drawable.person)
                    .error(R.drawable.person)
                    .into(binding.profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileInfoActivity", "User data could not be uploaded: $error")
            }
        })
    }
}