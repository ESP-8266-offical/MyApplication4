package com.example.zhilan.ui.status

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zhilan.data.TaskRepository
import com.example.zhilan.model.Task
import com.example.zhilan.model.TaskIconType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 任务视图模型，负责管理任务数据和UI状态
 */
class TaskViewModel(private val context: Context) : ViewModel() {
    private val taskRepository = TaskRepository(context)
    
    // 任务列表状态
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()
    
    // 当前日期状态
    private val _currentDate = MutableStateFlow(getCurrentDateString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()
    
    // 计划列表状态
    private val _schedules = MutableStateFlow<List<ScheduleItem>>(createSampleSchedules())
    val schedules: StateFlow<List<ScheduleItem>> = _schedules.asStateFlow()
    
    init {
        viewModelScope.launch {
            // 订阅任务数据变化
            taskRepository.tasks.collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
    }
    
    /**
     * 添加新任务
     */
    fun addTask(task: Task) {
        viewModelScope.launch {
            taskRepository.addTask(task)
        }
    }
    
    /**
     * 更新任务
     */
    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }
    
    /**
     * 删除任务
     */
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
    
    /**
     * 标记任务为已完成
     */
    fun completeTask(taskId: Int) {
        viewModelScope.launch {
            taskRepository.completeTask(taskId)
        }
    }
    
    /**
     * 获取当前日期字符串
     */
    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("今天是MM月dd日 EEEE", Locale.CHINESE)
        return dateFormat.format(Date())
    }
    
    /**
     * 创建示例计划数据
     */
    private fun createSampleSchedules(): List<ScheduleItem> {
        return listOf(
            ScheduleItem("网球训练", 16),
            ScheduleItem("游泳课", 16),
            ScheduleItem("高等数学", 16)
        )
    }
}

/**
 * 计划项数据类
 */
data class ScheduleItem(
    val title: String,
    val peopleCount: Int
)