package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.MainActivity
import com.gihansgamage.gpamaster.SemesterDetailActivity
import com.gihansgamage.gpamaster.databinding.FragmentHomeBinding
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager

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

        // Scale info
        binding.tvScale.text = "Scale: ${prefs.getScale()}"

        // Calculate overall GPA and credits
        val (overallGPA, totalCredits) = semesterManager.calculateOverallGPA()
        val scale = prefs.getScale()

        binding.tvCurrentGpa.text = GPAHelper.formatGPA(overallGPA, scale)
        binding.tvTotalCredits.text = totalCredits.toInt().toString()

        // Calculate progress
        val years = prefs.getYears()
        val semestersPerYear = prefs.getSemestersPerYear()
        val totalSemesters = years * semestersPerYear

        val completedSemesters = semesterManager.getAllSemesters()
            .count { it.totalCredits > 0 }

        binding.tvSemestersCompleted.text = "Semesters: $completedSemesters/$totalSemesters"
        binding.tvRemainingCredits.text = "Remaining: ${totalSemesters - completedSemesters}"

        val progressPercent = if (totalSemesters > 0) {
            (completedSemesters * 100) / totalSemesters
        } else 0

        binding.progressBar.progress = progressPercent
        binding.tvProgressText.text = "$progressPercent% Complete"
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

        // "View All" button - Switch to Semesters tab
        binding.btnViewAll.setOnClickListener {
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.gihansgamage.gpamaster.R.id.bottom_navigation
                )?.selectedItemId = com.gihansgamage.gpamaster.R.id.nav_semesters
            }
        }
    }

    private fun showQuickAddGradeDialog() {
        try {
            val dialogView = layoutInflater.inflate(
                com.gihansgamage.gpamaster.R.layout.dialog_quick_add_grade,
                null
            )
            val scale = prefs.getScale()

            // Get all semesters
            val allSemesters = semesterManager.getAllSemesters()
                .sortedWith(compareBy({ it.year }, { it.semesterNumber }))

            if (allSemesters.isEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "No semesters available",
                    android.widget.Toast.LENGTH_SHORT
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

            // Setup semester spinner
            val semesterNames = allSemesters.map {
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
            val firstIncompleteIndex = allSemesters.indexOfFirst { it.totalCredits == 0.0 }
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
                        val selectedSemester = allSemesters[spinnerSemester.selectedItemPosition]
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