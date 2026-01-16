package com.gihansgamage.gpamaster

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gihansgamage.gpamaster.adapters.SubjectAdapter
import com.gihansgamage.gpamaster.databinding.ActivitySemesterDetailBinding
import com.gihansgamage.gpamaster.models.Subject
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager

class SemesterDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySemesterDetailBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager
    private lateinit var subjectAdapter: SubjectAdapter
    private val subjects = mutableListOf<Subject>()
    private var semesterId: Int = -1
    private var year: Int = 1
    private var semesterNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySemesterDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            prefs = PrefsHelper(this)
            semesterManager = SemesterManager(this)

            year = intent.getIntExtra("year", 1)
            semesterNumber = intent.getIntExtra("semester_number", 1)

            Log.d("SemesterDetail", "Opening Year $year, Semester $semesterNumber")

            val semester = semesterManager.getOrCreateSemester(year, semesterNumber)
            semesterId = semester.id

            Log.d("SemesterDetail", "Semester ID: $semesterId")

            setupToolbar()
            setupRecyclerView()
            loadSubjects()
            setupClickListeners()
            updateGPADisplay()

        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error in onCreate", e)
            Toast.makeText(this, "Error loading semester: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Year $year - Semester $semesterNumber"
            binding.toolbar.setNavigationOnClickListener { finish() }
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error setting up toolbar", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            subjectAdapter = SubjectAdapter(subjects) { subject ->
                showEditSubjectDialog(subject)
            }
            binding.rvSubjects.apply {
                layoutManager = LinearLayoutManager(this@SemesterDetailActivity)
                adapter = subjectAdapter
            }
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error setting up RecyclerView", e)
        }
    }

    private fun loadSubjects() {
        try {
            subjects.clear()
            subjects.addAll(semesterManager.getSubjectsForSemester(semesterId))
            subjectAdapter.notifyDataSetChanged()
            Log.d("SemesterDetail", "Loaded ${subjects.size} subjects")
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error loading subjects", e)
            Toast.makeText(this, "Error loading subjects", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        try {
            binding.fabAddSubject.setOnClickListener {
                Log.d("SemesterDetail", "FAB clicked")
                showAddSubjectDialog()
            }

            binding.btnCalculate.setOnClickListener {
                updateGPADisplay()
                Toast.makeText(this, "GPA Updated!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error setting up click listeners", e)
        }
    }

    private fun showAddSubjectDialog() {
        try {
            Log.d("SemesterDetail", "Showing add subject dialog")

            // Inflate the dialog layout
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
            val scale = prefs.getScale()

            // Get views from dialog
            val etSubjectName = dialogView.findViewById<EditText>(R.id.et_subject_name)
            val etCredits = dialogView.findViewById<EditText>(R.id.et_credits)
            val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinner_grade)
            val etPercentage = dialogView.findViewById<EditText>(R.id.et_percentage)
            val tvPercentageLabel = dialogView.findViewById<TextView>(R.id.tv_percentage_label)

            // Setup grade spinner
            val grades = GPAHelper.getAvailableGrades(scale)
            val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGrade.adapter = gradeAdapter

            // Show percentage field only for percentage scale
            if (scale == "percentage") {
                etPercentage?.visibility = View.VISIBLE
                tvPercentageLabel?.visibility = View.VISIBLE
            } else {
                etPercentage?.visibility = View.GONE
                tvPercentageLabel?.visibility = View.GONE
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Add Subject")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    try {
                        val name = etSubjectName.text.toString().trim()
                        val creditsStr = etCredits.text.toString().trim()
                        val grade = spinnerGrade.selectedItem?.toString() ?: "A"
                        val percentageStr = etPercentage?.text?.toString()?.trim() ?: ""

                        Log.d("SemesterDetail", "Adding subject: $name, $creditsStr credits, grade $grade")

                        // Validation
                        if (name.isEmpty()) {
                            Toast.makeText(this, "Please enter subject name", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val credits = creditsStr.toDoubleOrNull()
                        if (credits == null || credits <= 0) {
                            Toast.makeText(this, "Please enter valid credits (e.g., 3 or 3.5)", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }

                        val percentage = if (scale == "percentage" && percentageStr.isNotEmpty()) {
                            percentageStr.toDoubleOrNull()
                        } else null

                        val newSubject = Subject(
                            id = 0,
                            semesterId = semesterId,
                            name = name,
                            credits = credits,
                            grade = grade,
                            percentage = percentage
                        )

                        semesterManager.addSubject(semesterId, newSubject)
                        loadSubjects()
                        updateGPADisplay()

                        Toast.makeText(this, "Subject added successfully!", Toast.LENGTH_SHORT).show()
                        Log.d("SemesterDetail", "Subject added successfully")

                    } catch (e: Exception) {
                        Log.e("SemesterDetail", "Error adding subject", e)
                        Toast.makeText(this, "Error adding subject: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()

        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error showing dialog", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditSubjectDialog(subject: Subject) {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
            val scale = prefs.getScale()

            val etSubjectName = dialogView.findViewById<EditText>(R.id.et_subject_name)
            val etCredits = dialogView.findViewById<EditText>(R.id.et_credits)
            val spinnerGrade = dialogView.findViewById<Spinner>(R.id.spinner_grade)
            val etPercentage = dialogView.findViewById<EditText>(R.id.et_percentage)
            val tvPercentageLabel = dialogView.findViewById<TextView>(R.id.tv_percentage_label)

            etSubjectName.setText(subject.name)
            etCredits.setText(subject.credits.toString())

            val grades = GPAHelper.getAvailableGrades(scale)
            val gradeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
            gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGrade.adapter = gradeAdapter

            val gradeIndex = grades.indexOf(subject.grade)
            if (gradeIndex != -1) {
                spinnerGrade.setSelection(gradeIndex)
            }

            if (scale == "percentage") {
                etPercentage?.visibility = View.VISIBLE
                tvPercentageLabel?.visibility = View.VISIBLE
                etPercentage?.setText(subject.percentage?.toString() ?: "")
            } else {
                etPercentage?.visibility = View.GONE
                tvPercentageLabel?.visibility = View.GONE
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Edit Subject")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    try {
                        val name = etSubjectName.text.toString().trim()
                        val creditsStr = etCredits.text.toString().trim()
                        val grade = spinnerGrade.selectedItem?.toString() ?: subject.grade
                        val percentageStr = etPercentage?.text?.toString()?.trim() ?: ""

                        if (name.isEmpty()) {
                            Toast.makeText(this, "Please enter subject name", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val credits = creditsStr.toDoubleOrNull()
                        if (credits == null || credits <= 0) {
                            Toast.makeText(this, "Please enter valid credits", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        val percentage = if (scale == "percentage" && percentageStr.isNotEmpty()) {
                            percentageStr.toDoubleOrNull()
                        } else null

                        val updatedSubject = subject.copy(
                            name = name,
                            credits = credits,
                            grade = grade,
                            percentage = percentage
                        )

                        semesterManager.updateSubject(semesterId, updatedSubject)
                        loadSubjects()
                        updateGPADisplay()

                        Toast.makeText(this, "Subject updated!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("SemesterDetail", "Error updating subject", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Delete") { _, _ ->
                    AlertDialog.Builder(this)
                        .setTitle("Delete Subject")
                        .setMessage("Delete ${subject.name}?")
                        .setPositiveButton("Delete") { _, _ ->
                            try {
                                semesterManager.deleteSubject(semesterId, subject.id)
                                loadSubjects()
                                updateGPADisplay()
                                Toast.makeText(this, "Subject deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("SemesterDetail", "Error deleting subject", e)
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
                .setNeutralButton("Cancel", null)
                .create()

            dialog.show()

        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error showing edit dialog", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateGPADisplay() {
        try {
            val scale = prefs.getScale()
            val subjects = semesterManager.getSubjectsForSemester(semesterId)
            val (gpa, totalCredits) = GPAHelper.calculateSemesterGPA(subjects, scale)

            binding.tvGpa.text = "GPA: ${GPAHelper.formatGPA(gpa, scale)}"
            binding.tvTotalCredits.text = "Total Credits: ${totalCredits.toInt()}"

            Log.d("SemesterDetail", "GPA: $gpa, Credits: $totalCredits")
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error updating GPA display", e)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            loadSubjects()
            updateGPADisplay()
        } catch (e: Exception) {
            Log.e("SemesterDetail", "Error in onResume", e)
        }
    }
}