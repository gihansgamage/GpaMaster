package com.gihansgamage.gpamaster.data

import androidx.room.*
import com.gihansgamage.gpamaster.models.UserData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_data WHERE id = 1")
    fun getUser(): Flow<UserData?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserData)

    @Update
    suspend fun updateUser(user: UserData)

    @Query("DELETE FROM user_data")
    suspend fun deleteUser()
}