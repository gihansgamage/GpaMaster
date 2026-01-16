package com.gihansgamage.gpamaster.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val semesterId: Int,
    val name: String,
    val credits: Double,
    val grade: String,
    val percentage: Double? = null, // Ensure this field exists
    val createdAt: Long = System.currentTimeMillis()
)