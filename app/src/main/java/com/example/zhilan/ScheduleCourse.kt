package com.example.zhilan

import androidx.compose.ui.graphics.Color

/**
 * 用于UI显示的课程数据类
 */
data class ScheduleCourse(
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: Int,
    val startTime: Int,
    val endTime: Int,
    val color: Color,
    val startSection: Int = 0,
    val endSection: Int = 0,
    )