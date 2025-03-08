package com.example.zhilan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zhilan.model.Course
import com.example.zhilan.ui.schedule.ScheduleScreen
import com.example.zhilan.ui.profile.ProfileScreen
import com.example.zhilan.ui.status.StatusScreen
import com.example.zhilan.ui.theme.ZhiLanTheme
import com.example.zhilan.ui.schedule.ScheduleViewModelFactory
import com.example.zhilan.ui.settings.SettingsScreen
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity

enum class NavigationItem(val icon: ImageVector, val label: String, val route: String) {
    Schedule(Icons.Default.DateRange, "课程表", "schedule"),
    Status(Icons.Default.Apps, "状态", "status"),
    Profile(Icons.Default.Person, "我的", "profile"),
    Settings(Icons.Default.Settings, "设置", "settings")
}

class MainActivity : ComponentActivity() {
    private var lastNavigationTime = 0L
    private val scheduleViewModel: ScheduleViewModel by viewModels { ScheduleViewModelFactory(this) }
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val editCourseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val dataChanged = result.data?.getBooleanExtra("RESULT_COURSE_CHANGED", false) ?: false
            if (dataChanged) {
                // Force reload courses from database
                scheduleViewModel.reloadCourses()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZhiLanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                NavigationItem.entries.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            val currentTime = System.currentTimeMillis()
                                            if (currentTime - lastNavigationTime > 300) {
                                                lastNavigationTime = currentTime
                                                navController.navigate(item.route) {
                                                    launchSingleTop = true
                                                    popUpTo("status") { saveState = true }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = "status",
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            composable("schedule") {

                                ScheduleScreen(
                                    courses = scheduleViewModel.courses.collectAsState().value,
                                    currentWeek = scheduleViewModel.currentWeek.collectAsState().value,
                                    onCourseClick = { course ->
                                        editCourseLauncher.launch(ScheduleEditActivity.createIntent(this@MainActivity, course))
                                    },
                                    onWeekChange = { newWeek ->
                                        scheduleViewModel.setCurrentWeek(newWeek)
                                    },
                                    onAddCourse = {
                                        editCourseLauncher.launch(ScheduleEditActivity.createIntent(this@MainActivity))
                                    }
                                )
                            }
                            composable("status") {
                                StatusScreen(
                                    onSportsClick = {
                                        Toast.makeText(this@MainActivity, "体育功能开发中", Toast.LENGTH_SHORT).show()
                                    },
                                    onGradeClick = {
                                        Toast.makeText(this@MainActivity, "成绩功能开发中", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    onScheduleClick = {
                                        navController.navigate("schedule")
                                    },
                                    onLibraryClick = {
                                    },
                                    onBusClick = {
                                    },
                                    onCardClick = {
                                    },
                                    onSportsClick = {
                                        Toast.makeText(this@MainActivity, "体育功能开发中", Toast.LENGTH_SHORT).show()
                                    },
                                    onGradeClick = {
                                        Toast.makeText(this@MainActivity, "成绩功能开发中", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            composable("settings") {
                                val settings by settingsViewModel.settings.collectAsState()

                                SettingsScreen(
                                    settings = settings,
                                    onSettingsChange = { newSettings ->
                                        settingsViewModel.updateSettings(newSettings)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}