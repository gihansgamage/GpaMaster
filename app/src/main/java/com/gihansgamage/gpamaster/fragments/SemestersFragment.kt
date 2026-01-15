package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.gihansgamage.gpamaster.SemesterDetailActivity
import com.gihansgamage.gpamaster.adapters.SemesterAdapter
import com.gihansgamage.gpamaster.databinding.FragmentSemestersBinding
import com.gihansgamage.gpamaster.models.Semester
import com.gihansgamage.gpamaster.utils.SharedPrefHelper

class SemestersFragment : Fragment() {

    private var _binding: FragmentSemestersBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var semesterAdapter: SemesterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemestersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPrefHelper = SharedPrefHelper(requireContext())
        setupRecyclerView()
        loadSemesters()
    }

    private fun setupRecyclerView() {
        semesterAdapter = SemesterAdapter(emptyList()) { semester ->
            val intent = Intent(requireContext(), SemesterDetailActivity::class.java).apply {
                putExtra("semester_id", semester.id)
                putExtra("year", semester.year)
                putExtra("semester_number", semester.semesterNumber)
            }
            startActivity(intent)
        }

        binding.rvSemesters.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = semesterAdapter
        }
    }

    private fun loadSemesters() {
        val totalYears = sharedPrefHelper.getInt("total_years", 4)
        val semestersPerYear = sharedPrefHelper.getInt("semesters_per_year", 2)

        val semesters = mutableListOf<Semester>()
        var semesterId = 1

        for (year in 1..totalYears) {
            for (semesterNum in 1..semestersPerYear) {
                semesters.add(
                    Semester(
                        id = semesterId++,
                        year = year,
                        semesterNumber = semesterNum,
                        gpa = 0.0,
                        totalCredits = 0.0
                    )
                )
            }
        }

        semesterAdapter.updateData(semesters)

        // Show/hide empty state
        if (semesters.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvSemesters.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvSemesters.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadSemesters() // Refresh when returning from detail
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}