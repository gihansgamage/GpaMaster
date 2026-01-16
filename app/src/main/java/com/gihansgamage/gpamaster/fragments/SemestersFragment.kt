package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.SemesterDetailActivity
import com.gihansgamage.gpamaster.databinding.FragmentSemestersBinding
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager

class SemestersFragment : Fragment() {
    private var _binding: FragmentSemestersBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemestersBinding.inflate(inflater, container, false)
        prefs = PrefsHelper(requireContext())
        semesterManager = SemesterManager(requireContext())

        // Initialize semesters if needed
        semesterManager.initializeSemesters()

        generateSemesterButtons()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        generateSemesterButtons()
    }

    private fun generateSemesterButtons() {
        binding.semesterContainer.removeAllViews()

        val years = prefs.getYears()
        val semestersPerYear = prefs.getSemestersPerYear()
        val scale = prefs.getScale()

        Log.d("SemestersFragment", "Generating for $years years, $semestersPerYear semesters/year")

        // Group by years
        for (y in 1..years) {
            // Year header
            val yearHeader = android.widget.TextView(requireContext()).apply {
                text = "Year $y"
                textSize = 20f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(16, 24, 16, 8)
            }
            binding.semesterContainer.addView(yearHeader)

            // Calculate year GPA
            var yearTotalPoints = 0.0
            var yearTotalCredits = 0.0

            for (s in 1..semestersPerYear) {
                val semester = semesterManager.getOrCreateSemester(y, s)
                val subjects = semesterManager.getSubjectsForSemester(semester.id)

                subjects.forEach { subject ->
                    val gradePoints = GPAHelper.getGradePoints(scale)[subject.grade] ?: 0.0
                    yearTotalPoints += gradePoints * subject.credits
                    yearTotalCredits += subject.credits
                }
            }

            val yearGPA = if (yearTotalCredits > 0) yearTotalPoints / yearTotalCredits else 0.0

            // Year summary card
            val yearCard = androidx.cardview.widget.CardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 8, 16, 16)
                }
                radius = 12f
                cardElevation = 4f
                setCardBackgroundColor(getYearGPAColor(yearGPA))
            }

            val yearCardContent = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)

                val yearGPAText = android.widget.TextView(requireContext()).apply {
                    text = "Year GPA: ${GPAHelper.formatGPA(yearGPA, scale)}"
                    textSize = 16f
                    setTextColor(Color.WHITE)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                addView(yearGPAText)

                val yearCreditsText = android.widget.TextView(requireContext()).apply {
                    text = "Total Credits: ${yearTotalCredits.toInt()}"
                    textSize = 14f
                    setTextColor(Color.WHITE)
                }
                addView(yearCreditsText)
            }

            yearCard.addView(yearCardContent)
            binding.semesterContainer.addView(yearCard)

            // Semester buttons - ONLY for semesters that should exist
            for (s in 1..semestersPerYear) {
                val semester = semesterManager.getOrCreateSemester(y, s)

                val btn = Button(requireContext()).apply {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(16, 8, 16, 8)
                    layoutParams = params

                    text = "Semester $s - GPA: ${GPAHelper.formatGPA(semester.gpa, scale)} (${semester.totalCredits.toInt()} credits)"
                    isAllCaps = false
                    textSize = 14f
                    setPadding(24, 24, 24, 24)

                    // Set background color based on GPA
                    setBackgroundColor(getSemesterGPAColor(semester.gpa))
                    setTextColor(Color.WHITE)

                    setOnClickListener {
                        val intent = Intent(requireContext(), SemesterDetailActivity::class.java).apply {
                            putExtra("year", y)
                            putExtra("semester_number", s)
                        }
                        startActivity(intent)
                    }
                }
                binding.semesterContainer.addView(btn)
            }
        }

        Log.d("SemestersFragment", "Generated ${years * semestersPerYear} semester buttons")
    }

    private fun getSemesterGPAColor(gpa: Double): Int {
        return when {
            gpa >= 3.5 -> ContextCompat.getColor(requireContext(), R.color.green)
            gpa >= 3.0 -> ContextCompat.getColor(requireContext(), R.color.orange)
            gpa >= 2.0 -> ContextCompat.getColor(requireContext(), R.color.yellow)
            gpa > 0 -> ContextCompat.getColor(requireContext(), R.color.red)
            else -> ContextCompat.getColor(requireContext(), R.color.text_secondary)
        }
    }

    private fun getYearGPAColor(gpa: Double): Int {
        return when {
            gpa >= 3.5 -> Color.parseColor("#4CAF50")
            gpa >= 3.0 -> Color.parseColor("#FF9800")
            gpa >= 2.0 -> Color.parseColor("#FFC107")
            gpa > 0 -> Color.parseColor("#F44336")
            else -> Color.parseColor("#9E9E9E")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}