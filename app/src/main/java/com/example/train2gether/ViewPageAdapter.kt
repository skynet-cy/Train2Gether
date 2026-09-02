package com.example.train2gether

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Adapter per gestire le schermate agganciate al ViewPager2 della MainActivity
class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Restituisce il numero totale di fragment gestiti dal pager
    override fun getItemCount(): Int = 2

    // Crea e restituisce il fragment corrispondente alla posizione numerica
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()      // Home
            1 -> AllenamentoFragment() // Allenamento
            else -> HomeFragment()     // Else di sicurezza che restituisce la Home in caso di indici non previsti
        }
    }
}