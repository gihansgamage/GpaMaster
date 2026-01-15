package com.gihansgamage.gpamaster.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "semesters")
data class Semester(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val year: Int,
    val semesterNumber: Int,
    var gpa: Double = 0.0,
    var totalCredits: Double = 0.0,
    val isActive: Boolean = true
) {
    fun getDisplayName(): String {
        return "Year $year - Semester $semesterNumber"
    }

    fun getShortName(): String {
        return "Y${year}S${semesterNumber}"
    }
}