package com.gihansgamage.gpamaster.data

import com.gihansgamage.gpamaster.models.Semester
import com.gihansgamage.gpamaster.models.Subject
import com.gihansgamage.gpamaster.models.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GPARepository @Inject constructor(
    private val userDao: UserDao,
    private val semesterDao: SemesterDao,
    private val subjectDao: SubjectDao
) {
    // User operations
    fun getUser(): Flow<UserData?> = userDao.getUser()
    suspend fun insertUser(user: UserData) = userDao.insertUser(user)
    suspend fun updateUser(user: UserData) = userDao.updateUser(user)
    suspend fun deleteUser() = userDao.deleteUser()

    // Semester operations
    fun getAllSemesters(): Flow<List<Semester>> = semesterDao.getAllSemesters()
    fun getSemesterById(semesterId: Int): Flow<Semester?> = semesterDao.getSemesterById(semesterId)
    suspend fun insertSemester(semester: Semester) = semesterDao.insertSemester(semester)
    suspend fun updateSemester(semester: Semester) = semesterDao.updateSemester(semester)
    suspend fun deleteSemester(semester: Semester) = semesterDao.deleteSemester(semester)
    suspend fun deleteAllSemesters() = semesterDao.deleteAllSemesters()

    // Subject operations
    fun getSubjectsBySemester(semesterId: Int): Flow<List<Subject>> = subjectDao.getSubjectsBySemester(semesterId)
    suspend fun insertSubject(subject: Subject) = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)
    suspend fun deleteSubjectsBySemester(semesterId: Int) = subjectDao.deleteSubjectsBySemester(semesterId)
}