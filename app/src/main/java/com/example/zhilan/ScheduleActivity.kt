package com.example.zhilan

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zhilan.ui.schedule.ScheduleScreen
import com.example.zhilan.ui.schedule.ScheduleViewModelFactory
import com.example.zhilan.ui.theme.ZhiLanTheme

class ScheduleActivity : ComponentActivity() {
    private lateinit var viewModel: ScheduleViewModel
    
    private val editCourseResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 课程已添加、更新或删除，刷新课程表
            viewModel.reloadCourses()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZhiLanTheme {
                viewModel = viewModel(factory = ScheduleViewModelFactory(this@ScheduleActivity))
                
                ScheduleScreen(
                    courses = viewModel.courses.collectAsState().value,
                    currentWeek = viewModel.currentWeek.collectAsState().value,
                    onCourseClick = { course ->
                        editCourseResultLauncher.launch(ScheduleEditActivity.createIntent(this@ScheduleActivity, course))
                    },
                    onWeekChange = { newWeek ->
                        viewModel.setCurrentWeek(newWeek)
                    },
                    onAddCourse = {
                        editCourseResultLauncher.launch(ScheduleEditActivity.createIntent(this@ScheduleActivity))
                    }
                )
            }
        }
    }
}