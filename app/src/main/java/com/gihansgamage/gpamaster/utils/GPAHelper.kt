package com.gihansgamage.gpamaster.utils

import android.content.Context
import com.gihansgamage.gpamaster.models.Subject

object GPAHelper {

    /**
     * Get grade points using custom or default mappings
     */
    fun getGradePoints(scale: String): Map<String, Double> {
        // This method is kept for backward compatibility
        // But now it returns standard mappings
        return when (scale) {
            "4.0" -> mapOf(
                "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
                "D" to 1.0, "F" to 0.0
            )
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
            else -> mapOf(
                "A+" to 4.0, "A" to 4.0, "A-" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B-" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C-" to 1.7,
                "D" to 1.0, "F" to 0.0
            )
        }
    }

    /**
     * Get grade points with context for custom mappings
     */
    fun getGradePointsWithContext(context: Context, scale: String): Map<String, Double> {
        return GradeMappingHelper.getGradeMappings(context, scale)
    }

    fun getAvailableGrades(scale: String): List<String> = getGradePoints(scale).keys.toList()

    fun calculateSemesterGPA(subjects: List<Subject>, scale: String): Pair<Double, Double> {
        if (subjects.isEmpty()) return Pair(0.0, 0.0)
        val pointsMap = getGradePoints(scale)
        var totalPoints = 0.0
        var totalCredits = 0.0
        subjects.forEach {
            totalPoints += (pointsMap[it.grade] ?: 0.0) * it.credits
            totalCredits += it.credits
        }
        return Pair(if (totalCredits > 0) totalPoints / totalCredits else 0.0, totalCredits)
    }

    /**
     * Calculate semester GPA with custom mappings
     */
    fun calculateSemesterGPAWithContext(
        context: Context,
        subjects: List<Subject>,
        scale: String
    ): Pair<Double, Double> {
        if (subjects.isEmpty()) return Pair(0.0, 0.0)
        val pointsMap = GradeMappingHelper.getGradeMappings(context, scale)
        var totalPoints = 0.0
        var totalCredits = 0.0
        subjects.forEach {
            totalPoints += (pointsMap[it.grade] ?: 0.0) * it.credits
            totalCredits += it.credits
        }
        return Pair(if (totalCredits > 0) totalPoints / totalCredits else 0.0, totalCredits)
    }

    fun formatGPA(gpa: Double, scale: String): String = "%.2f".format(gpa)
}