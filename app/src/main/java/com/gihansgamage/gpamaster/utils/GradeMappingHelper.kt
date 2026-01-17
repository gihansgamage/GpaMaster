package com.gihansgamage.gpamaster.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object GradeMappingHelper {

    /**
     * Get standard default grade mappings for a given scale
     */
    private fun getStandardMappings(scale: String): Map<String, Double> {
        return when (scale) {
            "4.0" -> mapOf(
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
            "5.0" -> mapOf(
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
            "10.0" -> mapOf(
                "A+" to 10.0,
                "A" to 9.0,
                "B+" to 8.0,
                "B" to 7.0,
                "C+" to 6.0,
                "C" to 5.0,
                "D" to 4.0,
                "F" to 0.0
            )
            "percentage" -> mapOf(
                "A+" to 100.0,
                "A" to 90.0,
                "B+" to 80.0,
                "B" to 70.0,
                "C" to 60.0,
                "D" to 50.0,
                "F" to 30.0
            )
            else -> mapOf(
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
    }

    /**
     * Get grade mappings for the current scale
     * Returns custom mappings if available, otherwise standard mappings
     */
    fun getGradeMappings(context: Context, scale: String): Map<String, Double> {
        val prefs = PrefsHelper(context)
        val json = prefs.getString("grade_mapping_$scale", "")

        if (json.isEmpty()) {
            return getStandardMappings(scale)
        }

        val type = object : TypeToken<Map<String, Double>>() {}.type
        val customMappings: Map<String, Double>? = Gson().fromJson(json, type)

        return customMappings ?: getStandardMappings(scale)
    }

    /**
     * Save custom grade mappings for a specific scale
     */
    fun saveGradeMappings(context: Context, scale: String, mappings: Map<String, Double>) {
        val prefs = PrefsHelper(context)
        val json = Gson().toJson(mappings)
        prefs.saveString("grade_mapping_$scale", json)
    }

    /**
     * Reset grade mappings to standard defaults for a specific scale
     */
    fun resetToDefaults(context: Context, scale: String) {
        val prefs = PrefsHelper(context)
        prefs.saveString("grade_mapping_$scale", "")
    }

    /**
     * Check if custom mappings exist for a scale
     */
    fun hasCustomMappings(context: Context, scale: String): Boolean {
        val prefs = PrefsHelper(context)
        val json = prefs.getString("grade_mapping_$scale", "")
        return json.isNotEmpty()
    }

    /**
     * Clear all custom mappings
     */
    fun clearCustomMappings(context: Context) {
        val prefs = PrefsHelper(context)
        val scales = listOf("4.0", "5.0", "10.0", "percentage")
        scales.forEach { scale ->
            prefs.saveString("grade_mapping_$scale", "")
        }
    }

    /**
     * Get available grades for a scale (in the correct order)
     */
    fun getAvailableGrades(scale: String): List<String> {
        return getStandardMappings(scale).keys.toList()
    }

    /**
     * Get grade points for a specific grade in the current scale
     */
    fun getGradePoints(context: Context, scale: String, grade: String): Double {
        val mappings = getGradeMappings(context, scale)
        return mappings[grade] ?: 0.0
    }
}