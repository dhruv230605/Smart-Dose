package com.example.mucproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType {
    BREAKFAST, LUNCH, DINNER
}

enum class Status {
    PENDING, TAKEN, MISSED, DELAYED
}

@Entity
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val age: Int,
    val breakfastTime: String = "08:00",
    val lunchTime: String = "13:00",
    val dinnerTime: String = "19:00",
    val largeTextMode: Boolean = false,
    val highContrastMode: Boolean = false
)

@Entity
data class CompartmentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val day: Int, // 0-6 (Mon-Sun)
    val meal: MealType,
    var status: Status,
    val scheduledTime: Long,
    val takenTime: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class UserRole {
    ELDER, CAREGIVER
}

data class UserSession(
    val email: String,
    val role: UserRole,
    val isLoggedIn: Boolean
)
