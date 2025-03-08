package com.example.zhilan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhilan.data.CourseRepository
import com.example.zhilan.model.Course
import com.example.zhilan.model.ScheduleSettings
import com.example.zhilan.model.WeekType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel(private val context: Context) : ViewModel() {
    private val courseRepository = CourseRepository(context)
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _currentWeek = MutableStateFlow(1)
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    val settings = ScheduleSettings()

    init {
        viewModelScope.launch {
            val dbCourses = courseRepository.getAllCourses()
            _courses.value = dbCourses
        }
    }

    fun reloadCourses() {
        viewModelScope.launch {
            _courses.value = courseRepository.getAllCourses()
        }
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            // 检查课程时间冲突
            val hasConflict = checkTimeConflict(course)
            if (hasConflict) {
                // 如果有冲突，可以通过回调通知UI层
                return@launch
            }

            val id = courseRepository.addCourse(course)
            if (id > 0) {
                // 重新从数据库加载所有课程
                val dbCourses = courseRepository.getAllCourses()
                _courses.value = dbCourses
            }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            // 检查课程时间冲突，排除当前课程自身
            val hasConflict = checkTimeConflict(course, excludeCourseId = course.id)
            if (hasConflict) {
                // 如果有冲突，可以通过回调通知UI层
                return@launch
            }

            courseRepository.updateCourse(course) // 使用专门的更新方法
            _courses.update { currentList ->
                currentList.map { if (it.id == course.id) course else it }
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId)
            _courses.update { currentList ->
                currentList.filter { it.id != courseId }
            }
        }
    }

    fun setCurrentWeek(week: Int) {
        viewModelScope.launch {
            _currentWeek.value = week.coerceIn(1, 20)
        }
    }

    /**
     * 检查课程时间是否与现有课程冲突
     * @param course 要检查的课程
     * @param excludeCourseId 要排除的课程ID（用于更新课程时排除自身）
     * @return 是否存在冲突
     */
    private fun checkTimeConflict(course: Course, excludeCourseId: Int = -1): Boolean {
        // 获取当前所有课程
        val currentCourses = _courses.value

        // 筛选出与新课程在同一天的课程，并排除自身（如果是更新操作）
        val sameDayCourses = currentCourses.filter {
            it.dayOfWeek == course.dayOfWeek && it.id != excludeCourseId
        }

        // 检查是否有时间重叠的课程
        return sameDayCourses.any { existingCourse ->
            // 检查周次重叠
            val weeksOverlap = hasWeekOverlap(course, existingCourse)

            // 检查节次重叠
            val sectionsOverlap = !(course.endSection < existingCourse.startSection ||
                    course.startSection > existingCourse.endSection)

            // 同时满足周次重叠和节次重叠才算冲突
            weeksOverlap && sectionsOverlap
        }
    }

    /**
     * 检查两个课程的周次是否重叠
     */
    private fun hasWeekOverlap(course1: Course, course2: Course): Boolean {
        // 获取两个课程的实际周次列表
        val weeks1 = getActualWeeks(course1)
        val weeks2 = getActualWeeks(course2)

        // 检查是否有交集
        return weeks1.any { it in weeks2 }
    }

    /**
     * 获取课程的实际周次列表
     */
    private fun getActualWeeks(course: Course): List<Int> {
        val weeks = mutableListOf<Int>()
        for (week in course.startWeek..course.endWeek) {
            when (course.weekType) {
                WeekType.ALL -> weeks.add(week)
                WeekType.ODD -> if (week % 2 == 1) weeks.add(week)
                WeekType.EVEN -> if (week % 2 == 0) weeks.add(week)
            }
        }
        return weeks
    }
}