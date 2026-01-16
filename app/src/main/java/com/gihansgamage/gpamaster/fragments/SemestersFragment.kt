package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.MainActivity
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.SemesterDetailActivity
import com.gihansgamage.gpamaster.databinding.FragmentSemestersBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper

class SemestersFragment : Fragment() {
    private var _binding: FragmentSemestersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSemestersBinding.inflate(inflater, container, false)
        val prefs = PrefsHelper(requireContext())

        // Generate dynamic buttons based on user setup (Years * Semesters per year)
        generateSemesterButtons(prefs.getYears(), prefs.getSemestersPerYear())
        return binding.root
    }

    private fun generateSemesterButtons(years: Int, sPerYear: Int) {
        binding.semesterContainer.removeAllViews()
        for (y in 1..years) {
            for (s in 1..sPerYear) {
                val btn = Button(requireContext()).apply {
                    val params = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, 8, 0, 8)
                    layoutParams = params
                    text = "Year $y - Semester $s"
                    isAllCaps = false
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}