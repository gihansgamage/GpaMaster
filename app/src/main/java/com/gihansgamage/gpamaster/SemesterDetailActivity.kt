package com.gihansgamage.gpamaster

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gihansgamage.gpamaster.adapters.SubjectAdapter
import com.gihansgamage.gpamaster.databinding.ActivitySemesterDetailBinding
import com.gihansgamage.gpamaster.databinding.DialogAddSubjectBinding
import com.gihansgamage.gpamaster.models.Subject
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.SharedPrefHelper

class SemesterDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySemesterDetailBinding
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var subjectAdapter: SubjectAdapter
    private val subjects = mutableListOf<Subject>()
    private var semesterId: Int = -1
    private var year: Int = 1
    private var semesterNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySemesterDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPrefHelper = SharedPrefHelper(this)
        semesterId = intent.getIntExtra("semester_id", -1)
        year = intent.getIntExtra("year", 1)
        semesterNumber = intent.getIntExtra("semester_number", 1)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        calculateGPA()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Year $year - Semester $semesterNumber"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(subjects) { subject -> showEditSubjectDialog(subject) }
        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(this@SemesterDetailActivity)
            adapter = subjectAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSubject.setOnClickListener { showAddSubjectDialog() }
        binding.btnCalculate.setOnClickListener { calculateGPA() }
    }

    private fun showAddSubjectDialog() {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0") ?: "4.0"

        val grades = GPAHelper.getAvailableGrades(scale)
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerGrade.adapter = gradeAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etSubjectName.text.toString()
                val credits = dialogBinding.etCredits.text.toString().toDoubleOrNull() ?: 0.0
                val grade = dialogBinding.spinnerGrade.selectedItem.toString()
                val percentage = dialogBinding.etPercentage.text.toString().toDoubleOrNull()

                if (name.isNotEmpty() && credits > 0) {
                    // Fixed: Parameter order matches Subject.kt model
                    val newSubject = Subject(
                        semesterId = semesterId,
                        name = name,
                        credits = credits,
                        grade = grade,
                        percentage = percentage
                    )
                    subjects.add(newSubject)
                    subjectAdapter.notifyDataSetChanged()
                    calculateGPA()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialogBinding.tilPercentage.visibility = if (scale == "percentage") View.VISIBLE else View.GONE
        dialog.show()
    }

    private fun showEditSubjectDialog(subject: Subject) {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0") ?: "4.0"

        dialogBinding.etSubjectName.setText(subject.name)
        dialogBinding.etCredits.setText(subject.credits.toString())

        val grades = GPAHelper.getAvailableGrades(scale)
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerGrade.adapter = gradeAdapter

        val gradeIndex = grades.indexOf(subject.grade)
        if (gradeIndex != -1) dialogBinding.spinnerGrade.setSelection(gradeIndex)

        if (scale == "percentage") {
            dialogBinding.tilPercentage.visibility = View.VISIBLE
            dialogBinding.etPercentage.setText(subject.percentage?.toString() ?: "")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.etSubjectName.text.toString()
                val credits = dialogBinding.etCredits.text.toString().toDoubleOrNull() ?: 0.0
                val grade = dialogBinding.spinnerGrade.selectedItem.toString()
                val percentage = dialogBinding.etPercentage.text.toString().toDoubleOrNull()

                if (name.isNotEmpty() && credits > 0) {
                    val index = subjects.indexOfFirst { it.id == subject.id }
                    if (index != -1) {
                        subjects[index] = subject.copy(
                            name = name,
                            credits = credits,
                            grade = grade,
                            percentage = percentage
                        )
                        subjectAdapter.notifyItemChanged(index)
                        calculateGPA()
                    }
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                subjects.removeAll { it.id == subject.id }
                subjectAdapter.notifyDataSetChanged()
                calculateGPA()
            }
            .setNeutralButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun calculateGPA() {
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0") ?: "4.0"
        // Fixed: Destructuring Pair now works correctly
        val (gpa, totalCredits) = GPAHelper.calculateSemesterGPA(subjects, scale)

        binding.tvGpa.text = "GPA: ${GPAHelper.formatGPA(gpa, scale)}"
        binding.tvTotalCredits.text = "Total Credits: $totalCredits"
    }
}