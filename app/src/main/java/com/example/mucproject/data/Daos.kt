package com.example.mucproject.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdherenceDao {
    @Query("SELECT * FROM CompartmentRecord ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CompartmentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CompartmentRecord)

    @Query("SELECT * FROM CompartmentRecord WHERE day = :day AND meal = :meal ORDER BY timestamp DESC LIMIT 1")
    fun getRecordForCompartment(day: Int, meal: MealType): Flow<CompartmentRecord?>

    @Query("DELETE FROM CompartmentRecord")
    suspend fun clearAllRecords()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM UserProfile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProfile(profile: UserProfile)
}
