package com.gihansgamage.gpamaster

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.gihansgamage.gpamaster.utils.PrefsHelper

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etName = findViewById<EditText>(R.id.et_name)
        val spinnerScale = findViewById<Spinner>(R.id.spinner_scale)
        val spinnerYears = findViewById<Spinner>(R.id.spinner_years)
        val spinnerSemesters = findViewById<Spinner>(R.id.spinner_semesters)
        val btnStart = findViewById<Button>(R.id.btn_start)

        setupUI(etName, spinnerScale, spinnerYears, spinnerSemesters, btnStart)
    }

    private fun setupUI(
        etName: EditText,
        spinnerScale: Spinner,
        spinnerYears: Spinner,
        spinnerSemesters: Spinner,
        btnStart: Button
    ) {
        // Setup scale spinner
        val scales = arrayOf("4.0 Scale", "5.0 Scale", "10.0 Scale", "Percentage Scale")
        val scaleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, scales)
        scaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerScale.adapter = scaleAdapter

        // Setup year spinner
        val years = arrayOf("1 year", "2 years", "3 years", "4 years", "5 years",
            "6 years", "7 years", "8 years", "9 years", "10 years")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYears.adapter = yearAdapter

        // Setup semesters spinner
        val semesters = arrayOf("1 semester per year", "2 semesters per year",
            "3 semesters per year", "4 semesters per year",
            "5 semesters per year")
        val semesterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, semesters)
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSemesters.adapter = semesterAdapter

        // Set default selections
        spinnerScale.setSelection(0)
        spinnerYears.setSelection(3) // 4 years
        spinnerSemesters.setSelection(1) // 2 semesters per year

        // Start button click
        btnStart.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Please enter your name"
                return@setOnClickListener
            }

            // Get selected values
            val scale = when (spinnerScale.selectedItemPosition) {
                0 -> "4.0"
                1 -> "5.0"
                2 -> "10.0"
                3 -> "percentage"
                else -> "4.0"
            }

            val years = spinnerYears.selectedItemPosition + 1
            val semestersPerYear = spinnerSemesters.selectedItemPosition + 1

            // Save to preferences
            val prefs = PrefsHelper(this)
            prefs.saveUserName(name)
            prefs.saveScale(scale)
            prefs.saveYears(years)
            prefs.saveSemestersPerYear(semestersPerYear)
            prefs.saveSetupCompleted(true)

            // Go to main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}