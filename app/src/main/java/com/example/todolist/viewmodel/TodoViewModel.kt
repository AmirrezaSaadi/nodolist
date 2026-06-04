package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.model.Todo
import com.example.todolist.data.repo.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    // ... other states remain the same ...
    private val _isDialogShown = MutableStateFlow(false)
    val isDialogShown: StateFlow<Boolean> = _isDialogShown.asStateFlow()
    private val _dialogTitle = MutableStateFlow("Add To-Do")
    val dialogTitle: StateFlow<String> = _dialogTitle.asStateFlow()
    private val _currentTodo = MutableStateFlow<Todo?>(null)
    val currentTodo: StateFlow<Todo?> = _currentTodo.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _isDatePickerShown = MutableStateFlow(false)
    val isDatePickerShown = _isDatePickerShown.asStateFlow()
    private val _tempDueDate = MutableStateFlow<Long?>(null)
    val tempDueDate = _tempDueDate.asStateFlow()


    private val _allTodos = repository.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todos: StateFlow<List<Todo>> = searchQuery
        .combine(_allTodos) { query, todos ->
            if (query.isBlank()) {
                todos
            } else {
                todos.filter {
                    it.title.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Add this function to handle pinning
    fun onPinToggle(todo: Todo) {
        viewModelScope.launch {
            repository.upsertTodo(todo.copy(isPinned = !todo.isPinned))
        }
    }

    // ... other functions remain the same ...
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    fun onDueDateClicked() {
        _isDatePickerShown.value = true
    }
    fun onDateSelected(date: Long?) {
        _tempDueDate.value = date
        _isDatePickerShown.value = false
    }
    fun onDatePickerDismiss() {
        _isDatePickerShown.value = false
    }
    fun onAddTodoClicked() {
        _dialogTitle.value = "Add To-Do"
        _currentTodo.value = null
        _tempDueDate.value = null
        _isDialogShown.value = true
    }
    fun onEditTodoClicked(todo: Todo) {
        _dialogTitle.value = "Edit To-Do"
        _currentTodo.value = todo
        _tempDueDate.value = todo.dueDate
        _isDialogShown.value = true
    }
    fun onDialogDismiss() {
        _isDialogShown.value = false
        _currentTodo.value = null
        _tempDueDate.value = null
    }
    fun upsertTodo(title: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                val todo = _currentTodo.value?.copy(
                    title = title,
                    dueDate = _tempDueDate.value
                ) ?: Todo(
                    title = title,
                    dueDate = _tempDueDate.value
                )
                repository.upsertTodo(todo)
                onDialogDismiss()
            }
        }
    }
    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }
    fun onTodoCheckedChange(todo: Todo, isChecked: Boolean) {
        viewModelScope.launch {
            repository.upsertTodo(todo.copy(isCompleted = isChecked))
        }
    }
}