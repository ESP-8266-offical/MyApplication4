package com.example.zhilan

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.zhilan.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.text.set

private data class AppUsageEvent(
    val packageName: String,
    val timestamp: Long
)

class UsageStatsViewModel(private val context: Context) : ViewModel() {
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    private val _todayUsageTime = MutableStateFlow(0L)
    val todayUsageTime = _todayUsageTime.asStateFlow()

    // Check for permission and update stats
    fun checkPermission() {
        val granted = hasUsageStatsPermission(context)
        _hasPermission.value = granted
        if (granted) {
            updateUsageStats()
        }
    }

    // Request usage stats permission
    fun requestPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // Update usage statistics - call this regularly
    fun updateUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _todayUsageTime.value = getTodayUsageTime()
        }
    }

    // Format milliseconds to readable time
    fun formatUsageTime(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = millis % (1000 * 60 * 60) / (1000 * 60)
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getSecUsageTime(millis: Long): Long {
        val hours = millis / (1000 * 60 * 60)
        val minutes = millis % (1000 * 60 * 60) / (1000 * 60)
        val seconds = hours * 3600 + minutes * 60
        return seconds
    }

    @SuppressLint("ServiceCast")
    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Calculate today's usage time correctly
    @SuppressLint("ServiceCast")
    private fun getTodayUsageTime(): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Define "today" as midnight to now
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // Use events to precisely calculate foreground time
        val events = usageStatsManager.queryEvents(startTime, endTime)

        var totalUsageTime = 0L
        val eventMap = mutableMapOf<String, AppUsageEvent?>()

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                eventMap[event.packageName] = AppUsageEvent(event.packageName, event.timeStamp)
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                val foregroundEvent = eventMap[event.packageName]
                if (foregroundEvent != null) {
                    totalUsageTime += event.timeStamp - foregroundEvent.timestamp
                    eventMap.remove(event.packageName)
                }
            }
        }

        // Add time for apps still in foreground
        val currentTime = System.currentTimeMillis()
        for (foregroundEvent in eventMap.values) {
            foregroundEvent?.let {
                totalUsageTime += currentTime - it.timestamp
            }
        }

        return totalUsageTime
    }
}

class UsageStatsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsageStatsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}