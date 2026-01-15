package com.gihansgamage..gpamaster.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gpamaster.R
import com.example.gpamaster.utils.PrefManager

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val pref = PrefManager(requireContext())

        view.findViewById<TextView>(R.id.tvSettings)
            .text = "Scale: ${pref.getScale()}\nYears: ${pref.getYears()}"

        return view
    }
}
