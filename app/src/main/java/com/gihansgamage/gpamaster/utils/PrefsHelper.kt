package com.gihansgamage.gpamaster.utils

import android.content.Context
import android.content.SharedPreferences

class PrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("gpa_master", Context.MODE_PRIVATE)

    // User Settings
    fun saveUserName(name: String) = prefs.edit().putString("user_name", name).apply()
    fun getUserName(): String = prefs.getString("user_name", "") ?: ""

    fun saveScale(scale: String) = prefs.edit().putString("scale", scale).apply()
    fun getScale(): String = prefs.getString("scale", "4.0") ?: "4.0"

    fun saveYears(years: Int) = prefs.edit().putInt("years", years).apply()
    fun getYears(): Int = prefs.getInt("years", 4)

    fun saveSemestersPerYear(semesters: Int) = prefs.edit().putInt("semesters_per_year", semesters).apply()
    fun getSemestersPerYear(): Int = prefs.getInt("semesters_per_year", 2)

    fun saveSetupCompleted(completed: Boolean) = prefs.edit().putBoolean("setup_completed", completed).apply()
    fun isSetupCompleted(): Boolean = prefs.getBoolean("setup_completed", false)

    // Clear all data
    fun clearAll() = prefs.edit().clear().apply()
}