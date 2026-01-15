package com.gihansgamage.gpamaster.data

import androidx.room.*
import com.gihansgamage.gpamaster.models.Semester
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semesters ORDER BY year, semesterNumber")
    fun getAllSemesters(): Flow<List<Semester>>

    @Query("SELECT * FROM semesters WHERE id = :semesterId")
    fun getSemesterById(semesterId: Int): Flow<Semester?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: Semester)

    @Update
    suspend fun updateSemester(semester: Semester)

    @Delete
    suspend fun deleteSemester(semester: Semester)

    @Query("DELETE FROM semesters")
    suspend fun deleteAllSemesters()
}