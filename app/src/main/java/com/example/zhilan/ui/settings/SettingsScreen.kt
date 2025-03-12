package com.example.zhilan.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.zhilan.model.ScheduleSettings
import com.example.zhilan.widget.ScheduleWidgetConfigureActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: ScheduleSettings,
    onSettingsChange: (ScheduleSettings) -> Unit
) {
    val context = LocalContext.current
    var showScheduleSettings by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 课程表设置
        Text(
            text = "课程表",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // 课程表设置入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScheduleSettings = !showScheduleSettings }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("课程表设置")
                }
                Icon(
                    imageVector = if (showScheduleSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            
            // 课程表设置详细内容
            if (showScheduleSettings) {
                ScheduleSettingsContent(settings, onSettingsChange)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 小组件设置
        Text(
            text = "小组件",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // 小组件设置入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        context.startActivity(Intent(context, ScheduleWidgetConfigureActivity::class.java))
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("小组件设置")
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 关于
        Text(
            text = "关于",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            // 关于入口
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        context.startActivity(Intent(context, AboutActivity::class.java))
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("关于知澜")
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ScheduleSettingsContent(
    settings: ScheduleSettings,
    onSettingsChange: (ScheduleSettings) -> Unit
) {
    var showWeekend by remember { mutableStateOf(settings.showWeekend) }
    var totalWeeks by remember { mutableStateOf(settings.totalWeeks.toString()) }
    var sectionsPerDay by remember { mutableStateOf(settings.sectionsPerDay.toString()) }
    var morningClasses by remember { mutableStateOf(settings.morningClasses.toString()) }
    var afternoonClasses by remember { mutableStateOf(settings.afternoonClasses.toString()) }
    var eveningClasses by remember { mutableStateOf(settings.eveningClasses.toString()) }
    var classDuration by remember { mutableStateOf(settings.classDuration.toString()) }
    var breakDuration by remember { mutableStateOf(settings.breakDuration.toString()) }

    Column(modifier = Modifier.padding(16.dp)) {
        // 显示周末设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("显示周末课程")
            Switch(
                checked = showWeekend,
                onCheckedChange = { 
                    showWeekend = it
                    onSettingsChange(settings.copy(showWeekend = it))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 总周数设置
        OutlinedTextField(
            value = totalWeeks,
            onValueChange = { 
                totalWeeks = it
                it.toIntOrNull()?.let { weeks ->
                    if (weeks in 1..30) {
                        onSettingsChange(settings.copy(totalWeeks = weeks))
                    }
                }
            },
            label = { Text("学期总周数") },
            leadingIcon = {
                Icon(Icons.Default.DateRange, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 每天课程节数设置
        OutlinedTextField(
            value = sectionsPerDay,
            onValueChange = { 
                sectionsPerDay = it
                it.toIntOrNull()?.let { sections ->
                    if (sections in 1..12) {
                        onSettingsChange(settings.copy(sectionsPerDay = sections))
                    }
                }
            },
            label = { Text("每天课程节数") },
            leadingIcon = {
                Icon(Icons.Default.Schedule, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 课程时段设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = morningClasses,
                onValueChange = { 
                    morningClasses = it
                    it.toIntOrNull()?.let { count ->
                        if (count in 0..6) {
                            onSettingsChange(settings.copy(morningClasses = count))
                        }
                    }
                },
                label = { Text("上午课程数") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = afternoonClasses,
                onValueChange = { 
                    afternoonClasses = it
                    it.toIntOrNull()?.let { count ->
                        if (count in 0..6) {
                            onSettingsChange(settings.copy(afternoonClasses = count))
                        }
                    }
                },
                label = { Text("下午课程数") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = eveningClasses,
                onValueChange = { 
                    eveningClasses = it
                    it.toIntOrNull()?.let { count ->
                        if (count in 0..4) {
                            onSettingsChange(settings.copy(eveningClasses = count))
                        }
                    }
                },
                label = { Text("晚上课程数") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 时间设置
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = classDuration,
                onValueChange = { 
                    classDuration = it
                    it.toIntOrNull()?.let { duration ->
                        if (duration in 30..120) {
                            onSettingsChange(settings.copy(classDuration = duration))
                        }
                    }
                },
                label = { Text("课程时长(分钟)") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = breakDuration,
                onValueChange = { 
                    breakDuration = it
                    it.toIntOrNull()?.let { duration ->
                        if (duration in 5..30) {
                            onSettingsChange(settings.copy(breakDuration = duration))
                        }
                    }
                },
                label = { Text("休息时长(分钟)") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}