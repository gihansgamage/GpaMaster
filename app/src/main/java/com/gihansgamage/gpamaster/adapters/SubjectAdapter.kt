package com.gihansgamage.gpamaster.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.models.Subject

class SubjectAdapter(
    private var subjects: List<Subject>,
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSubjectName: TextView = view.findViewById(R.id.tv_subject_name)
        val tvCredits: TextView = view.findViewById(R.id.tv_credits)
        val tvGrade: TextView = view.findViewById(R.id.tv_grade)
        val tvPoints: TextView = view.findViewById(R.id.tv_points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]

        holder.tvSubjectName.text = subject.name
        holder.tvCredits.text = "Credits: ${subject.credits}"
        holder.tvGrade.text = "Grade: ${subject.grade}"

        if (subject.percentage != null) {
            holder.tvPoints.text = "Percentage: ${subject.percentage}%"
        } else {
            holder.tvPoints.text = ""
        }

        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    override fun getItemCount() = subjects.size

    fun updateData(newSubjects: List<Subject>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }
}