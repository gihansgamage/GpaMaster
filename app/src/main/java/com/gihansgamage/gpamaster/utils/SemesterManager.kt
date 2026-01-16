package com.gihansgamage.gpamaster.utils

import android.content.Context
import com.gihansgamage.gpamaster.models.Semester
import com.gihansgamage.gpamaster.models.Subject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SemesterManager(private val context: Context) {

    private val prefs = PrefsHelper(context)
    private val gson = Gson()

    // Get all semesters
    fun getAllSemesters(): List<Semester> {
        val json = prefs.getString("semesters", "[]")
        val type = object : TypeToken<List<Semester>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Save all semesters
    private fun saveSemesters(semesters: List<Semester>) {
        val json = gson.toJson(semesters)
        prefs.saveString("semesters", json)
    }

    // Get or create semester
    fun getOrCreateSemester(year: Int, semesterNumber: Int): Semester {
        val semesters = getAllSemesters().toMutableList()
        val existing = semesters.find { it.year == year && it.semesterNumber == semesterNumber }

        if (existing != null) {
            return existing
        }

        // Create new semester
        val newId = (semesters.maxOfOrNull { it.id } ?: 0) + 1
        val newSemester = Semester(
            id = newId,
            year = year,
            semesterNumber = semesterNumber,
            gpa = 0.0,
            totalCredits = 0.0
        )

        semesters.add(newSemester)
        saveSemesters(semesters)
        return newSemester
    }

    // Update semester
    fun updateSemester(semester: Semester) {
        val semesters = getAllSemesters().toMutableList()
        val index = semesters.indexOfFirst { it.id == semester.id }
        if (index != -1) {
            semesters[index] = semester
            saveSemesters(semesters)
        }
    }

    // Get subjects for semester
    fun getSubjectsForSemester(semesterId: Int): List<Subject> {
        val json = prefs.getString("subjects_$semesterId", "[]")
        val type = object : TypeToken<List<Subject>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // Save subjects for semester
    fun saveSubjectsForSemester(semesterId: Int, subjects: List<Subject>) {
        val json = gson.toJson(subjects)
        prefs.saveString("subjects_$semesterId", json)

        // Recalculate semester GPA
        recalculateSemesterGPA(semesterId)
    }

    // Add subject to semester
    fun addSubject(semesterId: Int, subject: Subject) {
        val subjects = getSubjectsForSemester(semesterId).toMutableList()
        val newId = (subjects.maxOfOrNull { it.id } ?: 0) + 1
        val newSubject = subject.copy(id = newId, semesterId = semesterId)
        subjects.add(newSubject)
        saveSubjectsForSemester(semesterId, subjects)
    }

    // Update subject
    fun updateSubject(semesterId: Int, subject: Subject) {
        val subjects = getSubjectsForSemester(semesterId).toMutableList()
        val index = subjects.indexOfFirst { it.id == subject.id }
        if (index != -1) {
            subjects[index] = subject
            saveSubjectsForSemester(semesterId, subjects)
        }
    }

    // Delete subject
    fun deleteSubject(semesterId: Int, subjectId: Int) {
        val subjects = getSubjectsForSemester(semesterId).toMutableList()
        subjects.removeAll { it.id == subjectId }
        saveSubjectsForSemester(semesterId, subjects)
    }

    // Recalculate semester GPA
    private fun recalculateSemesterGPA(semesterId: Int) {
        val subjects = getSubjectsForSemester(semesterId)
        val scale = prefs.getScale()

        val (gpa, totalCredits) = GPAHelper.calculateSemesterGPA(subjects, scale)

        val semesters = getAllSemesters().toMutableList()
        val index = semesters.indexOfFirst { it.id == semesterId }
        if (index != -1) {
            semesters[index] = semesters[index].copy(
                gpa = gpa,
                totalCredits = totalCredits
            )
            saveSemesters(semesters)
        }
    }

    // Calculate overall GPA
    fun calculateOverallGPA(): Pair<Double, Double> {
        val semesters = getAllSemesters()
        var totalPoints = 0.0
        var totalCredits = 0.0

        semesters.forEach { semester ->
            val subjects = getSubjectsForSemester(semester.id)
            subjects.forEach { subject ->
                val gradePoints = GPAHelper.getGradePoints(prefs.getScale())[subject.grade] ?: 0.0
                totalPoints += gradePoints * subject.credits
                totalCredits += subject.credits
            }
        }

        val overallGPA = if (totalCredits > 0) totalPoints / totalCredits else 0.0
        return Pair(overallGPA, totalCredits)
    }

    // Reset all data
    fun resetAllData() {
        val semesters = getAllSemesters()
        semesters.forEach { semester ->
            prefs.saveString("subjects_${semester.id}", "[]")
        }
        saveSemesters(emptyList())
    }

    // Initialize semesters based on user settings
    fun initializeSemesters() {
        val years = prefs.getYears()
        val semestersPerYear = prefs.getSemestersPerYear()

        for (year in 1..years) {
            for (semester in 1..semestersPerYear) {
                getOrCreateSemester(year, semester)
            }
        }
    }
}