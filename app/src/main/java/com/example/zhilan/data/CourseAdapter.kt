package com.example.zhilan.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.zhilan.ScheduleCourse
import com.example.zhilan.model.Course
import com.example.zhilan.model.WeekType

/**
 * 课程适配器类，用于将数据库中的Course对象转换为UI显示所需的ScheduleCourse对象
 */
class CourseAdapter(private val context: Context) {
    private val repository = CourseRepository(context)
    
    /**
     * 获取指定周的所有课程
     * @param week 周数
     * @return 当前周的课程列表
     */
    fun getCoursesForWeek(week: Int): List<ScheduleCourse> {
        val allCourses = repository.getAllCourses()
        return allCourses
            .filter { course ->
                // 根据周数和周类型过滤课程
                when {
                    week < course.startWeek || week > course.endWeek -> false
                    course.weekType == WeekType.ALL -> true
                    course.weekType == WeekType.ODD && week % 2 == 1 -> true
                    course.weekType == WeekType.EVEN && week % 2 == 0 -> true
                    else -> false
                }
            }
            .map { course ->
                // 将Course转换为ScheduleCourse
                ScheduleCourse(
                    name = course.name,
                    teacher = course.teacher,
                    location = course.location,
                    dayOfWeek = course.dayOfWeek,
                    startTime = course.startSection,
                    endTime = course.endSection,
                    color = Color(course.color)
                )
            }
    }
    
    /**
     * 添加新课程
     * @param course 课程对象
     * @return 添加是否成功
     */
    fun addCourse(course: Course): Boolean {
        return repository.addCourse(course) > 0
    }
    
    /**
     * 根据课程颜色代码获取对应的Color对象
     * @param colorCode 颜色代码
     * @return Color对象
     */
    private fun getCourseColor(colorCode: Int): Color {
        // 默认颜色列表
        val colors = listOf(
            Color(0xFFE1F5FE), // 浅蓝
            Color(0xFFE8F5E9), // 浅绿
            Color(0xFFFFF3E0), // 浅橙
            Color(0xFFF3E5F5), // 浅紫
            Color(0xFFE0F7FA), // 青色
            Color(0xFFFBE9E7), // 浅红
            Color(0xFFF1F8E9)  // 浅黄绿
        )
        
        // 如果颜色代码有效，则使用该颜色，否则随机选择一个颜色
        return if (colorCode >= 0 && colorCode < colors.size) {
            colors[colorCode]
        } else {
            colors.random()
        }
    }
}