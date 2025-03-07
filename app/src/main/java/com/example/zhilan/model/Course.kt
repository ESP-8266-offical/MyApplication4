package com.example.zhilan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 表示一个课程，包含课程的所有属性信息
 * 使用 @Parcelize 注解使其可在 Activity 间传递
 */
@Parcelize
data class Course(
    val id: Int = 0,                    // 课程ID，数据库主键
    val name: String = "",              // 课程名称
    val location: String = "",          // 上课地点
    val teacher: String = "",           // 教师姓名
    val dayOfWeek: Int = 1,             // 星期几(1-7表示周一到周日)
    val startSection: Int = 1,          // 开始节次
    val endSection: Int = 2,            // 结束节次
    val startWeek: Int = 1,             // 开始周
    val endWeek: Int = 16,              // 结束周
    val weekType: WeekType = WeekType.ALL, // 周类型：每周、单周、双周
    val color: Int = 0,                 // 课程卡片颜色
    val alpha: Float = 0.5f             // 课程卡片透明度
) : Parcelable