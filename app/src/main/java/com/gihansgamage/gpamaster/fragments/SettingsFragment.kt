package com.gihansgamage.gpamaster.fragments

import android.app.AlertDialog
import android.content.Intent // Resolved: Added missing import
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.LoginActivity
import com.gihansgamage.gpamaster.databinding.FragmentSettingsBinding
import com.gihansgamage.gpamaster.utils.SharedPrefHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefHelper = SharedPrefHelper(requireContext())
        loadCurrentSettings()
        setupClickListeners()
    }

    private fun loadCurrentSettings() {
        val userName = sharedPrefHelper.getString("user_name", "Student")
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0")
        val years = sharedPrefHelper.getInt("total_years", 4)
        val semestersPerYear = sharedPrefHelper.getInt("semesters_per_year", 2)

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
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setMessage("This feature is under development")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showChangeScaleDialog() {
        val scales = arrayOf("4.0 Scale", "5.0 Scale", "10.0 Scale", "Percentage Scale")

        AlertDialog.Builder(requireContext())
            .setTitle("Change GPA Scale")
            .setItems(scales) { _, which ->
                val selectedScale = when (which) {
                    0 -> "4.0"
                    1 -> "5.0"
                    2 -> "10.0"
                    3 -> "percentage"
                    else -> "4.0"
                }
                sharedPrefHelper.saveString("gpa_scale", selectedScale)
                loadCurrentSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangeStructureDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Change Structure")
            .setMessage("This feature is under development")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun exportData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Export Data")
            .setMessage("This feature is under development")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset all data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetAllData() {
        sharedPrefHelper.clearAll()

        // Resolved: Fixed Intent initialization and type mismatch for flags
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About GPA Master")
            .setMessage("GPA Master v1.0\n\nA powerful GPA calculator for university students with multiple grading scales and detailed analytics.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}