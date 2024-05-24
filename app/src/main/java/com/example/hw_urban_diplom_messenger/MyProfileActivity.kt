package com.example.hw_urban_diplom_messenger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.hw_urban_diplom_messenger.databinding.ActivityMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    private val REQUEST_CODE_SELECT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        val maskedEmail = email?.let {
            val maskedChars = it.substring(0, it.length -14).replace("[a-zA-Z0-9]".toRegex(), "*")
            val lastFourChars = it.substring(it.length - 14)
            "$maskedChars$lastFourChars"
        }
        binding.emailTextView.text = maskedEmail

        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val occupation = binding.occupationEditText.text.toString()
            val address = binding.adressEditText.text.toString()
            val age = binding.ageEditText.text.toString()

            saveProfileInfoToFirebase(name, lastName, occupation, address, age)
            Toast.makeText(this, "Information saved", Toast.LENGTH_LONG).show()
        }

        binding.changeProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

        loadUserData()
    }

    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun getUserDatabaseRef(): DatabaseReference? {
        val userId = getUserId()
        return userId?.let { FirebaseDatabase.getInstance().getReference("users").child(it) }
    }

    private fun saveProfileInfoToFirebase(name: String, lastName: String, occupation: String, address: String, age: String) {
        val userRef = getUserDatabaseRef()
        userRef?.apply {
            child("name").setValue(name)
            child("lastName").setValue(lastName)
            child("occupation").setValue(occupation)
            child("address").setValue(address)
            child("age").setValue(age)
        }
    }

    private fun loadUserData() {
        val userRef = getUserDatabaseRef()
        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("name").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val occupation = dataSnapshot.child("occupation").getValue(String::class.java)
                val address = dataSnapshot.child("address").getValue(String::class.java)
                val age = dataSnapshot.child("age").getValue(String::class.java)

                binding.nameEditText.setText(name)
                binding.lastNameEditText.setText(lastName)
                binding.occupationEditText.setText(occupation)
                binding.adressEditText.setText(address)
                binding.ageEditText.setText(age)

                userRef.child("name").setValue(name)
                binding.nameTextView.text = name
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "error getting information", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            uploadImageToStorage(imageUri)
        }
    }

    private fun uploadImageToStorage(imageUri: Uri?) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = getUserId()
        val imagesRef = storageRef.child("profileImages/$userId/profile.jpg")

        val uploadTask = imageUri?.let { imagesRef.putFile(it) }

        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result

                val userRef = getUserDatabaseRef()
                userRef?.child("profileImageUri")?.setValue(downloadUri.toString())

                Picasso.get()
                    .load(downloadUri)
                    .placeholder(R.drawable.myprofile)
                    .error(R.drawable.person_edit)
                    .into(binding.myProfileImage)
            } else {
                Toast.makeText(this@MyProfileActivity, "error getting photo", Toast.LENGTH_LONG).show()
            }
        }
    }
}