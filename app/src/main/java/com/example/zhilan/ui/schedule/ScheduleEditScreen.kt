package com.example.zhilan.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.zhilan.model.Course
import com.example.zhilan.model.WeekType
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.zhilan.ui.schedule.ColorUtils.getGradientColors
import androidx.compose.ui.tooling.preview.Preview
import com.example.zhilan.ui.theme.ZhiLanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    course: Course?,
    onSave: (Course) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((Int) -> Unit)? = null
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var teacher by remember { mutableStateOf(course?.teacher ?: "") }
    var location by remember { mutableStateOf(course?.location ?: "") }
    var dayOfWeek by remember { mutableStateOf(course?.dayOfWeek ?: 1) }
    var startSection by remember { mutableStateOf(course?.startSection ?: 1) }
    var endSection by remember { mutableStateOf(course?.endSection ?: 2) }
    var startWeek by remember { mutableStateOf(course?.startWeek ?: 1) }
    var endWeek by remember { mutableStateOf(course?.endWeek ?: 16) }
    var weekType by remember { mutableStateOf(course?.weekType ?: WeekType.ALL) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
            Text(
                text = if (course == null) "添加课程" else "编辑课程",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("课程名称") },
                modifier = Modifier.fillMaxWidth()
            )
    
            Spacer(modifier = Modifier.height(8.dp))
    
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("教师") },
                modifier = Modifier.fillMaxWidth()
            )
    
            Spacer(modifier = Modifier.height(8.dp))
    
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("上课地点") },
                modifier = Modifier.fillMaxWidth()
            )
    
            Spacer(modifier = Modifier.height(16.dp))
    
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 星期选择
                var expandedDayOfWeek by remember { mutableStateOf(false) }
                OutlinedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDayOfWeek,
                        onExpandedChange = { expandedDayOfWeek = it }
                    ) {
                        OutlinedTextField(
                            value = "星期${dayOfWeek}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDayOfWeek) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDayOfWeek,
                            onDismissRequest = { expandedDayOfWeek = false }
                        ) {
                            (1..7).forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("星期${day}") },
                                    onClick = {
                                        dayOfWeek = day
                                        expandedDayOfWeek = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 节次选择
                var expandedStartSection by remember { mutableStateOf(false) }
                OutlinedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedStartSection,
                        onExpandedChange = { expandedStartSection = it }
                    ) {
                        OutlinedTextField(
                            value = "第${startSection}节",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStartSection) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStartSection,
                            onDismissRequest = { expandedStartSection = false }
                        ) {
                            (1..12).forEach { section ->
                                DropdownMenuItem(
                                    text = { Text("第${section}节") },
                                    onClick = {
                                        startSection = section
                                        if (endSection < section) endSection = section
                                        expandedStartSection = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                var expandedEndSection by remember { mutableStateOf(false) }
                OutlinedCard(
                    modifier = Modifier.weight(1f)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedEndSection,
                        onExpandedChange = { expandedEndSection = it }
                    ) {
                        OutlinedTextField(
                            value = "第${endSection}节",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEndSection) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEndSection,
                            onDismissRequest = { expandedEndSection = false }
                        ) {
                            (startSection..12).forEach { section ->
                                DropdownMenuItem(
                                    text = { Text("第${section}节") },
                                    onClick = {
                                        endSection = section
                                        expandedEndSection = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
    
            Spacer(modifier = Modifier.height(16.dp))
    
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 周数选择
                OutlinedTextField(
                    value = startWeek.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { start ->
                            if (start in 1..20) startWeek = start
                        }
                    },
                    label = { Text("开始周") },
                    modifier = Modifier.weight(1f)
                )
    
                Spacer(modifier = Modifier.width(8.dp))
    
                OutlinedTextField(
                    value = endWeek.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { end ->
                            if (end in startWeek..20) endWeek = end
                        }
                    },
                    label = { Text("结束周") },
                    modifier = Modifier.weight(1f)
                )
            }
    
            Spacer(modifier = Modifier.height(16.dp))
                // 颜色选择
                var selectedColorIndex by remember { mutableStateOf(course?.color ?: 0) }
                var colorAlpha by remember { mutableStateOf(0.5f) }

                Text(
                    text = "课程颜色",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(6) { index ->
                        val gradientPair = getGradientColors(index)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = gradientPair.map { it.copy(alpha = 0.5f) }
                                    )
                                )
                                .clickable { selectedColorIndex = index }
                                .border(
                                    width = 2.dp,
                                    color = if (selectedColorIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "透明度: ${(colorAlpha * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = colorAlpha,
                    onValueChange = { colorAlpha = it },
                    valueRange = 0.2f..0.8f
                )
            // 单双周选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeekType.values().forEach { type ->
                    FilterChip(
                        selected = weekType == type,
                        onClick = { weekType = type },
                        label = {
                            Text(
                                when (type) {
                                    WeekType.ALL -> "每周"
                                    WeekType.ODD -> "单周"
                                    WeekType.EVEN -> "双周"
                                }
                            )
                        }
                    )
                }
            }
    
            Spacer(modifier = Modifier.weight(1f))
    
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (course != null && onDelete != null) {
                    Button(
                        onClick = { onDelete(course.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除")
                    }
                }
                
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = {
                        onSave(
                            Course(
                                id = course?.id ?: 0,
                                name = name,
                                teacher = teacher,
                                location = location,
                                dayOfWeek = dayOfWeek,
                                startSection = startSection,
                                endSection = endSection,
                                startWeek = startWeek,
                                endWeek = endWeek,
                                weekType = weekType,
                                color = selectedColorIndex
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
        }
    }
    

    }
    

    
    }

@Preview(showBackground = true)
@Composable
fun ScheduleEditScreenEditPreview() {
    ZhiLanTheme {
        ScheduleEditScreen(
            course = Course(
                id = 1,
                name = "数据结构",
                teacher = "张教授",
                location = "主教学楼 301",
                dayOfWeek = 2,
                startSection = 1,
                endSection = 2,
                startWeek = 1,
                endWeek = 16,
                weekType = WeekType.ALL,
                color = 0
            ),
            onSave = {},
            onCancel = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleEditScreenAddPreview() {
    ZhiLanTheme {
        ScheduleEditScreen(
            course = null,
            onSave = {},
            onCancel = {},
            onDelete = null
        )
    }
}