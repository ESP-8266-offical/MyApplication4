// Kotlin
package com.example.zhilan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleView() {
    var currentWeek by rememberSaveable { mutableStateOf(2) }

    val weekInfo = remember { generateWeekInfo() }
//    val allCourses = remember { generateSampleCourses() }

    /*val filteredCourses = remember(currentWeek, allCourses) {
        derivedStateOf {
            allCourses.filter { it.dayOfWeek in 1..7 }
        }
    }.value*/

    Column(modifier = Modifier.fillMaxSize()) {
        WeekNavigationBar(
            currentWeek = currentWeek,
            weekInfo = weekInfo[currentWeek - 1],
            onPreviousWeek = { if (currentWeek > 1) currentWeek-- },
            onNextWeek = { if (currentWeek < weekInfo.size) currentWeek++ }
        )

        DayHeaderRow()
//        ScheduleGrid(courses = filteredCourses)
    }
}

@Composable
fun WeekNavigationBar(
    currentWeek: Int,
    weekInfo: WeekInfo,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous week", modifier = Modifier.size(20.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Week " + weekInfo.weekNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${weekInfo.startDate} - ${weekInfo.endDate}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        IconButton(onClick = onNextWeek, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next week", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun DayHeaderRow() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dates = listOf("2/24", "2/25", "2/26", "2/27", "2/28", "3/01", "3/02")
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(40.dp)
                .background(Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("")
        }
        days.forEachIndexed { index, day ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(if (index == 6) Color.Blue.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (index == 6) Color.Blue else Color.Black
                )
                Text(
                    text = dates[index],
                    fontSize = 10.sp,
                    color = if (index == 6) Color.Blue else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ScheduleGrid(courses: List<ScheduleCourse>) {
    val timeSlots = remember {
        listOf(
            "1\n08:00\n08:45", "2\n08:55\n09:40", "3\n10:00\n10:45", "4\n10:55\n11:40", "5\n11:45\n12:30",
            "6\n14:05\n14:50", "7\n15:00\n15:45", "8\n15:55\n16:40", "9\n17:00\n17:45", "10\n17:55\n18:40", "11\n19:10\n19:55"
        )
    }
    LazyColumn(modifier = Modifier) {
        items(timeSlots) { timeSlot ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(70.dp)
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        .border(0.5.dp, Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeSlot,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp
                    )
                }
                for (day in 1..7) {
                    val timeIndex = timeSlot.substringBefore("\n").toInt()
                    val coursesForThisCell = courses.filter {
                        it.dayOfWeek == day &&
                                timeIndex >= it.startSection &&
                                timeIndex <= it.endSection
                    }
                    if (coursesForThisCell.isNotEmpty()) {
                        val course = coursesForThisCell.first()
                        if (timeIndex == course.startSection) {
                            val rowSpan = course.endSection - course.startSection + 1
                            CourseCell(
                                course = course,
                                modifier = Modifier.height(70.dp * rowSpan)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(70.dp)
                                    .background(Color.Transparent)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(70.dp)
                                .background(if (day == 7) Color.Blue.copy(alpha = 0.05f) else Color.White)
                                .border(0.5.dp, Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCell(course: ScheduleCourse, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(course.color) // Ensure the color is correctly passed
            .border(0.5.dp, course.color, RoundedCornerShape(3.dp))
            .padding(2.dp)
    ) {
        Column {
            Text(
                text = course.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = course.teacher,
                fontSize = 9.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = course.location,
                fontSize = 9.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
        }
    }
}

data class WeekInfo(
    val weekNumber: Int,
    val startDate: String,
    val endDate: String
)

private fun generateWeekInfo(): List<WeekInfo> = listOf(
    WeekInfo(1, "2/17", "2/23"),
    WeekInfo(2, "2/24", "3/02"),
    WeekInfo(3, "3/03", "3/09"),
    WeekInfo(4, "3/10", "3/16"),
    WeekInfo(5, "3/17", "3/23"),
    WeekInfo(6, "3/24", "3/30"),
    WeekInfo(7, "3/31", "4/06"),
    WeekInfo(8, "4/07", "4/13"),
    WeekInfo(9, "4/14", "4/20"),
    WeekInfo(10, "4/21", "4/27"),
    WeekInfo(11, "4/28", "5/04"),
    WeekInfo(12, "5/05", "5/11"),
    WeekInfo(13, "5/12", "5/18"),
    WeekInfo(14, "5/19", "5/25"),
    WeekInfo(15, "5/26", "6/01"),
    WeekInfo(16, "6/02", "6/08")
)

/*
private fun generateSampleCourses(): List<ScheduleCourse> = listOf(
    ScheduleCourse("Calculus", "@Building203", "L\\u0129 Xia", 1, 1, 16, WeekType.ALL, 1, 2, ScheduleCourse.fromColor(Color(0xFFE1F5FE))),
    ScheduleCourse("Physical Education", "", "Zh\\u0101ng Sh\\u00E9ngt\\u00F3ng", 2, 1, 16, WeekType.ALL, 1, 2, ScheduleCourse.fromColor(Color(0xFFF3E5F5))),
    ScheduleCourse("OOP", "@Building117", "H\\u00E9 H\\u00F3nghu\\u00ED", 5, 1, 16, WeekType.ALL, 1, 2, ScheduleCourse.fromColor(Color(0xFFFBE9E7))),
    ScheduleCourse("Web Programming", "@Building302", "W\\u0101ng H\\u00E9ngh\\u00E0i", 1, 1, 16, WeekType.ALL, 3, 4, ScheduleCourse.fromColor(Color(0xFFFFF3E0))),
    ScheduleCourse("Web Programming", "", "W\\u0101ng H\\u00E9ngh\\u00E0i", 3, 1, 16, WeekType.ALL, 3, 4, ScheduleCourse.fromColor(Color(0xFFFFF3E0))),
    ScheduleCourse("OOP", "@Building117", "H\\u00E9 H\\u00F3nghu\\u00ED", 4, 1, 16, WeekType.ALL, 3, 4, ScheduleCourse.fromColor(Color(0xFFFBE9E7))),
    ScheduleCourse("Listening\\u0020\\u0026\\u0020Speaking", "@Building110", "Y\\u00FA Ji\\u00E9", 5, 1, 16, WeekType.ALL, 3, 4, ScheduleCourse.fromColor(Color(0xFFE0F7FA))),
    ScheduleCourse("Physics", "@Building301", "S\\u00FA Zuopi\\u00E0ng", 2, 1, 16, WeekType.ALL, 6, 7, ScheduleCourse.fromColor(Color(0xFFF1F8E9))),
    ScheduleCourse("Reading\\u0020\\u0026\\u0020Writing", "@Building305", "X\\u00EDng N\\u00E1", 3, 1, 16, WeekType.ALL, 6, 7, ScheduleCourse.fromColor(Color(0xFFE0F7FA))),
    ScheduleCourse("Moral\\u0020Education", "@Building209", "Ch\\u00E9n Ch\\u0101ng", 5, 1, 16, WeekType.ALL, 6, 7, ScheduleCourse.fromColor(Color(0xFFE8F5E9))),
    ScheduleCourse("Physics Lab", "", "Sh\\u00ED Yanni", 6, 1, 16, WeekType.ALL, 6, 10, ScheduleCourse.fromColor(Color(0xFFF3E5F5))),
    ScheduleCourse("Cultural Geography", "@Building102", "", 1, 1, 16, WeekType.ALL, 11, 11, ScheduleCourse.fromColor(Color(0xFFE0F7FA))),
    ScheduleCourse("Calculus", "@Building102", "L\\u0129 Xia", 2, 1, 16, WeekType.ALL, 11, 11, ScheduleCourse.fromColor(Color(0xFFE1F5FE))),
    ScheduleCourse("Advanced Math", "", " ", 3, 1, 16, WeekType.ALL, 11, 11, ScheduleCourse.fromColor(Color(0xFFE8F5E9))),
    ScheduleCourse("Advanced Math", "", " ", 4, 1, 16, WeekType.ALL, 11, 11, ScheduleCourse.fromColor(Color(0xFFE8F5E9))),
    ScheduleCourse("Advanced Math", "", " ", 5, 1, 16, WeekType.ALL, 11, 11, ScheduleCourse.fromColor(Color(0xFFE8F5E9)))
)*/
