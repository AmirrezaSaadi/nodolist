package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolist.di.AppModuleImpl
import com.example.todolist.ui.screens.AboutScreen
import com.example.todolist.ui.screens.AddEditNoteScreen
import com.example.todolist.ui.screens.MainScreen
import com.example.todolist.ui.theme.TodoListTheme
import com.example.todolist.viewmodel.MainViewModel
import com.example.todolist.viewmodel.NoteViewModel
import com.example.todolist.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appModule = AppModuleImpl(applicationContext)
        val mainViewModel: MainViewModel by viewModels { appModule.viewModelFactory }
        val todoViewModel: TodoViewModel by viewModels { appModule.viewModelFactory }
        val noteViewModel: NoteViewModel by viewModels { appModule.viewModelFactory }

        setContent {
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            TodoListTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main_screen") {
                        composable("main_screen") {
                            MainScreen(
                                navController = navController,
                                mainViewModel = mainViewModel,
                                todoViewModel = todoViewModel,
                                noteViewModel = noteViewModel
                            )
                        }
                        composable(
                            route = "add_edit_note_screen/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                            AddEditNoteScreen(
                                noteId = noteId,
                                navController = navController,
                                viewModel = noteViewModel
                            )
                        }
                        // Add the new route for the About screen
                        composable("about_screen") {
                            AboutScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}