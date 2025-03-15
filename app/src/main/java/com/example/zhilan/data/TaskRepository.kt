package com.example.zhilan.data

import android.content.Context
import com.example.zhilan.model.Task
import com.example.zhilan.model.TaskIconType
import com.example.zhilan.model.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 任务数据仓库，负责管理任务数据
 */
class TaskRepository(private val context: Context) {
    // 使用MutableStateFlow存储任务列表
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    
    // 对外暴露不可变的Flow
    val tasks: Flow<List<Task>> = _tasks.asStateFlow()
    
    init {
        // 初始化一些示例任务数据
        val sampleTasks = listOf(
            Task(
                id = 1,
                title = "U校园口语测试",
                location = "at Julo meeting rooms",
                iconType = TaskIconType.EXAM,
                dueDate = System.currentTimeMillis() + 86400000, // 明天
                isCompleted = false,
                taskType = TaskType.HOMEWORK
            ),
            Task(
                id = 2,
                title = "高等数学课后习题",
                location = "at Google Lino",
                iconType = TaskIconType.HOMEWORK,
                dueDate = System.currentTimeMillis() + 172800000, // 后天
                isCompleted = false,
                taskType = TaskType.HOMEWORK
            ),
            Task(
                id = 3,
                title = "校园跑打卡",
                location = "at Info lab Peru",
                iconType = TaskIconType.ACTIVITY,
                dueDate = System.currentTimeMillis() + 259200000, // 三天后
                isCompleted = false,
                taskType = TaskType.SCHEDULE
            )
        )
        _tasks.value = sampleTasks
    }
    
    /**
     * 添加新任务
     */
    suspend fun addTask(task: Task) {
        val newId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
        val newTask = task.copy(id = newId)
        _tasks.value = _tasks.value + newTask
    }
    
    /**
     * 更新任务
     */
    suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { 
            if (it.id == task.id) task else it 
        }
    }
    
    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: Int) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }
    
    /**
     * 标记任务为已完成
     */
    suspend fun completeTask(taskId: Int) {
        _tasks.value = _tasks.value.map { 
            if (it.id == taskId) it.copy(isCompleted = true) else it 
        }
    }
    
    /**
     * 获取单个任务
     */
    fun getTask(taskId: Int): Task? {
        return _tasks.value.find { it.id == taskId }
    }
}