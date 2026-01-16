package com.gihansgamage.gpamaster.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.gihansgamage.gpamaster.models.Subject

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE semesterId = :semesterId ORDER BY createdAt")
    fun getSubjectsBySemester(semesterId: Int): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE semesterId = :semesterId")
    suspend fun deleteSubjectsBySemester(semesterId: Int)
}