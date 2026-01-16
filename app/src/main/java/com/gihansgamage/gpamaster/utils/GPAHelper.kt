package com.gihansgamage.gpamaster.utils

import android.content.Context
import android.content.SharedPreferences
import com.gihansgamage.gpamaster.models.Subject

object GPAHelper {

    fun getGradePoints(scale: String): Map<String, Double> {
        return when (scale) {
            "5.0" -> mapOf(
                "A+" to 5.0, "A" to 4.5, "A-" to 4.0,
                "B+" to 3.5, "B" to 3.0, "B-" to 2.5,
                "C+" to 2.0, "C" to 1.5, "C-" to 1.0,
                "D" to 0.5, "F" to 0.0
            )
            "10.0" -> mapOf(
                "A+" to 10.0, "A" to 9.0,
                "B+" to 8.0, "B" to 7.0,
                "C+" to 6.0, "C" to 5.0,
                "D" to 4.0, "F" to 0.0
            )
            "percentage" -> mapOf(
                "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
                "D" to 1.0, "F" to 0.0
            )
            else -> mapOf( // 4.0 scale
                "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
                "D" to 1.0, "F" to 0.0
            )
        }
    }

    fun calculateGPA(subjects: List<Subject>, scale: String): Pair<Double, Double> {
        if (subjects.isEmpty()) return Pair(0.0, 0.0)

        val gradePoints = getGradePoints(scale)
        var totalPoints = 0.0
        var totalCredits = 0.0

        subjects.forEach { subject ->
            val gradePoint = gradePoints[subject.grade] ?: 0.0
            totalPoints += gradePoint * subject.credits
            totalCredits += subject.credits
        }

        val gpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        return Pair(gpa, totalCredits)
    }
}