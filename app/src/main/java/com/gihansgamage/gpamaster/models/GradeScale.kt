package com.gihansgamage.gpamaster.models

sealed class GradeScale(val name: String) {
    data object Scale4 : GradeScale("4.0 Scale") {
        val gradePoints = mapOf(
            "A+" to 4.0,
            "A" to 4.0,
            "A-" to 3.7,
            "B+" to 3.3,
            "B" to 3.0,
            "B-" to 2.7,
            "C+" to 2.3,
            "C" to 2.0,
            "C-" to 1.7,
            "D" to 1.0,
            "F" to 0.0
        )
    }

    data object Scale5 : GradeScale("5.0 Scale") {
        val gradePoints = mapOf(
            "A+" to 5.0,
            "A" to 4.5,
            "A-" to 4.0,
            "B+" to 3.5,
            "B" to 3.0,
            "B-" to 2.5,
            "C+" to 2.0,
            "C" to 1.5,
            "C-" to 1.0,
            "D" to 0.5,
            "F" to 0.0
        )
    }

    data object Scale10 : GradeScale("10.0 Scale") {
        val gradePoints = mapOf(
            "A+" to 10.0,
            "A" to 9.0,
            "B+" to 8.0,
            "B" to 7.0,
            "C+" to 6.0,
            "C" to 5.0,
            "D" to 4.0,
            "F" to 0.0
        )
    }

    data object Percentage : GradeScale("Percentage") {
        val percentageRanges = mapOf(
            "A+" to 85.0..100.0,
            "A" to 85.0..100.0,
            "B+" to 75.0..84.0,
            "B" to 65.0..74.0,
            "C" to 55.0..64.0,
            "D" to 40.0..54.0,
            "F" to 0.0..39.0
        )

        fun getGrade(percentage: Double): String {
            return when {
                percentage >= 85 -> "A"
                percentage >= 75 -> "B+"
                percentage >= 65 -> "B"
                percentage >= 55 -> "C"
                percentage >= 40 -> "D"
                else -> "F"
            }
        }
    }
}