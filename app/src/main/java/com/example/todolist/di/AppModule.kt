package com.example.todolist.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.data.db.AppDatabase
import com.example.todolist.data.repo.NoteRepository
import com.example.todolist.data.repo.SettingsRepository
import com.example.todolist.data.repo.TodoRepository
import com.example.todolist.viewmodel.MainViewModel
import com.example.todolist.viewmodel.NoteViewModel
import com.example.todolist.viewmodel.TodoViewModel

interface AppModule {
    val todoRepository: TodoRepository
    val noteRepository: NoteRepository
    val settingsRepository: SettingsRepository // Add settings repository
    val viewModelFactory: ViewModelProvider.Factory
}

class AppModuleImpl(private val appContext: Context) : AppModule {

    private val database by lazy { AppDatabase.getDatabase(appContext) }

    override val todoRepository: TodoRepository by lazy {
        TodoRepository(database.todoDao())
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    // Provide SettingsRepository instance
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(database.settingsDao())
    }

    override val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(TodoViewModel::class.java) -> {
                        TodoViewModel(todoRepository) as T
                    }
                    modelClass.isAssignableFrom(NoteViewModel::class.java) -> {
                        NoteViewModel(noteRepository) as T
                    }
                    // Inject SettingsRepository into MainViewModel
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                        MainViewModel(settingsRepository) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}