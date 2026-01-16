package com.gihansgamage.gpamaster.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.LoginActivity
import com.gihansgamage.gpamaster.databinding.FragmentSettingsBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        prefs = PrefsHelper(requireContext())
        semesterManager = SemesterManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCurrentSettings()
        setupClickListeners()
    }

    private fun loadCurrentSettings() {
        val userName = prefs.getUserName()
        val scale = prefs.getScale()
        val years = prefs.getYears()
        val semestersPerYear = prefs.getSemestersPerYear()

        binding.tvCurrentName.text = "Name: $userName"
        binding.tvCurrentScale.text = "Scale: $scale"
        binding.tvCurrentYears.text = "Years: $years"
        binding.tvCurrentSemesters.text = "Semesters per Year: $semestersPerYear"
    }

    private fun setupClickListeners() {
        binding.cardEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.cardChangeScale.setOnClickListener {
            showChangeScaleDialog()
        }

        binding.cardChangeStructure.setOnClickListener {
            showChangeStructureDialog()
        }

        binding.cardExportData.setOnClickListener {
            exportData()
        }

        binding.cardResetData.setOnClickListener {
            showResetConfirmationDialog()
        }

        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showEditProfileDialog() {
        val input = EditText(requireContext()).apply {
            setText(prefs.getUserName())
            setPadding(32, 32, 32, 32)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Your Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    prefs.saveUserName(newName)
                    loadCurrentSettings()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangeScaleDialog() {
        val scales = arrayOf("4.0 Scale", "5.0 Scale", "10.0 Scale", "Percentage Scale")
        val currentScale = prefs.getScale()
        val currentIndex = when (currentScale) {
            "4.0" -> 0
            "5.0" -> 1
            "10.0" -> 2
            "percentage" -> 3
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Change GPA Scale")
            .setSingleChoiceItems(scales, currentIndex) { dialog, which ->
                val selectedScale = when (which) {
                    0 -> "4.0"
                    1 -> "5.0"
                    2 -> "10.0"
                    3 -> "percentage"
                    else -> "4.0"
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Warning")
                    .setMessage("Changing the scale will affect all GPA calculations. Continue?")
                    .setPositiveButton("Yes") { _, _ ->
                        prefs.saveScale(selectedScale)
                        loadCurrentSettings()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangeStructureDialog() {
        val yearsArray = (1..10).map { "$it year${if (it > 1) "s" else ""}" }.toTypedArray()
        val semestersArray = (1..5).map { "$it semester${if (it > 1) "s" else ""} per year" }.toTypedArray()

        var selectedYears = prefs.getYears()
        var selectedSemesters = prefs.getSemestersPerYear()

        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val yearsText = android.widget.TextView(requireContext()).apply {
            text = "Number of Years"
            textSize = 16f
        }
        dialogView.addView(yearsText)

        val yearsSpinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, yearsArray).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selectedYears - 1)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedYears = position + 1
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
        dialogView.addView(yearsSpinner)

        val semestersText = android.widget.TextView(requireContext()).apply {
            text = "Semesters per Year"
            textSize = 16f
            setPadding(0, 24, 0, 0)
        }
        dialogView.addView(semestersText)

        val semestersSpinner = Spinner(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, semestersArray).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selectedSemesters - 1)
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedSemesters = position + 1
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
        dialogView.addView(semestersSpinner)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Program Structure")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                prefs.saveYears(selectedYears)
                prefs.saveSemestersPerYear(selectedSemesters)
                semesterManager.initializeSemesters()
                loadCurrentSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportData() {
        val exportBuilder = StringBuilder()
        val scale = prefs.getScale()

        // 1. Gather User Data
        exportBuilder.append("=== My Academic Report ===\n")
        exportBuilder.append("User Name: ${prefs.getUserName()}\n")
        exportBuilder.append("GPA Scale: $scale\n")
        exportBuilder.append("Program Structure: ${prefs.getYears()} Years, ${prefs.getSemestersPerYear()} Semesters/Year\n")

        val (overallGPA, totalCredits) = semesterManager.calculateOverallGPA()
        exportBuilder.append("Current Overall GPA: ${com.gihansgamage.gpamaster.utils.GPAHelper.formatGPA(overallGPA, scale)}\n")
        exportBuilder.append("Total Credits Earned: ${totalCredits.toInt()}\n")
        exportBuilder.append("==============================\n\n")

        // 2. Gather Semester and Subject Data
        val allSemesters = semesterManager.getAllSemesters()
            .sortedWith(compareBy({ it.year }, { it.semesterNumber }))

        for (semester in allSemesters) {
            exportBuilder.append("YEAR ${semester.year} - SEMESTER ${semester.semesterNumber}\n")
            exportBuilder.append("Semester GPA: ${com.gihansgamage.gpamaster.utils.GPAHelper.formatGPA(semester.gpa, scale)}\n")
            exportBuilder.append("Semester Credits: ${semester.totalCredits.toInt()}\n")
            exportBuilder.append("------------------------------\n")

            val subjects = semesterManager.getSubjectsForSemester(semester.id)
            if (subjects.isEmpty()) {
                exportBuilder.append("No subjects added.\n")
            } else {
                exportBuilder.append(String.format("%-30s | %-8s | %-6s\n", "Subject", "Credits", "Grade"))
                subjects.forEach { subject ->
                    exportBuilder.append(String.format("%-30s | %-8.1f | %-6s\n",
                        subject.name, subject.credits, subject.grade))
                }
            }
            exportBuilder.append("\n")
        }

        exportBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        exportBuilder.append("Generated by GPA Master on ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())}\n")

        // 3. Share/Save Intent
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "GPA Master Data Export - ${prefs.getUserName()}")
                putExtra(Intent.EXTRA_TEXT, exportBuilder.toString())
            }
            startActivity(Intent.createChooser(shareIntent, "Export Data via"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(requireContext(), "Export failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset ALL data? This will delete:\n\n• All semesters\n• All subjects\n• All grades\n• Your profile settings\n\nThis action CANNOT be undone!")
            .setPositiveButton("Reset Everything") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        // Clear all data
        semesterManager.resetAllData()
        prefs.clearAll()

        // Redirect to login
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About GPA Master")
            .setMessage("GPA Master v1.0\nDeveloped by Gihan S Gamage\n\nA comprehensive GPA calculator for university students supporting multiple grading scales:\n\n• 4.0 Scale\n• 5.0 Scale\n• 10.0 Scale\n• Percentage Scale\n\nFeatures:\n• Track multiple semesters\n• Calculate semester and overall GPA\n• Visual analytics and progress tracking\n• Flexible program structure\n\nDeveloped to help students monitor their academic progress effectively.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}