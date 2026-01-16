package com.gihansgamage.gpamaster.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.databinding.FragmentHomeBinding
import com.gihansgamage.gpamaster.utils.PrefsHelper

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PrefsHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        prefs = PrefsHelper(requireContext())

        setupDashboard()
        return binding.root
    }

    private fun setupDashboard() {
        binding.tvWelcome.text = "Hi, ${prefs.getUserName()} 👋"
        // In a real app, calculate these from the Room Database
        binding.tvCurrentGpa.text = "0.00"
        binding.tvTotalCredits.text = "0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}