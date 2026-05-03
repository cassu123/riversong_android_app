package com.riversongai.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.riversongai.ui.MemoryFactsFragment
import com.riversongai.ui.MemoryPreferencesFragment
import com.riversongai.ui.MemorySummariesFragment

class MemoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MemoryFactsFragment()
            1 -> MemoryPreferencesFragment()
            2 -> MemorySummariesFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
