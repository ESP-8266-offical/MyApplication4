package com.example.zhilan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    val id: Int = 0,
    val name: String = "",
    val location: String = "",
    val teacher: String = "",
    val dayOfWeek: Int = 1, // 1-7 表示周一到周日
    val startSection: Int = 1, // 开始节次
    val endSection: Int = 2, // 结束节次
    val startWeek: Int = 1, // 开始周
    val endWeek: Int = 16, // 结束周
    val weekType: WeekType = WeekType.ALL, // 周类型：每周、单周、双周
    val color: Int = 0, // 课程卡片颜色
    val alpha: Float = 0.5f // 课程卡片透明度
) : Parcelable