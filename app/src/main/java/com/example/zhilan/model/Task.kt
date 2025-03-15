package com.example.zhilan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 任务数据模型
 */
@Parcelize
data class Task(
    val id: Int = 0,
    val title: String = "", // 任务标题
    val location: String = "", // 任务地点
    val iconType: TaskIconType = TaskIconType.MEETING, // 任务图标类型
    val dueDate: Long = 0, // 截止日期时间戳
    val isCompleted: Boolean = false, // 是否已完成
    val description: String = "", // 任务描述
    val taskType: TaskType = TaskType.HOMEWORK // 任务类型：计划或作业
) : Parcelable

/**
 * 任务图标类型
 */
enum class TaskIconType {
    MEETING, // 会议
    HOMEWORK, // 作业
    EXAM, // 考试
    ACTIVITY, // 活动
    OTHER // 其他
}

/**
 * 任务类型
 */
enum class TaskType {
    SCHEDULE, // 计划
    HOMEWORK  // 作业
}