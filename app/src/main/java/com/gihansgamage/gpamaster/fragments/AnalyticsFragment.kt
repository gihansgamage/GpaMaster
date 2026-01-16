package com.gihansgamage.gpamaster.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.databinding.FragmentSimpleAnalyticsBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper

// Renamed from SimpleAnalyticsFragment to AnalyticsFragment to match MainActivity import
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentSimpleAnalyticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAnalyticsData()
        setupCustomChart()
    }

    private fun loadAnalyticsData() {
        val gpaHistory = listOf(3.2, 3.5, 3.4, 3.7, 3.8, 3.9)
        val creditsHistory = listOf(12.0, 15.0, 14.0, 16.0, 15.0, 16.0)

        val maxGPA = gpaHistory.maxOrNull() ?: 0.0
        val minGPA = gpaHistory.minOrNull() ?: 0.0
        val avgGPA = gpaHistory.average()
        val totalCredits = creditsHistory.sum()

        binding.tvMaxGpa.text = String.format("Max GPA: %.2f", maxGPA)
        binding.tvMinGpa.text = String.format("Min GPA: %.2f", minGPA)
        binding.tvAvgGpa.text = String.format("Average GPA: %.2f", avgGPA)
        binding.tvTotalCredits.text = String.format("Total Credits: %.1f", totalCredits)
        binding.tvSemestersCompleted.text = "Semesters Completed: ${gpaHistory.size}"

        val trend = if (gpaHistory.last() > gpaHistory.first()) {
            "↗ Improving"
        } else if (gpaHistory.last() < gpaHistory.first()) {
            "↘ Declining"
        } else {
            "→ Stable"
        }
        binding.tvTrend.text = "Trend: $trend"
    }

    private fun setupCustomChart() {
        val gpaData = listOf(3.2, 3.5, 3.4, 3.7, 3.8, 3.9)
        val container = binding.chartContainer

        container.removeAllViews()

        val maxGPA = 4.0
        val barWidth = 60.dpToPx()
        val maxBarHeight = 150.dpToPx()

        for ((index, gpa) in gpaData.withIndex()) {
            val barView = createBarView(gpa, maxGPA, index + 1, maxBarHeight, barWidth)
            container.addView(barView)
        }
    }

    private fun createBarView(
        gpa: Double,
        maxGPA: Double,
        semester: Int,
        maxHeight: Int,
        width: Int
    ): View {
        val barHeight = ((gpa / maxGPA) * maxHeight).toInt()

        // Fixed: Changed from TextView to LinearLayout to support addView()
        val barContainer = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val barView = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(width / 2, barHeight)
            setBackgroundColor(getBarColor(gpa))
        }

        val labelView = TextView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "S$semester\n${String.format("%.1f", gpa)}"
            textSize = 10f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(Color.BLACK)
        }

        barContainer.addView(barView)
        barContainer.addView(labelView)

        return barContainer
    }

    private fun getBarColor(gpa: Double): Int {
        return when {
            gpa >= 3.5 -> Color.parseColor("#4CAF50")
            gpa >= 3.0 -> Color.parseColor("#FF9800")
            gpa >= 2.0 -> Color.parseColor("#FFC107")
            else -> Color.parseColor("#F44336")
        }
    }

    private fun Int.dpToPx(): Int {
        val density = resources.displayMetrics.density
        return (this * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}