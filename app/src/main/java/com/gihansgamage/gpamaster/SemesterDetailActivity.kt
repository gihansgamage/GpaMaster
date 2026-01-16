package com.gihansgamage.gpamaster

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gihansgamage.gpamaster.adapters.SubjectAdapter
import com.gihansgamage.gpamaster.databinding.ActivitySemesterDetailBinding
import com.gihansgamage.gpamaster.databinding.DialogAddSubjectBinding
import com.gihansgamage.gpamaster.models.Subject
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.SharedPrefHelper
import android.widget.ArrayAdapter

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

        // Get semester info from intent
        semesterId = intent.getIntExtra("semester_id", -1)
        year = intent.getIntExtra("year", 1)
        semesterNumber = intent.getIntExtra("semester_number", 1)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadSubjects()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Year $year - Semester $semesterNumber"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectAdapter(subjects) { subject ->
            showEditSubjectDialog(subject)
        }

        binding.rvSubjects.apply {
            layoutManager = LinearLayoutManager(this@SemesterDetailActivity)
            adapter = subjectAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddSubject.setOnClickListener {
            showAddSubjectDialog()
        }

        binding.btnCalculate.setOnClickListener {
            calculateGPA()
        }
    }

    private fun showAddSubjectDialog() {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val subjectName = dialogBinding.etSubjectName.text.toString()
                val credits = dialogBinding.etCredits.text.toString().toDoubleOrNull() ?: 0.0
                val grade = dialogBinding.spinnerGrade.selectedItem.toString()
                val percentage = dialogBinding.etPercentage.text.toString().toDoubleOrNull()

                if (subjectName.isNotEmpty() && credits > 0) {
                    val newSubject = Subject(
                        semesterId = semesterId,
                        name = subjectName,
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

        // Setup grade spinner
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0")
        val grades = GPAHelper.getAvailableGrades(scale)
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerGrade.adapter = gradeAdapter

        // Show/hide percentage based on scale
        if (scale == "percentage") {
            dialogBinding.tilPercentage.visibility = android.view.View.VISIBLE
        } else {
            dialogBinding.tilPercentage.visibility = android.view.View.GONE
        }

        dialog.show()
    }

    private fun showEditSubjectDialog(subject: Subject) {
        val dialogBinding = DialogAddSubjectBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Subject")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val subjectName = dialogBinding.etSubjectName.text.toString()
                val credits = dialogBinding.etCredits.text.toString().toDoubleOrNull() ?: 0.0
                val grade = dialogBinding.spinnerGrade.selectedItem.toString()
                val percentage = dialogBinding.etPercentage.text.toString().toDoubleOrNull()

                if (subjectName.isNotEmpty() && credits > 0) {
                    val index = subjects.indexOfFirst { it.id == subject.id }
                    if (index != -1) {
                        subjects[index] = subject.copy(
                            name = subjectName,
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

        // Pre-fill data
        dialogBinding.etSubjectName.setText(subject.name)
        dialogBinding.etCredits.setText(subject.credits.toString())

        // Setup grade spinner
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0")
        val grades = GPAHelper.getAvailableGrades(scale)
        val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerGrade.adapter = gradeAdapter

        val gradeIndex = grades.indexOf(subject.grade)
        if (gradeIndex != -1) {
            dialogBinding.spinnerGrade.setSelection(gradeIndex)
        }

        // Show/hide percentage based on scale
        if (scale == "percentage") {
            dialogBinding.tilPercentage.visibility = android.view.View.VISIBLE
            dialogBinding.etPercentage.setText(subject.percentage?.toString() ?: "")
        } else {
            dialogBinding.tilPercentage.visibility = android.view.View.GONE
        }

        dialog.show()
    }

    private fun calculateGPA() {
        val scale = sharedPrefHelper.getString("gpa_scale", "4.0")
        val (gpa, totalCredits) = GPAHelper.calculateSemesterGPA(subjects, scale)

        binding.tvGpa.text = "GPA: ${GPAHelper.formatGPA(gpa, scale)}"
        binding.tvTotalCredits.text = "Total Credits: $totalCredits"

        // Update in database (you'll need to implement this)
        // semesterDao.updateSemester(semester.copy(gpa = gpa, totalCredits = totalCredits))
    }

    private fun loadSubjects() {
        // Load subjects from database based on semesterId
        // This is a placeholder - implement actual database loading
        calculateGPA()
    }
}