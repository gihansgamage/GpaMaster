package com.gihansgamage.gpamaster.utils

object DegreeClassificationHelper {

    data class Classification(
        val title: String,
        val emoji: String,
        val message: String,
        val color: Int
    )

    /**
     * Get degree classification based on GPA and scale
     */
    fun getClassification(gpa: Double, scale: String): Classification {
        return when (scale) {
            "4.0" -> getClassification4Scale(gpa)
            "5.0" -> getClassification5Scale(gpa)
            "10.0" -> getClassification10Scale(gpa)
            "percentage" -> getClassificationPercentage(gpa)
            else -> getClassification4Scale(gpa)
        }
    }

    private fun getClassification4Scale(gpa: Double): Classification {
        return when {
            gpa >= 3.70 -> Classification(
                title = "First Class Honours 🎓",
                emoji = "🏆",
                message = "Outstanding! You're achieving the highest academic standard. Keep up this exceptional work!",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 3.30 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent work! You're performing very well. A little more effort could push you to First Class!",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 3.00 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good progress! You're on solid ground. Focus on key subjects to improve your standing.",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 2.00 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You're making progress, but there's room for improvement. Don't give up - you can do this!",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This is challenging, but not impossible. Seek help, study harder, and believe in yourself!",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassification5Scale(gpa: Double): Classification {
        return when {
            gpa >= 4.50 -> Classification(
                title = "First Class Honours 🎓",
                emoji = "🏆",
                message = "Outstanding! You're achieving the highest academic standard. Keep up this exceptional work!",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 4.00 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent work! You're performing very well. A little more effort could push you to First Class!",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 3.50 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good progress! You're on solid ground. Focus on key subjects to improve your standing.",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 2.50 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You're making progress, but there's room for improvement. Don't give up - you can do this!",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This is challenging, but not impossible. Seek help, study harder, and believe in yourself!",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassification10Scale(gpa: Double): Classification {
        return when {
            gpa >= 7.0 -> Classification(
                title = "First Class Honours 🎓",
                emoji = "🏆",
                message = "Outstanding! You're achieving the highest academic standard. Keep up this exceptional work!",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            gpa >= 6.0 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent work! You're performing very well. A little more effort could push you to First Class!",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            gpa >= 5.0 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good progress! You're on solid ground. Focus on key subjects to improve your standing.",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            gpa >= 4.0 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You're making progress, but there's room for improvement. Don't give up - you can do this!",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This is challenging, but not impossible. Seek help, study harder, and believe in yourself!",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    private fun getClassificationPercentage(percentage: Double): Classification {
        return when {
            percentage >= 70.0 -> Classification(
                title = "First Class Honours 🎓",
                emoji = "🏆",
                message = "Outstanding! You're achieving the highest academic standard. Keep up this exceptional work!",
                color = android.graphics.Color.parseColor("#4CAF50")
            )
            percentage >= 60.0 -> Classification(
                title = "Second Class (Upper Division) 2:1",
                emoji = "⭐",
                message = "Excellent work! You're performing very well. A little more effort could push you to First Class!",
                color = android.graphics.Color.parseColor("#8BC34A")
            )
            percentage >= 50.0 -> Classification(
                title = "Second Class (Lower Division) 2:2",
                emoji = "📚",
                message = "Good progress! You're on solid ground. Focus on key subjects to improve your standing.",
                color = android.graphics.Color.parseColor("#FF9800")
            )
            percentage >= 40.0 -> Classification(
                title = "General Degree",
                emoji = "📖",
                message = "You're making progress, but there's room for improvement. Don't give up - you can do this!",
                color = android.graphics.Color.parseColor("#FFC107")
            )
            else -> Classification(
                title = "Below Pass Standard",
                emoji = "💪",
                message = "This is challenging, but not impossible. Seek help, study harder, and believe in yourself!",
                color = android.graphics.Color.parseColor("#F44336")
            )
        }
    }

    /**
     * Get detailed classification ranges for current scale
     */
    fun getClassificationRanges(scale: String): List<Pair<String, String>> {
        return when (scale) {
            "4.0" -> listOf(
                "First Class" to "3.70 – 4.00",
                "Second Class (Upper) 2:1" to "3.30 – 3.69",
                "Second Class (Lower) 2:2" to "3.00 – 3.29",
                "General Degree" to "2.00 – 2.99",
                "Fail" to "< 2.00"
            )
            "5.0" -> listOf(
                "First Class" to "4.50 – 5.00",
                "Second Class (Upper) 2:1" to "4.00 – 4.49",
                "Second Class (Lower) 2:2" to "3.50 – 3.99",
                "General Degree" to "2.50 – 3.49",
                "Fail" to "< 2.50"
            )
            "10.0" -> listOf(
                "First Class" to "7.0 – 10.0",
                "Second Class (Upper) 2:1" to "6.0 – 6.9",
                "Second Class (Lower) 2:2" to "5.0 – 5.9",
                "General Degree" to "4.0 – 4.9",
                "Fail" to "< 4.0"
            )
            "percentage" -> listOf(
                "First Class" to "≥ 70%",
                "Second Class (Upper) 2:1" to "60% – 69%",
                "Second Class (Lower) 2:2" to "50% – 59%",
                "General Degree" to "40% – 49%",
                "Fail" to "< 40%"
            )
            else -> emptyList()
        }
    }
}