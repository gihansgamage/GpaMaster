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
import com.gihansgamage.gpamaster.databinding.FragmentSimpleAnalyticsBinding
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentSimpleAnalyticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimpleAnalyticsBinding.inflate(inflater, container, false)
        prefs = PrefsHelper(requireContext())
        semesterManager = SemesterManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAnalyticsData()
        setupCustomChart()
    }

    override fun onResume() {
        super.onResume()
        loadAnalyticsData()
        setupCustomChart()
    }

    private fun loadAnalyticsData() {
        val scale = prefs.getScale()
        val allSemesters = semesterManager.getAllSemesters()
            .filter { it.totalCredits > 0 }
            .sortedWith(compareBy({ it.year }, { it.semesterNumber }))

        if (allSemesters.isEmpty()) {
            binding.tvMaxGpa.text = "Max GPA: N/A"
            binding.tvMinGpa.text = "Min GPA: N/A"
            binding.tvAvgGpa.text = "Average GPA: N/A"
            binding.tvTotalCredits.text = "Total Credits: 0"
            binding.tvSemestersCompleted.text = "Semesters Completed: 0"
            binding.tvTrend.text = "Trend: N/A"
            return
        }

        val gpaHistory = allSemesters.map { it.gpa }
        val creditsHistory = allSemesters.map { it.totalCredits }

        val maxGPA = gpaHistory.maxOrNull() ?: 0.0
        val minGPA = gpaHistory.minOrNull() ?: 0.0
        val avgGPA = gpaHistory.average()
        val totalCredits = creditsHistory.sum()

        binding.tvMaxGpa.text = "Max GPA: ${GPAHelper.formatGPA(maxGPA, scale)}"
        binding.tvMinGpa.text = "Min GPA: ${GPAHelper.formatGPA(minGPA, scale)}"
        binding.tvAvgGpa.text = "Average GPA: ${GPAHelper.formatGPA(avgGPA, scale)}"
        binding.tvTotalCredits.text = "Total Credits: ${totalCredits.toInt()}"
        binding.tvSemestersCompleted.text = "Semesters Completed: ${gpaHistory.size}"

        // Calculate trend
        val trend = if (gpaHistory.size >= 2) {
            val firstGPA = gpaHistory.first()
            val lastGPA = gpaHistory.last()
            when {
                lastGPA > firstGPA + 0.1 -> "↗ Improving"
                lastGPA < firstGPA - 0.1 -> "↘ Declining"
                else -> "→ Stable"
            }
        } else {
            "→ Stable"
        }
        binding.tvTrend.text = "Trend: $trend"
    }

    private fun setupCustomChart() {
        val scale = prefs.getScale()
        val maxScale = when (scale) {
            "4.0" -> 4.0
            "5.0" -> 5.0
            "10.0" -> 10.0
            else -> 4.0
        }

        val allSemesters = semesterManager.getAllSemesters()
            .filter { it.totalCredits > 0 }
            .sortedWith(compareBy({ it.year }, { it.semesterNumber }))

        val container = binding.chartContainer
        container.removeAllViews()

        if (allSemesters.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No data to display yet"
                textSize = 16f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(32, 32, 32, 32)
            }
            container.addView(emptyText)
            return
        }

        val barWidth = 80.dpToPx()
        val maxBarHeight = 150.dpToPx()

        for (semester in allSemesters) {
            val gpa = semester.gpa
            val label = "Y${semester.year}S${semester.semesterNumber}"

            val barView = createBarView(gpa, maxScale, label, maxBarHeight, barWidth)
            container.addView(barView)
        }
    }

    private fun createBarView(
        gpa: Double,
        maxGPA: Double,
        label: String,
        maxHeight: Int,
        width: Int
    ): View {
        val barHeight = ((gpa / maxGPA) * maxHeight).toInt().coerceAtLeast(20)

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
            text = "$label\n${String.format("%.2f", gpa)}"
            textSize = 10f
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(Color.BLACK)
            setPadding(4, 8, 4, 4)
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