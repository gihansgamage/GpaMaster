package com.gihansgamage.gpamaster.utils

import android.content.Context

class PrefManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("GPA_MASTER", Context.MODE_PRIVATE)

    fun isFirstTime() = prefs.getBoolean("FIRST_TIME", true)
    fun setNotFirstTime() = prefs.edit().putBoolean("FIRST_TIME", false).apply()

    fun saveUser(name: String, scale: String, years: Int, semesters: Int) {
        prefs.edit().apply {
            putString("NAME", name)
            putString("SCALE", scale)
            putInt("YEARS", years)
            putInt("SEMESTERS", semesters)
            apply()
        }
    }

    fun getName() = prefs.getString("NAME", "") ?: ""
    fun getScale() = prefs.getString("SCALE", "4.0") ?: "4.0"
    fun getYears() = prefs.getInt("YEARS", 1)
    fun getSemesters() = prefs.getInt("SEMESTERS", 2)
}
