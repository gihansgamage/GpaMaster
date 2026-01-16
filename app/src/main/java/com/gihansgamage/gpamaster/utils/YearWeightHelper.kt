package com.gihansgamage.gpamaster.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object YearWeightHelper {

    /**
     * Get year weights from preferences
     * Returns a map of year number to percentage weight
     */
    fun getYearWeights(context: Context): Map<Int, Double> {
        val prefs = PrefsHelper(context)
        val json = prefs.getString("year_weights", "")

        if (json.isEmpty()) {
            // Return default equal weights based on number of years
            return getDefaultWeights(prefs.getYears())
        }

        val type = object : TypeToken<Map<Int, Double>>() {}.type
        val weights: Map<Int, Double>? = Gson().fromJson(json, type)

        // Validate weights
        return if (weights != null && isValidWeights(weights, prefs.getYears())) {
            weights
        } else {
            getDefaultWeights(prefs.getYears())
        }
    }

    /**
     * Save year weights to preferences
     */
    fun saveYearWeights(context: Context, weights: Map<Int, Double>) {
        val prefs = PrefsHelper(context)
        val json = Gson().toJson(weights)
        prefs.saveString("year_weights", json)
    }

    /**
     * Get default equal weights for given number of years
     */
    fun getDefaultWeights(numYears: Int): Map<Int, Double> {
        val equalWeight = 100.0 / numYears
        return (1..numYears).associateWith { equalWeight }
    }

    /**
     * Get common preset weights based on number of years
     */
    fun getPresetWeights(numYears: Int): Map<String, Map<Int, Double>> {
        return when (numYears) {
            3 -> mapOf(
                "Equal (33.33% each)" to mapOf(1 to 33.33, 2 to 33.33, 3 to 33.34),
                "Progressive (20-30-50%)" to mapOf(1 to 20.0, 2 to 30.0, 3 to 50.0),
                "UK System (0-33-67%)" to mapOf(1 to 0.0, 2 to 33.33, 3 to 66.67)
            )
            4 -> mapOf(
                "Equal (25% each)" to mapOf(1 to 25.0, 2 to 25.0, 3 to 25.0, 4 to 25.0),
                "Progressive (10-20-30-40%)" to mapOf(1 to 10.0, 2 to 20.0, 3 to 30.0, 4 to 40.0),
                "US System (20-20-30-30%)" to mapOf(1 to 20.0, 2 to 20.0, 3 to 30.0, 4 to 30.0),
                "UK System (0-20-30-50%)" to mapOf(1 to 0.0, 2 to 20.0, 3 to 30.0, 4 to 50.0)
            )
            5 -> mapOf(
                "Equal (20% each)" to mapOf(1 to 20.0, 2 to 20.0, 3 to 20.0, 4 to 20.0, 5 to 20.0),
                "Progressive" to mapOf(1 to 10.0, 2 to 15.0, 3 to 20.0, 4 to 25.0, 5 to 30.0)
            )
            else -> mapOf(
                "Equal" to getDefaultWeights(numYears)
            )
        }
    }

    /**
     * Validate that weights sum to 100 and cover all years
     */
    private fun isValidWeights(weights: Map<Int, Double>, numYears: Int): Boolean {
        // Check all years are present
        if (weights.size != numYears) return false
        for (year in 1..numYears) {
            if (!weights.containsKey(year)) return false
        }

        // Check sum is approximately 100
        val sum = weights.values.sum()
        return sum in 99.9..100.1
    }

    /**
     * Calculate weighted GPA based on year weights
     */
    fun calculateWeightedGPA(
        context: Context,
        semesterManager: SemesterManager
    ): Pair<Double, Double> {
        val prefs = PrefsHelper(context)
        val years = prefs.getYears()
        val scale = prefs.getScale()
        val weights = getYearWeights(context)

        var weightedGPASum = 0.0
        var totalWeight = 0.0

        // Calculate GPA for each year
        for (year in 1..years) {
            val yearGPA = calculateYearGPA(context, semesterManager, year, scale)
            val weight = weights[year] ?: 0.0

            // Only include years with data
            if (yearGPA > 0.0 && weight > 0.0) {
                weightedGPASum += yearGPA * (weight / 100.0)
                totalWeight += weight
            }
        }

        val normalizedWeight = if (totalWeight > 0) 100.0 / totalWeight else 0.0
        val weightedGPA = if (totalWeight > 0) weightedGPASum * normalizedWeight else 0.0

        return Pair(weightedGPA, totalWeight)
    }

    /**
     * Calculate GPA for a specific year
     */
    private fun calculateYearGPA(
        context: Context,
        semesterManager: SemesterManager,
        year: Int,
        scale: String
    ): Double {
        val prefs = PrefsHelper(context)
        val semestersPerYear = prefs.getSemestersPerYear()

        var totalPoints = 0.0
        var totalCredits = 0.0

        for (semesterNum in 1..semestersPerYear) {
            val semester = semesterManager.getOrCreateSemester(year, semesterNum)
            val subjects = semesterManager.getSubjectsForSemester(semester.id)

            subjects.forEach { subject ->
                val gradePoints = GPAHelper.getGradePoints(scale)[subject.grade] ?: 0.0
                totalPoints += gradePoints * subject.credits
                totalCredits += subject.credits
            }
        }

        return if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    /**
     * Update weights when number of years changes
     */
    fun updateWeightsForYearChange(context: Context, newNumYears: Int) {
        val currentWeights = getYearWeights(context)
        val oldNumYears = currentWeights.size

        if (oldNumYears == newNumYears) return

        // Create new weights
        val newWeights = mutableMapOf<Int, Double>()

        if (newNumYears > oldNumYears) {
            // Adding years - keep existing weights and add equal weight for new years
            val existingTotal = currentWeights.values.sum()
            val remainingPercentage = 100.0 - existingTotal
            val newYearWeight = if (remainingPercentage > 0) {
                remainingPercentage / (newNumYears - oldNumYears)
            } else {
                // Redistribute equally
                100.0 / newNumYears
            }

            for (year in 1..newNumYears) {
                newWeights[year] = if (year <= oldNumYears) {
                    if (remainingPercentage > 0) currentWeights[year] ?: 0.0
                    else 100.0 / newNumYears
                } else {
                    newYearWeight
                }
            }
        } else {
            // Removing years - redistribute proportionally
            val keptYears = (1..newNumYears).toList()
            val totalKeptWeight = keptYears.sumOf { currentWeights[it] ?: 0.0 }

            if (totalKeptWeight > 0) {
                val scaleFactor = 100.0 / totalKeptWeight
                for (year in 1..newNumYears) {
                    newWeights[year] = (currentWeights[year] ?: 0.0) * scaleFactor
                }
            } else {
                // Default to equal weights
                return saveYearWeights(context, getDefaultWeights(newNumYears))
            }
        }

        saveYearWeights(context, newWeights)
    }
}