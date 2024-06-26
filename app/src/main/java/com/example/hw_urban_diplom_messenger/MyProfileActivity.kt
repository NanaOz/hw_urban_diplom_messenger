package com.example.hw_urban_diplom_messenger

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hw_urban_diplom_messenger.databinding.ActivityMyProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import java.io.ByteArrayOutputStream

class MyProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProfileBinding
    val REQUEST_CODE_SELECT_IMAGE = 1
    val REQUEST_CODE_SELECT_PHOTO = 2
    val REQUEST_CAMERA_PERMISSION = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storageRef = FirebaseStorage.getInstance().reference
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
            val atIndex = it.indexOf('@')
            val maskedChars = it.substring(0, atIndex - 2).replace("[a-zA-Z0-9]".toRegex(), "*")
            val lastChars = it.substring(maxOf(atIndex - 2, 0))
            "$maskedChars$lastChars"
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
            val options = arrayOf("Choose from Gallery", "Take Photo")
            AlertDialog.Builder(this)
                .setTitle("Choose an option")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> {
                            val galleryIntent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(galleryIntent, REQUEST_CODE_SELECT_IMAGE)
                        }

                        1 -> {
                            if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.CAMERA
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this,
                                    arrayOf(Manifest.permission.CAMERA),
                                    REQUEST_CAMERA_PERMISSION
                                )
                            } else {
                                dispatchTakePictureIntent()
                            }
                        }
                    }
                }.show()
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
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_CODE_SELECT_PHOTO)
    }

    private fun savePhoneNumberToFirebase(countryCode: String, phoneNumber: String) {
        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = userId?.let { database.getReference("Users").child(it) }
        val phoneWithCode = "$countryCode $phoneNumber"
        userRef?.child("phoneNumber")?.setValue(phoneWithCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(
                        this,
                        "Camera permission is required to take a photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap?

            if (imageBitmap != null) {
                binding.myProfileImage.setImageBitmap(imageBitmap)

                val bytes = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path =
                    MediaStore.Images.Media.insertImage(contentResolver, imageBitmap, "Title", null)
                val imageUri = Uri.parse(path)

                val storageRef = FirebaseStorage.getInstance().reference
                val imagesRef =
                    storageRef.child("profileImages/${FirebaseAuth.getInstance().currentUser?.uid}/profile.jpg")

                val uploadTask = imagesRef.putFile(imageUri)

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imagesRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val database = FirebaseDatabase.getInstance()
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        val userRef =
                            userId?.let { database.getReference("Users").child(it) }
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
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            Log.d("MyApp", "Image URI: " + imageUri.toString())
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