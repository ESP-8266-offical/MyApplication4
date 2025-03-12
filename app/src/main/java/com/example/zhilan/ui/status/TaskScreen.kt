package com.example.zhilan.ui.status

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhilan.model.Task
import com.example.zhilan.model.TaskIconType
import com.example.zhilan.model.TaskType

@Composable
fun TaskScreen() {
    val taskViewModel: TaskViewModel = viewModel(factory = TaskViewModelFactory(LocalContext.current))
    val tasks by taskViewModel.tasks.collectAsState()
    val currentDate by taskViewModel.currentDate.collectAsState()
    val schedules by taskViewModel.schedules.collectAsState()
    
    // 添加任务对话框状态
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskLocation by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedTaskType by remember { mutableStateOf(TaskType.HOMEWORK) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加任务",
                    tint = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            // 顶部日期和问候语
            Text(
                text = currentDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 计划区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "计划",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                var navigateToAllSchedules by remember { mutableStateOf(false) }
                
                TextButton(onClick = { navigateToAllSchedules = true }) {
                    Text("See All")
                }
                
                // 导航到所有计划界面
                if (navigateToAllSchedules) {
                    AllSchedulesScreen(onBackClick = { navigateToAllSchedules = false })
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 计划卡片横向滚动列表
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 显示计划类型的任务
                val scheduleTasks = tasks.filter { it.taskType == TaskType.SCHEDULE }
                items(scheduleTasks) { task ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = task.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "位置",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.location,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // 同时显示原有的计划项
                items(schedules) { schedule ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = schedule.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "参与人数",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${schedule.peopleCount}人",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 作业区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "作业",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                var navigateToNewestHomework by remember { mutableStateOf(false) }
                
                TextButton(onClick = { navigateToNewestHomework = true }) {
                    Text("Newest")
                }
                
                // 导航到最新作业界面
                if (navigateToNewestHomework) {
                    NewestHomeworkScreen(onBackClick = { navigateToNewestHomework = false })
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 作业列表 - 只显示作业类型的任务
            val homeworkTasks = tasks.filter { it.taskType == TaskType.HOMEWORK }
            homeworkTasks.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { /* TODO: 点击作业项 */ },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "位置",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.location,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加任务对话框
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("添加新任务") },
            text = {
                Column {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("任务标题") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskLocation,
                        onValueChange = { taskLocation = it },
                        label = { Text("任务地点") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("任务描述") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("任务类型", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 任务类型选择器
                    Row(Modifier.selectableGroup()) {
                        Row(
                            Modifier
                                .selectable(
                                    selected = selectedTaskType == TaskType.HOMEWORK,
                                    onClick = { selectedTaskType = TaskType.HOMEWORK },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTaskType == TaskType.HOMEWORK,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("作业")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Row(
                            Modifier
                                .selectable(
                                    selected = selectedTaskType == TaskType.SCHEDULE,
                                    onClick = { selectedTaskType = TaskType.SCHEDULE },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTaskType == TaskType.SCHEDULE,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("计划")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 创建新任务并添加
                        val newTask = Task(
                            id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                            title = taskTitle,
                            location = taskLocation,
                            description = taskDescription,
                            iconType = TaskIconType.OTHER,
                            dueDate = System.currentTimeMillis(),
                            taskType = selectedTaskType
                        )
                        taskViewModel.addTask(newTask)
                        
                        // 重置表单并关闭对话框
                        taskTitle = ""
                        taskLocation = ""
                        taskDescription = ""
                        showAddTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank()
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddTaskDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}