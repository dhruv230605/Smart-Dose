package com.example.mucproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CompartmentRecord::class, UserProfile::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SmartDoseDatabase : RoomDatabase() {
    abstract fun adherenceDao(): AdherenceDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: SmartDoseDatabase? = null

        fun getDatabase(context: Context): SmartDoseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartDoseDatabase::class.java,
                    "smart_dose_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @androidx.room.TypeConverter
    fun toMealType(value: String): MealType {
        return try {
            MealType.valueOf(value)
        } catch (e: Exception) {
            MealType.BREAKFAST
        }
    }

    @androidx.room.TypeConverter
    fun fromStatus(status: Status): String = status.name

    @androidx.room.TypeConverter
    fun toStatus(value: String): Status {
        return try {
            Status.valueOf(value)
        } catch (e: Exception) {
            Status.PENDING
        }
    }
}
