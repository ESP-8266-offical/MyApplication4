package com.example.zhilan.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.zhilan.model.Course
import com.example.zhilan.model.WeekType
import com.example.zhilan.ui.schedule.ColorUtils.getGradientColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    courses: List<Course>,
    currentWeek: Int,
    onCourseClick: (Course) -> Unit,
    onWeekChange: (Int) -> Unit,
    onAddCourse: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "课程表",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            // 周数选择器
            WeekSelector(currentWeek = currentWeek, onWeekChange = onWeekChange)
            
            // 课程表
            Row(modifier = Modifier.weight(1f)) {
                // 时间列
                TimeColumn()
                
                // 课程网格
                CourseGrid(
                    courses = courses,
                    currentWeek = currentWeek,
                    onCourseClick = onCourseClick
                )
            }
        }
        
        // 添加浮动操作按钮
        FloatingActionButton(
            onClick = onAddCourse,
            modifier = Modifier
                .padding(16.dp)
                .size(56.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加课程")
        }
    }
}

@Composable
fun WeekSelector(
    currentWeek: Int,
    onWeekChange: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(20) { week ->
            val isSelected = week + 1 == currentWeek
            FilterChip(
                selected = isSelected,
                onClick = { onWeekChange(week + 1) },
                label = { Text("第${week + 1}周") }
            )
        }
    }
}

@Composable
fun TimeColumn() {
    Column(
        modifier = Modifier
            .width(40.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 空白格子，对应星期行
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface)
        )
        
        // 课节数
        repeat(12) { section ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (section + 1).toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CourseGrid(
    courses: List<Course>,
    currentWeek: Int,
    onCourseClick: (Course) -> Unit
) {
    Row {
        // 星期几
        (1..7).forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 星期标题
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (day) {
                            1 -> "周一"
                            2 -> "周二"
                            3 -> "周三"
                            4 -> "周四"
                            5 -> "周五"
                            6 -> "周六"
                            else -> "周日"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // 课程格子
                Box(modifier = Modifier.weight(1f)) {
                    courses.filter { course ->
                        course.dayOfWeek == day &&
                        course.startWeek <= currentWeek &&
                        course.endWeek >= currentWeek &&
                        when (course.weekType) {
                            WeekType.ALL -> true
                            WeekType.ODD -> currentWeek % 2 == 1
                            WeekType.EVEN -> currentWeek % 2 == 0
                        }
                    }.also { filteredCourses ->
                        // 调试信息
                        println("Day $day, Week $currentWeek: Found ${filteredCourses.size} courses")
                        filteredCourses.forEach { course ->
                            println("Course: ${course.name}, Day: ${course.dayOfWeek}, Week: ${course.startWeek}-${course.endWeek}, Type: ${course.weekType}")
                        }
                    }.forEach { course ->
                        CourseCard(
                            course = course,
                            modifier = Modifier
                                .padding(1.dp)
                                .fillMaxWidth()
                                .height(((course.endSection - course.startSection + 1) * 40).dp)
                                .absoluteOffset(y = ((course.startSection - 1) * 40).dp)
                                .clickable { onCourseClick(course) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = getGradientColors(course.color)[0].copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                color = Color.White
            )
            if (course.location.isNotEmpty()) {
                Text(
                    text = course.location,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            if (course.teacher.isNotEmpty()) {
                Text(
                    text = course.teacher,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}