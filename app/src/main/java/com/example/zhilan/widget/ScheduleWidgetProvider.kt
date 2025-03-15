package com.example.zhilan.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.ComponentName
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import com.example.zhilan.MainActivity
import com.example.zhilan.R
import com.example.zhilan.ScheduleActivity
import com.example.zhilan.data.CourseRepository
import com.example.zhilan.model.Course
import com.example.zhilan.model.ScheduleSettings
import java.text.SimpleDateFormat
import java.util.*

/**
 * 课程表小组件提供者
 * 用于在桌面显示课程信息的小组件
 */
class ScheduleWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val PREFS_NAME = "com.example.zhilan.widget.ScheduleWidgetProvider"
        private const val PREF_UPDATE_INTERVAL = "update_interval"
        private const val PREF_SHOW_TODAY_ONLY = "show_today_only"
        private const val DEFAULT_UPDATE_INTERVAL = 20 // 默认更新间隔（分钟）
        
        // 更新所有小组件的方法
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ScheduleWidgetProvider::class.java)
            )
            
            if (appWidgetIds.isNotEmpty()) {
                // 触发小组件更新
                val intent = Intent(context, ScheduleWidgetProvider::class.java)
                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                context.sendBroadcast(intent)
            }
        }
        
        // 获取小组件设置
        fun getWidgetPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        
        // 获取更新间隔
        fun getUpdateInterval(context: Context): Int {
            return getWidgetPreferences(context).getInt(PREF_UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL)
        }
        
        // 设置更新间隔
        fun setUpdateInterval(context: Context, minutes: Int) {
            getWidgetPreferences(context).edit().putInt(PREF_UPDATE_INTERVAL, minutes).apply()
        }
        
        // 获取是否只显示今天的课程
        fun getShowTodayOnly(context: Context): Boolean {
            return getWidgetPreferences(context).getBoolean(PREF_SHOW_TODAY_ONLY, true)
        }
        
        // 设置是否只显示今天的课程
        fun setShowTodayOnly(context: Context, showTodayOnly: Boolean) {
            getWidgetPreferences(context).edit().putBoolean(PREF_SHOW_TODAY_ONLY, showTodayOnly).apply()
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // 更新每个小组件
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // 当第一个小组件被创建时调用
        // 可以在这里设置闹钟等定期更新机制
    }

    override fun onDisabled(context: Context) {
        // 当最后一个小组件被删除时调用
        // 可以在这里清理资源
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // 处理自定义的广播事件
    }

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            // 创建RemoteViews对象
            val views = RemoteViews(context.packageName, R.layout.schedule_widget)
            
            // 设置点击小组件打开课程表的意图
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("NAVIGATE_TO", "schedule") // 添加导航到课程表的标记
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // 获取当前日期和星期
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 转换为0-6，对应周日到周六
            val chineseDayOfWeek = when(dayOfWeek) {
                0 -> "周日"
                1 -> "周一"
                2 -> "周二"
                3 -> "周三"
                4 -> "周四"
                5 -> "周五"
                6 -> "周六"
                else -> "周一"
            }
            
            // 设置日期和星期信息
            views.setTextViewText(R.id.widget_day_info, "今天 / ${chineseDayOfWeek}")
            
            // 计算当前是第几周
            // 这里需要根据实际情况计算，暂时使用示例数据
            val currentWeek = 4 // 示例：第4周
            views.setTextViewText(R.id.widget_week_info, "第${currentWeek}周")
            
            // 获取课程数据
            val courseRepository = CourseRepository(context)
            val courses = courseRepository.getAllCourses().value ?: emptyList()
            
            // 筛选当天的课程
            val todayCourses = courses.filter { course -> 
                try {
                    // 筛选当天的课程
                    val isDayMatch = course.dayOfWeek == (if(dayOfWeek == 0) 7 else dayOfWeek) // 转换周日为7
                    
                    // 筛选当前周次的课程
                    val isWeekMatch = when (course.weekType) {
                        com.example.zhilan.model.WeekType.ALL -> currentWeek in course.startWeek..course.endWeek
                        com.example.zhilan.model.WeekType.ODD -> currentWeek in course.startWeek..course.endWeek && currentWeek % 2 == 1
                        com.example.zhilan.model.WeekType.EVEN -> currentWeek in course.startWeek..course.endWeek && currentWeek % 2 == 0
                    }
                    
                    isDayMatch && isWeekMatch
                } catch (e: Exception) {
                    false // 如果处理单个课程时出错，跳过该课程
                }
            }
            
            // 如果没有课程，显示空视图
            if (todayCourses.isEmpty()) {
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setViewVisibility(R.id.widget_course_list, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                views.setViewVisibility(R.id.widget_course_list, View.VISIBLE)
                
                // 创建适配器来显示课程列表
                // 由于RemoteViews的限制，我们需要手动创建每个课程项
                val settings = ScheduleSettings()
                
                // 清除之前的视图
                views.removeAllViews(R.id.widget_course_list)
                
                // 添加课程项
                val sortedCourses = todayCourses.sortedBy { course -> course.startSection }
                for (course in sortedCourses) {
                    try {
                        val itemView = RemoteViews(context.packageName, R.layout.schedule_widget_item)
                        
                        // 设置课程指示条颜色
                        itemView.setInt(R.id.course_indicator, "setBackgroundColor", course.color)
                        
                        // 设置课程时间
                        val startTimeStr = settings.getClassTime(course.startSection).split("-").getOrNull(0) ?: ""
                        val endTimeStr = settings.getClassTime(course.endSection).split("-").getOrNull(1) ?: ""
                        itemView.setTextViewText(R.id.course_time_start, startTimeStr)
                        itemView.setTextViewText(R.id.course_time_end, endTimeStr)
                        
                        // 设置课程名称
                        itemView.setTextViewText(R.id.course_name, course.name)
                        
                        // 设置课程详情
                        val detailsText = "第${course.startSection}-${course.endSection}节 | ${course.location} | ${course.teacher}"
                        itemView.setTextViewText(R.id.course_details, detailsText)
                        
                        // 添加到列表
                        views.addView(R.id.widget_course_list, itemView)
                    } catch (e: Exception) {
                        // 如果处理单个课程项时出错，跳过该课程项
                        continue
                    }
                }
            }
            
            // 更新小组件
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            // 创建一个简单的错误视图
            val errorViews = RemoteViews(context.packageName, R.layout.schedule_widget)
            errorViews.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
            errorViews.setViewVisibility(R.id.widget_course_list, View.GONE)
            errorViews.setTextViewText(R.id.widget_empty_view, "加载小组件时出错，请重试")
            
            // 更新小组件显示错误信息
            appWidgetManager.updateAppWidget(appWidgetId, errorViews)
        }
    }
}