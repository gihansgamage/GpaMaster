package com.gihansgamage.gpamaster.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.activities.SemesterActivity
import com.gihansgamage.gpamaster.utils.PrefManager

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        val btnSemester = view.findViewById<Button>(R.id.btnSemester)

        val pref = PrefManager(requireContext())
        tvWelcome.text = "Hi ${pref.getName()} 👋"

        btnSemester.setOnClickListener {
            startActivity(Intent(requireContext(), SemesterActivity::class.java))
        }

        return view
    }
}
