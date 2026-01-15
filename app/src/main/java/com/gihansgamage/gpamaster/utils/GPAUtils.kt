package com.gihansgamage.gpamaster.utils

import com.gihansgamage.gpamaster.models.Subject

object GPAUtils {

    fun getGradePoint(grade: String, scale: String): Double {
        return when (scale) {
            "4.0" -> mapOf(
                "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
                "D" to 1.0, "F" to 0.0
            )[grade] ?: 0.0

            "5.0" -> mapOf(
                "A+" to 5.0, "A" to 4.5, "A-" to 4.0,
                "B+" to 3.5, "B" to 3.0, "B-" to 2.5,
                "C+" to 2.0, "C" to 1.5, "C-" to 1.0,
                "D" to 0.5, "F" to 0.0
            )[grade] ?: 0.0

            else -> 0.0
        }
    }

    fun calculateGPA(subjects: List<Subject>, scale: String): Double {
        var totalPoints = 0.0
        var totalCredits = 0

        for (s in subjects) {
            totalPoints += getGradePoint(s.grade, scale) * s.credits
            totalCredits += s.credits
        }
        return if (totalCredits == 0) 0.0 else totalPoints / totalCredits
    }
}
