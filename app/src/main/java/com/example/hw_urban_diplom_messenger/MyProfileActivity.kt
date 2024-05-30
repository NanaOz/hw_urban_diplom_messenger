package com.example.hw_urban_diplom_messenger

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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
    val REQUEST_CODE_SELECT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storageRef = FirebaseStorage.getInstance().reference
        val imagesRef =
            storageRef.child("profileImages/${FirebaseAuth.getInstance().currentUser?.uid}/profile.jpg")

        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("Users").child(it) }
        userRef?.child("profileImageUri")?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val downloadUri = dataSnapshot.getValue(String::class.java)

                Picasso.get()
                    .load(downloadUri)
                    .placeholder(R.drawable.myprofile)
                    .error(R.drawable.person_edit)
                    .into(binding.myProfileImage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MyProfileActivity, "error getting image URI", Toast.LENGTH_LONG)
                    .show()
            }
        })

        userId?.let {
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
                    Toast.makeText(
                        this@MyProfileActivity,
                        "error getting information",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
        binding.saveButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val occupation = binding.occupationEditText.text.toString()
            val address = binding.adressEditText.text.toString()
            val age = binding.ageEditText.text.toString()

            saveProfileInfoToFirebase(name, lastName, occupation, address, age)
            Toast.makeText(this, "Information saved", Toast.LENGTH_LONG).show()
        }

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        val maskedEmail = email?.let {
            val maskedChars = it.substring(0, it.length - 6).replace("[a-zA-Z0-9]".toRegex(), "*")
            val lastTwoChars = it.substring(it.length - 6)
            "$maskedChars$lastTwoChars"
        }

        binding.emailTextView.text = maskedEmail
        userId?.let {
            userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    Log.d("MyProfileActivity", "Name from database: $name")
                    if (user != null) {
                        binding.nameTextView.text = name
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@MyProfileActivity,
                        "error getting information",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }

        binding.changeProfileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

        binding.addPhoneImageView.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_phone, null)
            val flagImageView = dialogView.findViewById<ImageView>(R.id.flagImageView)
            val codeSpinner = dialogView.findViewById<Spinner>(R.id.codeSpinner)
            val phoneEditText = dialogView.findViewById<EditText>(R.id.phoneEditText)
            val applyButton = dialogView.findViewById<Button>(R.id.applyButton)

            val adapter = ArrayAdapter.createFromResource(
                this,
                R.array.country_codes,
                android.R.layout.simple_spinner_item
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            codeSpinner.adapter = adapter

            codeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val flagsArray = resources.obtainTypedArray(R.array.country_flags)
                    flagImageView.setImageResource(flagsArray.getResourceId(position, -1))
                    flagsArray.recycle()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .show()

            applyButton.setOnClickListener {
                val selectedCountryCode = codeSpinner.selectedItem.toString()
                val phoneNumber = phoneEditText.text.toString()

                savePhoneNumberToFirebase(selectedCountryCode, phoneNumber)

                dialog.dismiss()
            }
        }

    }

    private fun savePhoneNumberToFirebase(countryCode: String, phoneNumber: String) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("Users").child(it) }
        val phoneWithCode = "$countryCode $phoneNumber"
        userRef?.child("phoneNumber")?.setValue(phoneWithCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data

            val storageRef = FirebaseStorage.getInstance().reference
            val imagesRef =
                storageRef.child("profileImages/${FirebaseAuth.getInstance().currentUser?.uid}/profile.jpg")

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

                    val database = FirebaseDatabase.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val userRef = userId?.let { database.getReference("Users").child(it) }
                    userRef?.child("profileImageUri")?.setValue(downloadUri.toString())

                    Picasso.get()
                        .load(downloadUri)
                        .placeholder(R.drawable.myprofile)
                        .error(R.drawable.person_edit)
                        .into(binding.myProfileImage)
                } else {
                    Toast.makeText(this, "image upload error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveProfileInfoToFirebase(
        name: String,
        lastName: String,
        occupation: String,
        address: String,
        age: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("Users").child(it) }
        userRef?.child("name")?.setValue(name)
        userRef?.child("lastName")?.setValue(lastName)
        userRef?.child("occupation")?.setValue(occupation)
        userRef?.child("address")?.setValue(address)
        userRef?.child("age")?.setValue(age)
    }
}