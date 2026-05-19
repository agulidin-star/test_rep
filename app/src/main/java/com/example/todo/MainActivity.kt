package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todo.data.TaskDatabase
import com.example.todo.notification.NotificationHelper
import com.example.todo.repository.TaskRepository
import com.example.todo.ui.EditScreen
import com.example.todo.ui.ListScreen
import com.example.todo.ui.theme.TodoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel once
        NotificationHelper(this).createNotificationChannel()

        // Kick off periodic overdue worker
        TaskRepository(this).schedulePeriodicOverdueWorker()

        setContent {
            TodoTheme {
                NavHost(
                    navController  = rememberNavController().also { navController = it },
                    startDestination = "list",
                    modifier       = Modifier.fillMaxSize()
                ) {
                    composable("list") {
                        Scaffold(modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0)
                        ) { innerPadding ->
                            ListScreen(
                                repository = TaskRepository(this@MainActivity),
                                onOpenTask = navController::navigate,
                                onNewTask  = { navController.navigate("edit/0") }
                            )
                        }
                    }
                    composable("edit/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                        EditScreen(
                            repository    = TaskRepository(this@MainActivity),
                            taskId        = taskId,
                            onNavigateUp  = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    // Initialised in setContent via rememberNavController().
    private lateinit var _navController: androidx.navigation.NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper(this).createNotificationChannel()
        TaskRepository(this).schedulePeriodicOverdueWorker()

        setContent {
            TodoTheme {
                val navController = rememberNavController().also { _navController = it }
                NavHost(
                    navController  = navController,
                    startDestination = "list",
                    modifier       = Modifier.fillMaxSize()
                ) {
                    composable("list") {
                        Scaffold(modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0)
                        ) { innerPadding ->
                            ListScreen(
                                repository = TaskRepository(this@MainActivity),
                                onOpenTask = navController::navigate,
                                onNewTask  = { navController.navigate("edit/0") }
                            )
                        }
                    }
                    composable("edit/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                        EditScreen(
                            repository    = TaskRepository(this@MainActivity),
                            taskId        = taskId,
                            onNavigateUp  = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
