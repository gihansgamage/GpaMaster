package com.gihansgamage.gpamaster.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_data")
data class UserData(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val scale: String, // "4.0", "5.0", "10.0", "percentage"
    val totalYears: Int,
    val semestersPerYear: Int,
    val totalCredits: Double = 0.0,
    val overallGPA: Double = 0.0,
    val setupCompleted: Boolean = false
)