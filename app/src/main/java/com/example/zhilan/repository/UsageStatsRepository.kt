package com.example.zhilan.repository

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.util.Calendar

class UsageStatsRepository(private val context: Context) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * Check if the app has permission to access usage stats
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Open usage access settings screen for the user to grant permission
     */
    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * Get total device usage time in milliseconds for a specific time range
     */
    fun getTotalUsageTime(startTime: Long, endTime: Long): Long {
        if (!hasUsageStatsPermission()) return 0

        var totalTimeInForeground = 0L
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        var lastEventTime = startTime
        var currentApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                currentApp = event.packageName
                lastEventTime = event.timeStamp
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND &&
                event.packageName == currentApp) {
                totalTimeInForeground += event.timeStamp - lastEventTime
                currentApp = null
            }
        }

        // If an app is still in the foreground
        if (currentApp != null) {
            totalTimeInForeground += endTime - lastEventTime
        }

        return totalTimeInForeground
    }

    /**
     * Get today's device usage time in milliseconds
     */
    fun getTodayUsageTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return getTotalUsageTime(startTime, endTime)
    }

    /**
     * Get device usage time for last 7 days (including today)
     * @return Map of day to usage time in milliseconds
     */
    fun getWeeklyUsageTime(): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        val calendar = Calendar.getInstance()
        val dayOfWeek = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

        // Get today's date
        val today = Calendar.getInstance()

        // For each of the last 7 days
        for (i in 6 downTo 0) {
            calendar.timeInMillis = today.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endTime = calendar.timeInMillis

            val day = dayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1]
            result[day] = getTotalUsageTime(startTime, endTime)
        }

        return result
    }
}