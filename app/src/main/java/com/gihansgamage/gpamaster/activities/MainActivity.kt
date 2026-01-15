package com.gihansgamage.gpamaster.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.fragments.AnalyticsFragment
import com.gihansgamage.gpamaster.fragments.DashboardFragment
import com.gihansgamage.gpamaster.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(DashboardFragment())

        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(DashboardFragment())
                R.id.nav_analytics -> loadFragment(AnalyticsFragment())
                R.id.nav_settings -> loadFragment(SettingsFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
