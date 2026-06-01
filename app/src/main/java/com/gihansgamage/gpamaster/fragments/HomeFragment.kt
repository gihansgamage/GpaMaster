package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.AllResultsActivity
import com.gihansgamage.gpamaster.MainActivity
import com.gihansgamage.gpamaster.SemesterDetailActivity
import com.gihansgamage.gpamaster.databinding.FragmentHomeBinding
import com.gihansgamage.gpamaster.utils.DegreeClassificationHelper
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager
import com.gihansgamage.gpamaster.utils.WeightedGPAResult
import com.gihansgamage.gpamaster.utils.YearWeightHelper
import com.gihansgamage.gpamaster.views.GpaScaleBarView

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = PrefsHelper(requireContext())
        semesterManager = SemesterManager(requireContext())

        loadDashboardData()
        setupRefresh()
        setupQuickActions()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // Welcome message
        binding.tvWelcome.text = "Hi, ${prefs.getUserName()} 👋"

        // Scale and structure info
        binding.tvScale.text = "GPA Scale: ${prefs.getScale()}"
        binding.tvProgramStructure.text = "${prefs.getYears()} Years • ${prefs.getSemestersPerYear()} Semesters/Year"

        // Calculate overall GPA (normal - equal weight for all semesters)
        val (overallGPA, totalCredits) = semesterManager.calculateOverallGPA()
        val scale = prefs.getScale()

        binding.tvCurrentGpa.text = GPAHelper.formatGPA(overallGPA, scale)
        binding.tvTotalCredits.text = totalCredits.toInt().toString()

        // Calculate weighted GPA (based on year percentages)
        val weightedResult = YearWeightHelper.calculateWeightedGPA(
            requireContext(),
            semesterManager
        )
        val weights = YearWeightHelper.getYearWeights(requireContext())
        val allWeightsEqual = YearWeightHelper.areWeightsEqual(weights)

        // Show weighted GPA card only when weights are unequal and there is data
        if (weightedResult.weightedGPA > 0.0 && !allWeightsEqual) {
            binding.cardWeightedGpa.visibility = android.view.View.VISIBLE
            binding.tvWeightedGpa.text = GPAHelper.formatGPA(weightedResult.weightedGPA, scale)
            binding.tvWeightedDescription.text = buildWeightedDescription(weightedResult, weights)
        } else {
            binding.cardWeightedGpa.visibility = android.view.View.GONE
        }

        // Calculate progress
        val years = prefs.getYears()
        val semestersPerYear = prefs.getSemestersPerYear()
        val totalSemesters = years * semestersPerYear

        val completedSemesters = semesterManager.getAvailableSemesters()
            .count { it.totalCredits > 0 }

        binding.tvSemestersCompleted.text = "Semesters: $completedSemesters/$totalSemesters"
        binding.tvRemainingCredits.text = "Remaining: ${totalSemesters - completedSemesters}"

        val progressPercent = if (totalSemesters > 0) {
            (completedSemesters * 100) / totalSemesters
        } else 0

        binding.progressBar.progress = progressPercent
        binding.tvProgressText.text = "$progressPercent% Complete"

        // Show degree classification bar based on weighted GPA (if available) or normal GPA
        val gpaForClassification = if (weightedResult.weightedGPA > 0.0 && !allWeightsEqual) {
            weightedResult.weightedGPA
        } else {
            overallGPA
        }

        if (gpaForClassification > 0.0) {
            val classification = DegreeClassificationHelper.getClassification(gpaForClassification, scale)
            val bands = DegreeClassificationHelper.getBands(scale)
            val maxScale = DegreeClassificationHelper.getMaxScale(scale)

            binding.cardClassification.visibility = android.view.View.VISIBLE
            // Reset card to white background — colours are on the bar itself
            binding.cardClassification.setCardBackgroundColor(android.graphics.Color.WHITE)
            binding.gpaScaleBar.setData(gpaForClassification, maxScale, bands)
            binding.tvClassificationMessage.text = classification.message
        } else {
            binding.cardClassification.visibility = android.view.View.GONE
        }
    }

    /**
     * Builds the description text shown below the Weighted GPA value.
     *
     * - Full year scenario: "Based on year weights: Y1: 10%, Y2: 20%, Y3: 30%, Y4: 40%"
     * - Partial year scenario: shows which years have data, the original ratio,
     *   and the effective renormalized percentages.
     *   e.g. "Years with data: Y1, Y2 | Original ratio 10:20 → Effective: Y1: 33.3%, Y2: 66.7%"
     */
    private fun buildWeightedDescription(
        result: WeightedGPAResult,
        fullWeights: Map<Int, Double>
    ): String {
        return if (result.isPartial) {
            val yearsLabel = result.yearsWithData.joinToString(", ") { "Y$it" }
            val ratioLabel = result.yearsWithData.joinToString(":") { year ->
                String.format("%.0f", fullWeights[year] ?: 0.0)
            }
            val sortedEffective = result.effectiveWeights.entries.sortedBy { it.key }
            val effectiveLabel = sortedEffective.joinToString(", ") { e: Map.Entry<Int, Double> ->
                "Y${e.key}: ${String.format("%.1f", e.value)}%"
            }
            "Years with data: $yearsLabel | Ratio $ratioLabel \u2192 Effective: $effectiveLabel"
        } else {
            val sortedWeights = result.effectiveWeights.entries.sortedBy { it.key }
            val weightsLabel = sortedWeights.joinToString(", ") { e: Map.Entry<Int, Double> ->
                "Y${e.key}: ${String.format("%.0f", e.value)}%"
            }
            "Based on year weights: $weightsLabel"
        }
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadDashboardData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupQuickActions() {
        // "Add Grade" button - Show quick add dialog with semester selection
        binding.btnAddGrade.setOnClickListener {
            showQuickAddGradeDialog()
        }

        // "View All" button - Open All Results screen
        binding.btnViewAll.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), AllResultsActivity::class.java))
        }
    }

    private fun showQuickAddGradeDialog() {
        try {
            val dialogView = layoutInflater.inflate(
                com.gihansgamage.gpamaster.R.layout.dialog_quick_add_grade,
                null
            )
            val scale = prefs.getScale()

            // Get available semesters (only those that should exist based on current settings)
            val availableSemesters = semesterManager.getAvailableSemesters()

            if (availableSemesters.isEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "No semesters available. Please check your program structure settings.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }

            // Get views from dialog
            val spinnerSemester = dialogView.findViewById<android.widget.Spinner>(
                com.gihansgamage.gpamaster.R.id.spinner_semester
            )
            val etSubjectName = dialogView.findViewById<android.widget.EditText>(
                com.gihansgamage.gpamaster.R.id.et_subject_name
            )
            val etCredits = dialogView.findViewById<android.widget.EditText>(
                com.gihansgamage.gpamaster.R.id.et_credits
            )
            val spinnerGrade = dialogView.findViewById<android.widget.Spinner>(
                com.gihansgamage.gpamaster.R.id.spinner_grade
            )
            val etPercentage = dialogView.findViewById<android.widget.EditText>(
                com.gihansgamage.gpamaster.R.id.et_percentage
            )
            val tvPercentageLabel = dialogView.findViewById<android.widget.TextView>(
                com.gihansgamage.gpamaster.R.id.tv_percentage_label
            )

            // Setup semester spinner - Only show semesters within current program structure
            val semesterNames = availableSemesters.map {
                "Year ${it.year} - Semester ${it.semesterNumber}"
            }
            val semesterAdapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                semesterNames
            )
            semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSemester.adapter = semesterAdapter

            // Select first incomplete semester by default
            val firstIncompleteIndex = availableSemesters.indexOfFirst { it.totalCredits == 0.0 }
            if (firstIncompleteIndex != -1) {
                spinnerSemester.setSelection(firstIncompleteIndex)
            }

            // Setup grade spinner
            val grades = GPAHelper.getAvailableGrades(scale)
            val gradeAdapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                grades
            )
            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGrade.adapter = gradeAdapter

            // Show percentage field only for percentage scale
            if (scale == "percentage") {
                etPercentage?.visibility = android.view.View.VISIBLE
                tvPercentageLabel?.visibility = android.view.View.VISIBLE
            } else {
                etPercentage?.visibility = android.view.View.GONE
                tvPercentageLabel?.visibility = android.view.View.GONE
            }

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("Quick Add Grade")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    try {
                        if (spinnerSemester.selectedItemPosition < 0 ||
                            spinnerSemester.selectedItemPosition >= availableSemesters.size) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Please select a valid semester",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@setPositiveButton
                        }

                        val selectedSemester = availableSemesters[spinnerSemester.selectedItemPosition]
                        val name = etSubjectName.text.toString().trim()
                        val creditsStr = etCredits.text.toString().trim()
                        val grade = spinnerGrade.selectedItem?.toString() ?: "A"
                        val percentageStr = etPercentage?.text?.toString()?.trim() ?: ""

                        // Validation
                        if (name.isEmpty()) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Please enter subject name",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            return@setPositiveButton
                        }

                        val credits = creditsStr.toDoubleOrNull()
                        if (credits == null || credits <= 0) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Please enter valid credits",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            return@setPositiveButton
                        }

                        val percentage = if (scale == "percentage" && percentageStr.isNotEmpty()) {
                            percentageStr.toDoubleOrNull()
                        } else null

                        val newSubject = com.gihansgamage.gpamaster.models.Subject(
                            id = 0,
                            semesterId = selectedSemester.id,
                            name = name,
                            credits = credits,
                            grade = grade,
                            percentage = percentage
                        )

                        semesterManager.addSubject(selectedSemester.id, newSubject)

                        android.widget.Toast.makeText(
                            requireContext(),
                            "Added to Year ${selectedSemester.year}, Semester ${selectedSemester.semesterNumber}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()

                        // Refresh dashboard
                        loadDashboardData()

                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()

        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Error showing dialog: ${e.message}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}