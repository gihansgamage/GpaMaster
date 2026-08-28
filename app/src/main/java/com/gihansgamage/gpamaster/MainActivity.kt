package com.gihansgamage.gpamaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.utils.AdHelper
import com.gihansgamage.gpamaster.databinding.ActivityMainBinding
import com.gihansgamage.gpamaster.fragments.HomeFragment
import com.gihansgamage.gpamaster.fragments.SemestersFragment
import com.gihansgamage.gpamaster.fragments.SettingsFragment
import com.gihansgamage.gpamaster.utils.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Google Mobile Ads SDK via AdHelper
        AdHelper.initialize(this)

        prefs = PrefsHelper(this)

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_semesters -> {
                    loadFragment(SemestersFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}