package com.example.hw_urban_diplom_messenger

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hw_urban_diplom_messenger.databinding.ActivityMyProfileBinding

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("userName", "")
        val email = sharedPreferences.getString("userEmail", "")

        binding.nameTextView.text = name

        val hiddenEmail = hideEmail(email)
        binding.emailTextView.text = hiddenEmail
    }

    private fun hideEmail(email: String?): String {
        if (!email.isNullOrEmpty() && email.contains("@")) {
            val atIndex = email.indexOf('@')
            val maskedEmail = StringBuilder()
            for (i in 0 until atIndex-2) {
                maskedEmail.append('*')
            }
            maskedEmail.append(email.substring(atIndex - 2))
            return maskedEmail.toString()
        }
        return ""
    }
}