package com.example.hw_urban_diplom_messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.hw_urban_diplom_messenger.databinding.ActivityMessengerBinding

class MessengerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessengerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
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
            R.id.action_about -> {
                //TODO
                finishAffinity()
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