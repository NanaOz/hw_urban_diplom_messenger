package com.example.hw_urban_diplom_messenger.adapters

import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.hw_urban_diplom_messenger.fragments.ChatsFragment
import com.example.hw_urban_diplom_messenger.fragments.UsersFragment

class PagerAdapter(fm: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle){

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ChatsFragment()
            1 -> UsersFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
