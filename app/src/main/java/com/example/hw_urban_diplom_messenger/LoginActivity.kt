package com.example.hw_urban_diplom_messenger

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hw_urban_diplom_messenger.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        checkUserLogin()

        binding.signupTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.forgotMyPasswordTextView.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.loginButton.setOnClickListener {
            if (binding.emailEditText.text.toString().isEmpty() || binding.passwordEditText.text.toString().isEmpty()){
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_LONG).show()
            } else {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                ).addOnCompleteListener{ task ->
                    if(task.isSuccessful){
                        sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                        startActivity(Intent(this, MessengerActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login failed or password.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun checkUserLogin() {
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn){
            startActivity(Intent(this, MessengerActivity::class.java))
            finish()
        }
    }
}