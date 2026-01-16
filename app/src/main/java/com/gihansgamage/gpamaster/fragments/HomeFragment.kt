package com.gihansgamage.gpamaster.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.MainActivity
import com.gihansgamage.gpamaster.databinding.FragmentHomeBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsHelper: PrefsHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = PrefsHelper(requireContext())
        loadUserData()
        setupClickListeners()
        setupRefreshListener()
    }

    private fun loadUserData() {
        val userName = prefsHelper.getUserName()
        val scale = prefsHelper.getScale()
        val years = prefsHelper.getYears()
        val semestersPerYear = prefsHelper.getSemestersPerYear()

        binding.tvWelcome.text = if (userName.isNotEmpty()) "Hi, $userName!" else "Hi, Student!"
        binding.tvScale.text = "Scale: $scale"

        // Placeholder logic for current progress
        val currentGPA = 3.75
        val totalCredits = 45.0
        val semestersCompleted = 6
        val totalSemesters = years * semestersPerYear
        val progressPercentage = (semestersCompleted.toDouble() / totalSemesters * 100).toInt()

        binding.tvCurrentGpa.text = String.format("%.2f", currentGPA)
        binding.tvTotalCredits.text = totalCredits.toInt().toString()
        binding.tvSemestersCompleted.text = "Semesters: $semestersCompleted/$totalSemesters"
        binding.tvRemainingCredits.text = "Remaining: ${totalSemesters - semestersCompleted}"
        binding.tvProgressText.text = "$progressPercentage% Complete"
        binding.progressBar.progress = progressPercentage

        // Dynamic color selection based on GPA
        val progressBarColor = when {
            currentGPA >= 3.5 -> R.color.green
            currentGPA >= 3.0 -> R.color.orange
            currentGPA >= 2.0 -> R.color.yellow
            else -> R.color.red
        }

        binding.progressBar.progressTintList =
            ContextCompat.getColorStateList(requireContext(), progressBarColor)
    }

    private fun setupClickListeners() {
        binding.btnAddGrade.setOnClickListener {
            showAddGradeDialog()
        }

        binding.btnViewAll.setOnClickListener {
            navigateToSemesters()
        }
    }

    private fun navigateToSemesters() {
        // Correctly casting activity to MainActivity to access public functions
        (activity as? MainActivity)?.let { mainActivity ->
            mainActivity.loadFragment(SemestersFragment())
            mainActivity.bottomNavigation.selectedItemId = R.id.nav_semesters
        }
    }

    private fun showAddGradeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Grade")
            .setMessage("This will take you to the semester selection screen.")
            .setPositiveButton("Continue") { _, _ ->
                navigateToSemesters()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRefreshListener() {
        binding.swipeRefresh.setOnRefreshListener {
            loadUserData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}