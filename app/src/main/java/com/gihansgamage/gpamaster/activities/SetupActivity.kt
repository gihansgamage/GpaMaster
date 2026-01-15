package com.gihansgamage.gpamaster.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.gihansgamage.gpamaster.R
import com.gihansgamage.gpamaster.utils.PrefManager

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val etName = findViewById<EditText>(R.id.etName)
        val spScale = findViewById<Spinner>(R.id.spScale)
        val spYears = findViewById<Spinner>(R.id.spYears)
        val spSemesters = findViewById<Spinner>(R.id.spSemesters)
        val btnSave = findViewById<Button>(R.id.btnSave)

        spScale.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("4.0", "5.0", "10.0")
        )

        spYears.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            (1..10).toList()
        )

        spSemesters.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            (1..5).toList()
        )

        btnSave.setOnClickListener {
            if (etName.text.isEmpty()) {
                etName.error = "Enter name"
                return@setOnClickListener
            }

            val pref = PrefManager(this)
            pref.saveUser(
                etName.text.toString(),
                spScale.selectedItem.toString(),
                spYears.selectedItem as Int,
                spSemesters.selectedItem as Int
            )
            pref.setNotFirstTime()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
