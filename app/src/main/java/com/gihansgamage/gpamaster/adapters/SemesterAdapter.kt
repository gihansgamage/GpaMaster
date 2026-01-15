package com.gihansgamage.gpamaster.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.models.Semester
import com.gihansgamage.gpamaster.utils.GPAHelper

class SemesterAdapter(
    private var semesters: List<Semester>,
    private val onItemClick: (Semester) -> Unit
) : RecyclerView.Adapter<SemesterAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card_semester)
        val tvSemesterName: TextView = view.findViewById(R.id.tv_semester_name)
        val tvGpa: TextView = view.findViewById(R.id.tv_gpa)
        val tvCredits: TextView = view.findViewById(R.id.tv_credits)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_semester, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val semester = semesters[position]

        holder.tvSemesterName.text = semester.getDisplayName()
        holder.tvGpa.text = "GPA: ${String.format("%.2f", semester.gpa)}"
        holder.tvCredits.text = "Credits: ${semester.totalCredits}"

        // Set color based on GPA
        val color = when {
            semester.gpa >= 3.5 -> R.color.green
            semester.gpa >= 3.0 -> R.color.orange
            semester.gpa >= 2.0 -> R.color.yellow
            else -> R.color.red
        }

        holder.cardView.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, color)
        )

        holder.cardView.setOnClickListener {
            onItemClick(semester)
        }
    }

    override fun getItemCount() = semesters.size

    fun updateData(newSemesters: List<Semester>) {
        semesters = newSemesters
        notifyDataSetChanged()
    }
}