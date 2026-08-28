package com.gihansgamage.gpamaster

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gihansgamage.gpamaster.utils.AdHelper
import com.gihansgamage.gpamaster.databinding.ActivityAllResultsBinding
import com.gihansgamage.gpamaster.utils.ExportHelper
import com.gihansgamage.gpamaster.utils.GPAHelper
import com.gihansgamage.gpamaster.utils.GradeMappingHelper
import com.gihansgamage.gpamaster.utils.PrefsHelper
import com.gihansgamage.gpamaster.utils.SemesterManager
import com.gihansgamage.gpamaster.utils.YearWeightHelper

class AllResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllResultsBinding
    private lateinit var prefs: PrefsHelper
    private lateinit var semesterManager: SemesterManager

    // Cached data for export
    private val semesterDataList = mutableListOf<ExportHelper.SemesterData>()
    private var overallGpa = 0.0
    private var weightedGpa: Double? = null
    private var totalCredits = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load AdMob Banner Ad via AdHelper
        AdHelper.loadBannerAd(binding.adViewAllResults)

        prefs = PrefsHelper(this)
        semesterManager = SemesterManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadResults()
        setupExportButtons()
    }

    override fun onPause() {
        binding.adViewAllResults.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adViewAllResults.resume()
    }

    override fun onDestroy() {
        binding.adViewAllResults.destroy()
        super.onDestroy()
    }

    private fun loadResults() {
        val scale = prefs.getScale()
        val gradeMappings = GradeMappingHelper.getGradeMappings(this, scale)

        semesterDataList.clear()
        var totalPoints = 0.0
        totalCredits = 0.0
        var totalSubjects = 0

        val container = binding.containerResults

        val allSemesters = semesterManager.getAllSemesters()
            .filter { it.totalCredits > 0 }
            .sortedWith(compareBy({ it.year }, { it.semesterNumber }))

        if (allSemesters.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No results entered yet.\nGo back and add some grades!"
                textSize = 16f
                setTextColor(Color.parseColor("#9E9E9E"))
                gravity = Gravity.CENTER
                setPadding(32, 80, 32, 32)
            }
            container.addView(emptyView)
            binding.tvSummaryGpa.text = "–"
            binding.tvSummaryCredits.text = "0"
            binding.tvSummarySubjects.text = "0"
            return
        }

        // Group by year
        val years = allSemesters.map { it.year }.distinct().sorted()

        for (year in years) {
            // Year header
            container.addView(buildYearHeader(year))

            val yearSemesters = allSemesters.filter { it.year == year }

            for (sem in yearSemesters) {
                val subjects = semesterManager.getSubjectsForSemester(sem.id)
                val subjectDataList = mutableListOf<ExportHelper.SubjectData>()

                var semPoints = 0.0
                var semCredits = 0.0

                subjects.forEach { subject ->
                    val gradePoints = gradeMappings[subject.grade] ?: 0.0
                    val pts = gradePoints * subject.credits
                    semPoints += pts
                    semCredits += subject.credits
                    totalPoints += pts
                    totalCredits += subject.credits
                    totalSubjects++
                    subjectDataList.add(
                        ExportHelper.SubjectData(
                            name = subject.name,
                            credits = subject.credits,
                            grade = subject.grade,
                            gradePoints = gradePoints
                        )
                    )
                }

                val semGpa = if (semCredits > 0) semPoints / semCredits else 0.0
                semesterDataList.add(
                    ExportHelper.SemesterData(
                        year = sem.year,
                        semesterNumber = sem.semesterNumber,
                        gpa = semGpa,
                        totalCredits = semCredits,
                        subjects = subjectDataList
                    )
                )

                // Semester card
                container.addView(buildSemesterCard(
                    sem.year, sem.semesterNumber, semGpa, semCredits, subjects.map { subject ->
                        Triple(subject.name, subject.credits, subject.grade)
                    }, gradeMappings, scale
                ))
            }
        }

        overallGpa = if (totalCredits > 0) totalPoints / totalCredits else 0.0

        // Calculate weighted GPA
        val weightedResult = YearWeightHelper.calculateWeightedGPA(this, semesterManager)
        val weights = YearWeightHelper.getYearWeights(this)
        val allWeightsEqual = YearWeightHelper.areWeightsEqual(weights)
        weightedGpa = if (weightedResult.weightedGPA > 0.0 && !allWeightsEqual) weightedResult.weightedGPA else null

        binding.tvSummaryGpa.text = GPAHelper.formatGPA(overallGpa, scale)
        binding.tvSummaryCredits.text = totalCredits.toInt().toString()
        binding.tvSummarySubjects.text = totalSubjects.toString()
    }

    // ── View builders ─────────────────────────────────────────────────────────

    private fun buildYearHeader(year: Int): TextView {
        return TextView(this).apply {
            text = "YEAR $year"
            textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#009688"))
            setPadding(0, dpToPx(8), 0, dpToPx(8))
        }
    }

    private fun buildSemesterCard(
        year: Int,
        semNum: Int,
        semGpa: Double,
        semCredits: Double,
        subjects: List<Triple<String, Double, String>>,
        gradeMappings: Map<String, Double>,
        scale: String
    ): View {
        val card = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { lp -> lp.bottomMargin = dpToPx(12) }
            radius = dpToPx(12).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(Color.WHITE)
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
        }

        // Semester header row
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dpToPx(10))
        }
        val semTitle = TextView(this).apply {
            text = "Semester $semNum"
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#263238"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val semStats = TextView(this).apply {
            text = "GPA ${String.format("%.2f", semGpa)} · ${semCredits.toInt()} cr"
            textSize = 13f
            setTextColor(Color.parseColor("#546E7A"))
            gravity = Gravity.END
        }
        headerRow.addView(semTitle)
        headerRow.addView(semStats)
        inner.addView(headerRow)

        // Divider
        inner.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).also {
                it.bottomMargin = dpToPx(8)
            }
            setBackgroundColor(Color.parseColor("#E0E0E0"))
        })

        // Column header
        inner.addView(buildTableRow("Subject", "Cr", "Grade", "Pts", isHeader = true))

        // Subject rows
        subjects.forEachIndexed { i, (name, credits, grade) ->
            val gradePoints = gradeMappings[grade] ?: 0.0
            inner.addView(buildTableRow(name, credits.toInt().toString(), grade, String.format("%.2f", gradePoints), isHeader = false, alt = i % 2 == 1))
        }

        card.addView(inner)
        return card
    }

    private fun buildTableRow(
        col1: String, col2: String, col3: String, col4: String,
        isHeader: Boolean, alt: Boolean = false
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            if (!isHeader && alt) setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(0, dpToPx(4), 0, dpToPx(4))
        }

        fun cell(text: String, weight: Float, alignEnd: Boolean = false): TextView =
            TextView(this).apply {
                this.text = text
                textSize = if (isHeader) 11f else 12f
                setTypeface(typeface, if (isHeader) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if (isHeader) Color.parseColor("#455A64") else Color.parseColor("#263238"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
                gravity = if (alignEnd) Gravity.END else Gravity.START
            }

        row.addView(cell(col1, 2.2f))
        row.addView(cell(col2, 0.5f, alignEnd = true))
        row.addView(cell(col3, 0.6f, alignEnd = true))
        row.addView(cell(col4, 0.7f, alignEnd = true))
        return row
    }

    // ── Export ────────────────────────────────────────────────────────────────

    private fun setupExportButtons() {
        val userName = prefs.getUserName()
        val scale = prefs.getScale()

        binding.btnExportPdf.setOnClickListener {
            try {
                val file = ExportHelper.exportPdf(this, userName, overallGpa, weightedGpa, scale, totalCredits, semesterDataList)
                ExportHelper.shareFile(this, file)
            } catch (e: Exception) {
                Toast.makeText(this, "PDF export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnExportTxt.setOnClickListener {
            try {
                val file = ExportHelper.exportTxt(this, userName, overallGpa, weightedGpa, scale, totalCredits, semesterDataList)
                ExportHelper.shareFile(this, file)
            } catch (e: Exception) {
                Toast.makeText(this, "TXT export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnExportCsv.setOnClickListener {
            try {
                val file = ExportHelper.exportCsv(this, userName, overallGpa, weightedGpa, scale, totalCredits, semesterDataList)
                ExportHelper.shareFile(this, file)
            } catch (e: Exception) {
                Toast.makeText(this, "CSV export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
