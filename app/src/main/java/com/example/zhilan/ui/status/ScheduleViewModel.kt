package com.example.zhilan.ui.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhilan.data.CourseRepository
import com.example.zhilan.model.Course
import com.example.zhilan.model.WeekType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(private val context: Context) : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _currentWeek = MutableStateFlow(1)
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    private val courseRepository: CourseRepository = CourseRepository(context)

    init {
        // 订阅数据库变化
        viewModelScope.launch {
            courseRepository.getAllCourses().collectLatest { dbCourses ->
                _courses.value = dbCourses
            }
        }
        
        // 设置当前周数
        setCurrentWeek(calculateCurrentWeek())
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.addCourse(course)
            // 不需要手动更新_courses，因为已经通过Flow订阅了数据变化
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            courseRepository.updateCourse(course)
            // 不需要手动更新_courses，因为已经通过Flow订阅了数据变化
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            courseRepository.deleteCourse(courseId)
            // 不需要手动更新_courses，因为已经通过Flow订阅了数据变化
        }
    }

    fun setCurrentWeek(week: Int) {
        viewModelScope.launch {
            _currentWeek.value = week.coerceIn(1, 20)
        }
    }
    
    // 计算当前是第几周
    private fun calculateCurrentWeek(): Int {
        // 这里简单返回1，实际应用中应该根据开学日期计算
        return 1
    }
}