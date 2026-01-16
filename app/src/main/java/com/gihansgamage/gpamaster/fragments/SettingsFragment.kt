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
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.LoginActivity
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.databinding.FragmentSettingsBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager
import com.gihansgamage.gpamaster.utils.YearWeightHelper

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
        binding.cardEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.cardChangeScale.setOnClickListener { showChangeScaleDialog() }
        binding.cardChangeStructure.setOnClickListener { showChangeStructureDialog() }
        binding.cardYearWeights.setOnClickListener { showYearWeightsDialog() }
        binding.cardExportData.setOnClickListener { exportData() }
        binding.cardResetData.setOnClickListener { showResetConfirmationDialog() }
        binding.cardAbout.setOnClickListener { showAboutDialog() }
        binding.cardGradeMapping.setOnClickListener { showGradeGpaDialog() }
    }

    // =========================
    // Edit Profile
    // =========================
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

    // =========================
    // Change GPA Scale
    // =========================
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

    // =========================
    // Change Program Structure
    // =========================
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
                val oldYears = prefs.getYears()
                prefs.saveYears(selectedYears)
                prefs.saveSemestersPerYear(selectedSemesters)
                semesterManager.initializeSemesters()

                // Update year weights if number of years changed
                if (selectedYears != oldYears) {
                    YearWeightHelper.updateWeightsForYearChange(requireContext(), selectedYears)
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Year weights have been adjusted. Review them in 'Configure Year Weights'.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }

                loadCurrentSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================
    // Configure Year Weights (NEW!)
    // =========================
    private fun showYearWeightsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_weights, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.year_weights_container)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tv_total_percentage)
        val tvValidation = dialogView.findViewById<TextView>(R.id.tv_validation_message)

        val years = prefs.getYears()
        val currentWeights = YearWeightHelper.getYearWeights(requireContext())
        val editTexts = mutableListOf<EditText>()

        // Create input fields for each year
        for (year in 1..years) {
            val yearLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val label = TextView(requireContext()).apply {
                text = "Year $year:"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val input = EditText(requireContext()).apply {
                setText(String.format("%.2f", currentWeights[year] ?: 0.0))
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                hint = "0.00"
                background = resources.getDrawable(R.drawable.field_bg, null)
                setPadding(12, 12, 12, 12)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val percentSign = TextView(requireContext()).apply {
                text = " %"
                textSize = 16f
                setPadding(8, 0, 0, 0)
            }

            // Update total when text changes
            input.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateTotal(editTexts, tvTotal, tvValidation)
                }
            })

            editTexts.add(input)
            yearLayout.addView(label)
            yearLayout.addView(input)
            yearLayout.addView(percentSign)
            container.addView(yearLayout)
        }

        // Add preset buttons
        val presetsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 0)
        }

        val presetsLabel = TextView(requireContext()).apply {
            text = "Quick Presets:"
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        presetsLayout.addView(presetsLabel)

        val presets = YearWeightHelper.getPresetWeights(years)
        presets.forEach { (name, weights) ->
            val btn = android.widget.Button(requireContext()).apply {
                text = name
                isAllCaps = false
                setOnClickListener {
                    // Apply preset
                    for (year in 1..years) {
                        editTexts[year - 1].setText(String.format("%.2f", weights[year] ?: 0.0))
                    }
                }
            }
            presetsLayout.addView(btn)
        }

        container.addView(presetsLayout)

        // Initial total calculation
        updateTotal(editTexts, tvTotal, tvValidation)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Configure Year Weights")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val total = editTexts.sumOf {
                    it.text.toString().toDoubleOrNull() ?: 0.0
                }

                if (total < 99.9 || total > 100.1) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Total must equal 100%. Current: ${String.format("%.2f", total)}%",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                val newWeights = mutableMapOf<Int, Double>()
                for (year in 1..years) {
                    newWeights[year] = editTexts[year - 1].text.toString().toDoubleOrNull() ?: 0.0
                }

                YearWeightHelper.saveYearWeights(requireContext(), newWeights)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Year weights saved successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateTotal(
        editTexts: List<EditText>,
        tvTotal: TextView,
        tvValidation: TextView
    ) {
        val total = editTexts.sumOf {
            it.text.toString().toDoubleOrNull() ?: 0.0
        }

        tvTotal.text = String.format("%.2f%%", total)

        when {
            total < 99.9 -> {
                tvValidation.text = "Total is less than 100% (${String.format("%.2f", 100.0 - total)}% remaining)"
                tvValidation.visibility = android.view.View.VISIBLE
                tvTotal.setTextColor(resources.getColor(R.color.error, null))
            }
            total > 100.1 -> {
                tvValidation.text = "Total exceeds 100% (${String.format("%.2f", total - 100.0)}% over)"
                tvValidation.visibility = android.view.View.VISIBLE
                tvTotal.setTextColor(resources.getColor(R.color.error, null))
            }
            else -> {
                tvValidation.visibility = android.view.View.GONE
                tvTotal.setTextColor(resources.getColor(R.color.success, null))
            }
        }
    }

    // =========================
    // Export Data
    // =========================
    private fun exportData() {
        val exportBuilder = StringBuilder()
        val scale = prefs.getScale()

        exportBuilder.append("=== My Academic Report ===\n")
        exportBuilder.append("User Name: ${prefs.getUserName()}\n")
        exportBuilder.append("GPA Scale: $scale\n")
        exportBuilder.append("Program Structure: ${prefs.getYears()} Years, ${prefs.getSemestersPerYear()} Semesters/Year\n")

        val (overallGPA, totalCredits) = semesterManager.calculateOverallGPA()
        val (weightedGPA, _) = YearWeightHelper.calculateWeightedGPA(requireContext(), semesterManager)

        exportBuilder.append("Current Overall GPA: ${com.gihansgamage.gpamaster.utils.GPAHelper.formatGPA(overallGPA, scale)}\n")

        if (weightedGPA > 0.0 && weightedGPA != overallGPA) {
            exportBuilder.append("Weighted GPA: ${com.gihansgamage.gpamaster.utils.GPAHelper.formatGPA(weightedGPA, scale)}\n")
            exportBuilder.append("\nYear Weights:\n")
            val weights = YearWeightHelper.getYearWeights(requireContext())
            weights.forEach { (year, weight) ->
                exportBuilder.append("  Year $year: ${String.format("%.2f", weight)}%\n")
            }
        }

        exportBuilder.append("Total Credits Earned: ${totalCredits.toInt()}\n")
        exportBuilder.append("==============================\n\n")

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

    // =========================
    // Reset All Data
    // =========================
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset ALL data? This will delete:\n\n• All semesters\n• All subjects\n• All grades\n• Your profile settings\n• Year weight configuration\n\nThis action CANNOT be undone!")
            .setPositiveButton("Reset Everything") { _, _ -> resetAllData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        semesterManager.resetAllData()
        prefs.clearAll()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    // =========================
    // About Dialog
    // =========================
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About GPA Master")
            .setMessage("GPA Master v1.0\nDeveloped by Gihan S Gamage\uD83D\uDDA4\n\nA comprehensive GPA calculator for university students supporting multiple grading scales:\n\n• 4.0 Scale\n• 5.0 Scale\n• 10.0 Scale\n• Percentage Scale\n\nFeatures:\n• Track multiple semesters\n• Calculate semester and overall GPA\n• Year-wise weighted GPA calculation\n• Visual progress tracking\n• Flexible program structure\n• Export academic reports\n\nDeveloped to help students monitor their academic progress effectively.")
            .setPositiveButton("OK", null)
            .show()
    }

    // =========================
    // Grade → GPA Mapping
    // =========================
    private fun getGradeGpaMapping(scale: String): Map<String, Double> {
        return when (scale) {
            "4.0" -> mapOf(
                "A+" to 4.0, "A" to 4.0, "A−" to 3.7,
                "B+" to 3.3, "B" to 3.0, "B−" to 2.7,
                "C+" to 2.3, "C" to 2.0, "C−" to 1.7,
                "D" to 1.0, "F" to 0.0
            )
            "5.0" -> mapOf(
                "A+" to 5.0, "A" to 4.5, "A−" to 4.0,
                "B+" to 3.5, "B" to 3.0, "B−" to 2.5,
                "C+" to 2.0, "C" to 1.5, "C−" to 1.0,
                "D" to 0.5, "F" to 0.0
            )
            "10.0" -> mapOf(
                "A+" to 10.0, "A" to 9.0,
                "B+" to 8.0, "B" to 7.0,
                "C+" to 6.0, "C" to 5.0,
                "D" to 4.0, "F" to 0.0
            )
            "percentage" -> mapOf(
                "A+" to 100.0, "A" to 90.0,
                "B+" to 80.0, "B" to 70.0,
                "C" to 60.0, "D" to 50.0,
                "F" to 30.0
            )
            else -> emptyMap()
        }
    }

    private fun showGradeGpaDialog() {
        val scale = prefs.getScale()
        val mapping = getGradeGpaMapping(scale)
        if (mapping.isEmpty()) return

        val message = StringBuilder()
        mapping.forEach { (grade, gpa) ->
            message.append("$grade → $gpa\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Grade → GPA ($scale Scale)")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}