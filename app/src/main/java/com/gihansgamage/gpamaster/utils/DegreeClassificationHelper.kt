package com.gihansgamage.gpamaster.utils

import com.gihansgamage.gpamaster.views.GpaScaleBarView

object DegreeClassificationHelper {

    data class Classification(
        val title: String,
        val emoji: String,
        val message: String,
        val color: Int
    )

    fun getClassification(gpa: Double, scale: String): Classification {
        return when (scale) {
            "4.0" -> getClassification4Scale(gpa)
            "5.0" -> getClassification5Scale(gpa)
            "10.0" -> getClassification10Scale(gpa)
            "percentage" -> getClassificationPercentage(gpa)
            else -> getClassification4Scale(gpa)
        }
    }

    /**
     * Returns the ordered list of classification bands for use in [GpaScaleBarView].
     * Bands are ordered from lowest (index 0) to highest GPA.
     */
    fun getBands(scale: String): List<GpaScaleBarView.Band> {
        return when (scale) {
            "4.0" -> listOf(
                GpaScaleBarView.Band(0.0, 2.0,  android.graphics.Color.parseColor("#F44336"), "Below Pass",              "Fail"),
                GpaScaleBarView.Band(2.0, 3.0,  android.graphics.Color.parseColor("#FFC107"), "General Degree",          "Gen."),
                GpaScaleBarView.Band(3.0, 3.30, android.graphics.Color.parseColor("#FF9800"), "Second Class (Lower) 2:2","2:2"),
                GpaScaleBarView.Band(3.30, 3.70,android.graphics.Color.parseColor("#8BC34A"), "Second Class (Upper) 2:1","2:1"),
                GpaScaleBarView.Band(3.70, 4.0, android.graphics.Color.parseColor("#4CAF50"), "First Class Honours",     "1st")
            )
            "5.0" -> listOf(
                GpaScaleBarView.Band(0.0, 2.5,  android.graphics.Color.parseColor("#F44336"), "Below Pass",              "Fail"),
                GpaScaleBarView.Band(2.5, 3.5,  android.graphics.Color.parseColor("#FFC107"), "General Degree",          "Gen."),
                GpaScaleBarView.Band(3.5, 4.0,  android.graphics.Color.parseColor("#FF9800"), "Second Class (Lower) 2:2","2:2"),
                GpaScaleBarView.Band(4.0, 4.5,  android.graphics.Color.parseColor("#8BC34A"), "Second Class (Upper) 2:1","2:1"),
                GpaScaleBarView.Band(4.5, 5.0,  android.graphics.Color.parseColor("#4CAF50"), "First Class Honours",     "1st")
            )
            "10.0" -> listOf(
                GpaScaleBarView.Band(0.0, 4.0,  android.graphics.Color.parseColor("#F44336"), "Below Pass",              "Fail"),
                GpaScaleBarView.Band(4.0, 5.0,  android.graphics.Color.parseColor("#FFC107"), "General Degree",          "Gen."),
                GpaScaleBarView.Band(5.0, 6.0,  android.graphics.Color.parseColor("#FF9800"), "Second Class (Lower) 2:2","2:2"),
                GpaScaleBarView.Band(6.0, 7.0,  android.graphics.Color.parseColor("#8BC34A"), "Second Class (Upper) 2:1","2:1"),
                GpaScaleBarView.Band(7.0, 10.0, android.graphics.Color.parseColor("#4CAF50"), "First Class Honours",     "1st")
            )
            "percentage" -> listOf(
                GpaScaleBarView.Band(0.0,  40.0, android.graphics.Color.parseColor("#F44336"), "Below Pass",              "Fail"),
                GpaScaleBarView.Band(40.0, 50.0, android.graphics.Color.parseColor("#FFC107"), "General Degree",          "Gen."),
                GpaScaleBarView.Band(50.0, 60.0, android.graphics.Color.parseColor("#FF9800"), "Second Class (Lower) 2:2","2:2"),
                GpaScaleBarView.Band(60.0, 70.0, android.graphics.Color.parseColor("#8BC34A"), "Second Class (Upper) 2:1","2:1"),
                GpaScaleBarView.Band(70.0,100.0, android.graphics.Color.parseColor("#4CAF50"), "First Class Honours",     "1st")
            )
            else -> getBands("4.0")
        }
    }

    /** Max value for the given scale (used to size the bar). */
    fun getMaxScale(scale: String): Double = when (scale) {
        "4.0" -> 4.0
        "5.0" -> 5.0
        "10.0" -> 10.0
        "percentage" -> 100.0
        else -> 4.0
    }

    private fun getClassification4Scale(gpa: Double): Classification {
        return when {
            gpa >= 3.70 -> Classification(
                title = "First Class Honours",
                emoji = "🏆",
                message = "Outstanding academic performance. You are achieving at the highest level 🏆",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 3.30 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent results with strong consistency. Keep pushing forward ⭐",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 3.00 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good academic standing with room for improvement 📚",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 2.00 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You have met the minimum requirements. Focus on strengthening weak areas 📖",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This result does not define you. With effort and guidance, improvement is possible 💪",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassification5Scale(gpa: Double): Classification {
        return when {
            gpa >= 4.50 -> Classification(
                title = "First Class Honours",
                emoji = "🏆",
                message = "Outstanding academic performance. You are achieving at the highest level 🏆",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 4.00 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent results with strong consistency. Keep pushing forward ⭐",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 3.50 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good academic standing with room for improvement 📚",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 2.50 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You have met the minimum requirements. Focus on strengthening weak areas 📖",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This result does not define you. With effort and guidance, improvement is possible 💪",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassification10Scale(gpa: Double): Classification {
        return when {
            gpa >= 7.0 -> Classification(
                title = "First Class Honours",
                emoji = "🏆",
                message = "Outstanding academic performance. You are achieving at the highest level 🏆",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 6.0 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent results with strong consistency. Keep pushing forward ⭐",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 5.0 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good academic standing with room for improvement 📚",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 4.0 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You have met the minimum requirements. Focus on strengthening weak areas 📖",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This result does not define you. With effort and guidance, improvement is possible 💪",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassificationPercentage(percentage: Double): Classification {
        return when {
            percentage >= 70.0 -> Classification(
                title = "First Class Honours",
                emoji = "🏆",
                message = "Outstanding academic performance. You are achieving at the highest level 🏆",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            percentage >= 60.0 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent results with strong consistency. Keep pushing forward ⭐",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            percentage >= 50.0 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good academic standing with room for improvement 📚",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            percentage >= 40.0 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You have met the minimum requirements. Focus on strengthening weak areas 📖",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This result does not define you. With effort and guidance, improvement is possible 💪",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }
}

