package com.gihansgamage.gpamaster.models

// Make sure you are in the correct file, likely Subject.kt
data class Subject(
    val id: Long = 0, // Keep the id if you have it
    val semesterId: Int,
    val name: String,
    val credits: Double,
    val grade: String,
    val percentage: Double? = null // Add this line
)
