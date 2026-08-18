package com.example.train2gether

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()        // Pagina 0: Home / Storico (A sinistra)
            1 -> AllenamentoFragment() // Pagina 1: Allenamento (A destra)
            else -> HomeFragment()
        }
    }
}