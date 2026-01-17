package com.gihansgamage.gpamaster.utils

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
