package com.example.mucproject.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mucproject.data.*
import com.example.mucproject.utils.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class SmartDoseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SmartDoseRepository
    private val dataStoreManager = DataStoreManager(application)
    private val notificationHelper = NotificationHelper(application)
    private val apiService = ApiService.create()

    val allRecords: StateFlow<List<CompartmentRecord>>
    val userSession: StateFlow<UserSession?> = dataStoreManager.userSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val userProfile: StateFlow<UserProfile?>

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _currentDay = MutableStateFlow(0)
    val currentDay: StateFlow<Int> = _currentDay

    private val _currentMeal = MutableStateFlow(MealType.BREAKFAST)
    val currentMeal: StateFlow<MealType> = _currentMeal

    private val _isFillMode = MutableStateFlow(false)
    val isFillMode: StateFlow<Boolean> = _isFillMode

    private val _fillDay = MutableStateFlow(0)
    val fillDay: StateFlow<Int> = _fillDay

    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode

    init {
        val database = SmartDoseDatabase.getDatabase(application)
        repository = SmartDoseRepository(
            database.adherenceDao(),
            database.userProfileDao()
        )

        allRecords = repository.allRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        userProfile = database.userProfileDao().getUserProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile(id = 1, name = "User", age = 70))

        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val event = apiService.getEvent()
                processEvent(event)
            } catch (e: Exception) {
                _connectionStatus.value = false
                Log.e("SmartDose", "Refresh failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun processEvent(event: EventResponse) {
        _connectionStatus.value = true
        val completedDay = event.day
        val completedMealType = mapMeal(event.meal)

        // Mark this completed meal as TAKEN
        onCompartmentOpened(completedDay, completedMealType)

        // Reset All logic: If we just finished Sunday Dinner (Day 6, Meal 2)
        if (completedDay == 6 && completedMealType == MealType.DINNER) {
            viewModelScope.launch {
                repository.clearAllRecords()
                triggerFeedback("Week complete! Records reset.")
            }
        }

        // Calculate Next Target for UI
        var nextDay = completedDay
        val nextMealType = when (completedMealType) {
            MealType.BREAKFAST -> MealType.LUNCH
            MealType.LUNCH -> MealType.DINNER
            MealType.DINNER -> {
                nextDay = (completedDay + 1) % 7
                MealType.BREAKFAST
            }
        }

        _currentDay.value = nextDay
        _currentMeal.value = nextMealType
    }

    fun fastForwardMeal() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Call server to increment its internal day/meal
                val event = apiService.fastForward()
                processEvent(event)
            } catch (e: Exception) {
                Log.e("SmartDose", "Fast Forward failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllRecords()
            triggerFeedback("All records cleared.")
        }
    }

    private fun mapMeal(meal: Int): MealType {
        return when(meal) {
            0 -> MealType.BREAKFAST
            1 -> MealType.LUNCH
            2 -> MealType.DINNER
            else -> MealType.BREAKFAST
        }
    }

    fun toggleFillMode() {
        _isFillMode.value = !_isFillMode.value
        if (_isFillMode.value) _fillDay.value = 0
    }

    fun confirmBreakfastFill() {
        if (_fillDay.value < 6) {
            _fillDay.value += 1
        } else {
            _isFillMode.value = false
        }
    }

    fun toggleTestMode() {
        _isTestMode.value = !_isTestMode.value
    }

    fun login(email: String, password: String, role: UserRole, rememberMe: Boolean) {
        viewModelScope.launch {
            dataStoreManager.saveSession(email, role, rememberMe)
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearSession()
        }
    }

    fun onCompartmentOpened(day: Int, meal: MealType, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val existing = allRecords.value.any { it.day == day && it.meal == meal && (it.status == Status.TAKEN || it.status == Status.DELAYED) }
            if (existing && !_isTestMode.value) return@launch

            val profile = userProfile.value ?: UserProfile(id = 1, name = "User", age = 70)
            val scheduledTimeStr = when(meal) {
                MealType.BREAKFAST -> profile.breakfastTime
                MealType.LUNCH -> profile.lunchTime
                MealType.DINNER -> profile.dinnerTime
            }
            
            val scheduledParts = scheduledTimeStr.split(":")
            val scheduledCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, scheduledParts[0].toInt())
                set(Calendar.MINUTE, scheduledParts[1].toInt())
                set(Calendar.SECOND, 0)
            }
            
            val windowEnd = scheduledCal.timeInMillis + (30 * 60 * 1000)
            val status = if (timestamp <= windowEnd) Status.TAKEN else Status.DELAYED
            
            val record = CompartmentRecord(
                day = day,
                meal = meal,
                status = status,
                scheduledTime = scheduledCal.timeInMillis,
                takenTime = timestamp
            )
            repository.insertRecord(record)
        }
    }

    private fun triggerFeedback(message: String) {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        } catch (e: Exception) {}
        notificationHelper.showNotification("SmartDose", message)
    }

    fun updateSettings(breakfast: String, lunch: String, dinner: String, largeText: Boolean, highContrast: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile(id = 1, name = "User", age = 70)
            repository.updateProfile(current.copy(
                breakfastTime = breakfast,
                lunchTime = lunch,
                dinnerTime = dinner,
                largeTextMode = largeText,
                highContrastMode = highContrast
            ))
        }
    }

    fun getCurrentTarget(): Pair<Int, MealType> {
        return Pair(_currentDay.value, _currentMeal.value)
    }
    
    fun getAdherenceStats(): Map<String, Any> {
        val records = allRecords.value
        if (records.isEmpty()) return mapOf("percentage" to 0, "insight" to "No data yet")
        
        val takenCount = records.count { it.status == Status.TAKEN || it.status == Status.DELAYED }
        val percentage = (takenCount.toFloat() / 21 * 100).toInt().coerceAtMost(100)
        
        val missedMeals = records.filter { it.status == Status.MISSED }
            .groupBy { it.meal }
            .maxByOrNull { it.value.size }?.key
            
        val insight = if (missedMeals != null) "Most missed doses occur at ${missedMeals.name.lowercase()}" 
                      else "Doing great this week!"
                      
        return mapOf("percentage" to percentage, "insight" to insight)
    }
}
