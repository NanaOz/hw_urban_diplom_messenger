package com.example.hw_urban_diplom_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hw_urban_diplom_messenger.databinding.ActivityProfileInfoBinding
import com.squareup.picasso.Picasso

class ProfileInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName")
        val profileImageUri = intent.getStringExtra("profileImageUri")

        binding.nameEditText.text = userName
        Picasso.get()
            .load(profileImageUri)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(binding.profileImage)
    }
}