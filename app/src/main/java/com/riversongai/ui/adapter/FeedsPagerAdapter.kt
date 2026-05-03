package com.riversongai.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.riversongai.ui.NewsFragment
import com.riversongai.ui.WeatherFragment
import com.riversongai.ui.StocksFragment
import com.riversongai.ui.SportsFragment

class FeedsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 4
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NewsFragment()
            1 -> WeatherFragment()
            2 -> StocksFragment()
            3 -> SportsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
