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
