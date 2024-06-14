package com.example.hw_urban_diplom_messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hw_urban_diplom_messenger.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signButton.setOnClickListener {
            if (binding.emailEditText.text.toString().isEmpty() ||
                binding.passwordEditText.text.toString().isEmpty() ||
                binding.passwordReplayEditText.text.toString().isEmpty()
            ) {
                Toast.makeText(
                    applicationContext,
                    "Fields cannot be empty",
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.passwordEditText.text.toString() != binding.passwordReplayEditText.text.toString()) {
                Toast.makeText(
                    applicationContext,
                    "Passwords do not match",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    binding.emailEditText.text.toString(),
                    binding.passwordEditText.text.toString()
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userInfo = HashMap<String, String>()
                        userInfo["email"] = binding.emailEditText.text.toString()
                        userInfo["name"] = binding.nameEditText.text.toString()
                        FirebaseDatabase.getInstance().reference.child("Users").child(
                            FirebaseAuth.getInstance().currentUser!!.uid
                        ).setValue(userInfo)
                        startActivity(
                            Intent(
                                this@SignupActivity,
                                MessengerActivity::class.java
                            )
                        )
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Email is already registered",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}