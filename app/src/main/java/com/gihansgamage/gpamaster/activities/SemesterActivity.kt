package com.gihansgamage.gpamaster.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gpamaster.R
import com.example.gpamaster.models.Subject
import com.example.gpamaster.utils.GPAUtils
import com.example.gpamaster.utils.PrefManager

class SemesterActivity : AppCompatActivity() {

    private val subjects = mutableListOf<Subject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_semester)

        val etSub = findViewById<EditText>(R.id.etSubject)
        val etCredits = findViewById<EditText>(R.id.etCredits)
        val spGrade = findViewById<Spinner>(R.id.spGrade)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val tvGpa = findViewById<TextView>(R.id.tvSemesterGpa)

        spGrade.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F")
        )

        btnAdd.setOnClickListener {
            val subject = Subject(
                etSub.text.toString(),
                etCredits.text.toString().toInt(),
                spGrade.selectedItem.toString()
            )
            subjects.add(subject)

            val scale = PrefManager(this).getScale()
            val gpa = GPAUtils.calculateGPA(subjects, scale)
            tvGpa.text = "Semester GPA: %.2f".format(gpa)

            etSub.text.clear()
            etCredits.text.clear()
        }
    }
}
