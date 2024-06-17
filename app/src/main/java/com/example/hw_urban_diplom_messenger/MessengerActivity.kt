package com.example.hw_urban_diplom_messenger

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.hw_urban_diplom_messenger.adapters.PagerAdapter
import com.example.hw_urban_diplom_messenger.databinding.ActivityMessengerBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class MessengerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerBinding
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase // статус
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseDatabase = FirebaseDatabase.getInstance() // статус

        setSupportActionBar(binding.toolbar)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val database = FirebaseDatabase.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val userRef = userId?.let { database.getReference("Users").child(it) }
            userId?.let {
                userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        supportActionBar?.title = name
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@MessengerActivity,
                            "error getting information",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }

        pagerAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.visibility = View.VISIBLE
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chats"
                1 -> "Users"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()

        addTokenToDatabase()

        // Установка статуса онлайн в Firebase
        FirebaseAuth.getInstance().currentUser?.let { it1 ->
            firebaseDatabase.getReference("Users").child(it1.uid)
                .child("isOnline").setValue(true)
        }

    }


//    private fun getToken(): String {
//        var token = ""
//        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Log.w("pushNotification", "Fetching FCM registration token failed", task.exception)
//                return@OnCompleteListener
//            }
//
//            token = task.result
//            Log.d("pushNotification", token)
//        })
//        return token
//    }

    private fun addTokenToDatabase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("pushNotification", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
                userReference.child("token").setValue(token)
            }
        }
    }


    override fun onPause() {
        super.onPause()
        // Установка статуса оффлайн в Firebase при сворачивании приложения
        FirebaseAuth.getInstance().currentUser?.let {
            firebaseDatabase.getReference("Users").child(it.uid)
                .child("isOnline").setValue(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, MyProfileActivity::class.java))
                return true
            }

            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
                val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            }

            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.action_exit -> {
                finishAffinity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}