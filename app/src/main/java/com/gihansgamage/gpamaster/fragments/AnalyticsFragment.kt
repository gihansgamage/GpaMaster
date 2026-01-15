package com.gihansgamage.gpamaster.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gihansgamage.gpamaster.R

class AnalyticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }
}
