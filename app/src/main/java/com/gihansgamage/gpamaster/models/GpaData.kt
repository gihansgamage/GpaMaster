package com.gihansgamage.gpamaster.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val scale: String = "4.0",
    val totalYears: Int = 4,
    val semestersPerYear: Int = 2
)

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val year: Int,
    val semesterNumber: Int,
    val gpa: Double = 0.0,
    val totalCredits: Double = 0.0
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val semesterId: Int,
    val name: String,
    val credits: Double,
    val grade: String
)