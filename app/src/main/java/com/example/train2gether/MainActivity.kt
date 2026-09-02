package com.example.train2gether

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

// L'activity principale che gestisce globalmente l'app tramite ViewPager e BottomNavigationView
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Abilitiamo l'edge to edge per coprire tutto lo schermo
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Inizializzazione dei componenti della schermata
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Inizializzazione dell'adapter per gestire i fragment
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        // Gestione della barra di navigazione per passare dalla home alla schermata di allenamento
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0 // Qui spostiamo alla home
                    true
                }
                R.id.nav_allenamento -> {
                    viewPager.currentItem = 1 // Qui spostiamo all'allenamento
                    true
                }
                else -> false
            }
        }

        // Sincronizzazione dello scorrimento del ViewPager con la visualizzazione effettiva
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }
}