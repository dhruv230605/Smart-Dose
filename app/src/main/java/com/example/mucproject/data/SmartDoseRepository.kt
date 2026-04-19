package com.example.mucproject.data

import kotlinx.coroutines.flow.Flow

class SmartDoseRepository(
    private val adherenceDao: AdherenceDao,
    private val userProfileDao: UserProfileDao
) {
    val allRecords: Flow<List<CompartmentRecord>> = adherenceDao.getAllRecords()

    suspend fun insertRecord(record: CompartmentRecord) {
        adherenceDao.insertRecord(record)
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateProfile(profile)
    }

    suspend fun clearAllRecords() {
        adherenceDao.clearAllRecords()
    }
}
