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
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.GradeMappingHelper
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
        val yearWeights = YearWeightHelper.getYearWeights(requireContext())

        binding.tvCurrentName.text = "Name: $userName"
        binding.tvCurrentScale.text = "Scale: $scale"
        binding.tvCurrentYears.text = "Years: $years"
        binding.tvCurrentSemesters.text = "Semesters per Year: $semestersPerYear"

        // Display year weights
        val weightsText = yearWeights.entries
            .sortedBy { it.key }
            .joinToString(", ") { (year, weight) ->
                "Y$year: ${String.format("%.0f", weight)}%"
            }
        binding.tvCurrentYearWeights.text = "Year Weights: $weightsText"
    }

    private fun setupClickListeners() {
        binding.cardEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.cardChangeScale.setOnClickListener { showChangeScaleDialog() }
        binding.cardChangeStructure.setOnClickListener { showChangeStructureDialog() }
        binding.cardResetData.setOnClickListener { showResetConfirmationDialog() }
        binding.cardAbout.setOnClickListener { showAboutDialog() }
        binding.cardGradeMapping.setOnClickListener { showEditGradeMappingDialog() }
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
                        // Reset custom grade mappings when scale changes
                        GradeMappingHelper.resetToDefaults(requireContext(), selectedScale)
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
    // Change Program Structure with Year Weights
    // =========================
    private fun showChangeStructureDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_structure, null)

        val yearsSpinner = dialogView.findViewById<Spinner>(R.id.spinner_years)
        val semestersSpinner = dialogView.findViewById<Spinner>(R.id.spinner_semesters)
        val btnConfigureWeights = dialogView.findViewById<android.widget.Button>(R.id.btn_configure_weights)
        val weightsPreviewText = dialogView.findViewById<TextView>(R.id.tv_weights_preview)
        val warningText = dialogView.findViewById<TextView>(R.id.tv_structure_warning)

        val yearsArray = (1..10).map { "$it year${if (it > 1) "s" else ""}" }.toTypedArray()
        val semestersArray = (1..5).map { "$it semester${if (it > 1) "s" else ""} per year" }.toTypedArray()

        val oldYears = prefs.getYears()
        val oldSemesters = prefs.getSemestersPerYear()
        var selectedYears = oldYears
        var selectedSemesters = oldSemesters

        yearsSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, yearsArray).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        yearsSpinner.setSelection(selectedYears - 1)

        semestersSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, semestersArray).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        semestersSpinner.setSelection(selectedSemesters - 1)

        // Update preview and warnings when selection changes
        fun updatePreviewAndWarnings() {
            val weights = YearWeightHelper.getYearWeights(requireContext())
            val weightsText = if (selectedYears != oldYears) {
                "Weights will be recalculated"
            } else {
                weights.entries
                    .sortedBy { it.key }
                    .joinToString(", ") { (year, weight) ->
                        "Y$year: ${String.format("%.0f", weight)}%"
                    }
            }
            weightsPreviewText.text = "Current: $weightsText"

            // Show warning if data will be deleted
            val willDeleteData = selectedYears < oldYears || selectedSemesters < oldSemesters
            if (willDeleteData) {
                val deletedYears = if (selectedYears < oldYears) {
                    "Years ${selectedYears + 1} to $oldYears"
                } else ""
                val deletedSemesters = if (selectedSemesters < oldSemesters) {
                    "Semesters ${selectedSemesters + 1} to $oldSemesters (in all years)"
                } else ""

                val warningMessage = buildString {
                    append("⚠️ WARNING: This will permanently delete:\n")
                    if (deletedYears.isNotEmpty()) append("• $deletedYears\n")
                    if (deletedSemesters.isNotEmpty()) append("• $deletedSemesters\n")
                    append("All subjects and grades in deleted semesters will be lost!")
                }

                warningText.text = warningMessage
                warningText.visibility = View.VISIBLE
            } else {
                warningText.visibility = View.GONE
            }
        }

        yearsSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYears = position + 1
                updatePreviewAndWarnings()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        semestersSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSemesters = position + 1
                updatePreviewAndWarnings()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        updatePreviewAndWarnings()

        // Configure weights button
        btnConfigureWeights.setOnClickListener {
            showYearWeightsDialog(selectedYears)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Program Structure")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val willDeleteData = selectedYears < oldYears || selectedSemesters < oldSemesters

                if (willDeleteData) {
                    // Show confirmation dialog before deleting data
                    AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Changes")
                        .setMessage("This will permanently delete data from removed years/semesters. Are you sure you want to continue?")
                        .setPositiveButton("Yes, Delete") { _, _ ->
                            applyStructureChanges(oldYears, oldSemesters, selectedYears, selectedSemesters)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    applyStructureChanges(oldYears, oldSemesters, selectedYears, selectedSemesters)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun applyStructureChanges(
        oldYears: Int,
        oldSemesters: Int,
        newYears: Int,
        newSemesters: Int
    ) {
        // Delete data from removed years
        if (newYears < oldYears) {
            for (year in (newYears + 1)..oldYears) {
                semesterManager.deleteYear(year)
            }
        }

        // Delete data from removed semesters in all years
        if (newSemesters < oldSemesters) {
            for (year in 1..newYears) {
                for (semester in (newSemesters + 1)..oldSemesters) {
                    semesterManager.deleteSemester(year, semester)
                }
            }
        }

        // Save new settings
        prefs.saveYears(newYears)
        prefs.saveSemestersPerYear(newSemesters)

        // Initialize new semesters if added
        semesterManager.initializeSemesters()

        // Update year weights if number of years changed
        if (newYears != oldYears) {
            YearWeightHelper.updateWeightsForYearChange(requireContext(), newYears)
        }

        val message = if (newYears < oldYears || newSemesters < oldSemesters) {
            "Structure updated. Data from removed years/semesters has been deleted."
        } else {
            "Structure updated successfully!"
        }

        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
        loadCurrentSettings()
    }

    // =========================
    // Configure Year Weights
    // =========================
    private fun showYearWeightsDialog(yearsToConfig: Int = prefs.getYears()) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_year_weights, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.year_weights_container)
        val tvTotal = dialogView.findViewById<TextView>(R.id.tv_total_percentage)
        val tvValidation = dialogView.findViewById<TextView>(R.id.tv_validation_message)

        val currentWeights = YearWeightHelper.getYearWeights(requireContext())
        val editTexts = mutableListOf<EditText>()

        // Create input fields for each year
        for (year in 1..yearsToConfig) {
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

        val presets = YearWeightHelper.getPresetWeights(yearsToConfig)
        presets.forEach { (name, weights) ->
            val btn = android.widget.Button(requireContext()).apply {
                text = name
                isAllCaps = false
                setOnClickListener {
                    for (year in 1..yearsToConfig) {
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
                for (year in 1..yearsToConfig) {
                    newWeights[year] = editTexts[year - 1].text.toString().toDoubleOrNull() ?: 0.0
                }

                YearWeightHelper.saveYearWeights(requireContext(), newWeights)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Year weights saved successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                loadCurrentSettings()
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
    // Edit Grade Mapping
    // =========================
    private fun showEditGradeMappingDialog() {
        val scale = prefs.getScale()
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_grade_mapping, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.grade_mapping_container)
        val btnReset = dialogView.findViewById<android.widget.Button>(R.id.btn_reset_defaults)

        val currentMappings = GradeMappingHelper.getGradeMappings(requireContext(), scale)
        val editTexts = mutableMapOf<String, EditText>()

        // Create input fields for each grade
        currentMappings.forEach { (grade, value) ->
            val gradeLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 8, 0, 8)
            }

            val label = TextView(requireContext()).apply {
                text = "$grade → "
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val input = EditText(requireContext()).apply {
                setText(String.format("%.2f", value))
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

            editTexts[grade] = input
            gradeLayout.addView(label)
            gradeLayout.addView(input)
            container.addView(gradeLayout)
        }

        // Reset to defaults button
        btnReset.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Reset to Defaults?")
                .setMessage("This will restore standard grade mappings for the $scale scale.")
                .setPositiveButton("Reset") { _, _ ->
                    GradeMappingHelper.resetToDefaults(requireContext(), scale)
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Grade mappings reset to defaults",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    // Refresh the dialog
                    showEditGradeMappingDialog()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Grade Mappings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newMappings = mutableMapOf<String, Double>()
                editTexts.forEach { (grade, editText) ->
                    val value = editText.text.toString().toDoubleOrNull()
                    if (value != null) {
                        newMappings[grade] = value
                    }
                }

                GradeMappingHelper.saveGradeMappings(requireContext(), scale, newMappings)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Grade mappings saved successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("View Only") { _, _ ->
                showGradeGpaDialog()
            }
            .create()

        dialog.show()
    }


    // =========================
    // Reset All Data
    // =========================
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset ALL data? This will delete:\n\n• All semesters\n• All subjects\n• All grades\n• Your profile settings\n• Year weight configuration\n• Custom grade mappings\n\nThis action CANNOT be undone!")
            .setPositiveButton("Reset Everything") { _, _ -> resetAllData() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        semesterManager.resetAllData()
        prefs.clearAll()
        GradeMappingHelper.clearCustomMappings(requireContext())

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
            .setMessage("GPA Master v1.0\nDeveloped by Gihan S Gamage\uD83D\uDDA4\n\nA comprehensive GPA calculator for university students supporting multiple grading scales:\n\n• 4.0 Scale\n• 5.0 Scale\n• 10.0 Scale\n• Percentage Scale\n\nFeatures:\n• Track multiple semesters\n• Calculate semester and overall GPA\n• Year-wise weighted GPA calculation\n• Customizable grade mappings\n• Visual progress tracking\n• Flexible program structure\n• Export academic reports\n\nDeveloped to help students monitor their academic progress effectively.")
            .setPositiveButton("OK", null)
            .show()
    }

    // =========================
    // View Grade → GPA Mapping (Read Only)
    // =========================
    private fun showGradeGpaDialog() {
        val scale = prefs.getScale()
        val mapping = GradeMappingHelper.getGradeMappings(requireContext(), scale)

        if (mapping.isEmpty()) return

        val message = StringBuilder()
        message.append("Current mappings for $scale scale:\n\n")
        mapping.forEach { (grade, gpa) ->
            message.append("$grade → ${String.format("%.2f", gpa)}\n")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Grade → GPA Mapping")
            .setMessage(message.toString())
            .setPositiveButton("Edit") { _, _ ->
                showEditGradeMappingDialog()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}