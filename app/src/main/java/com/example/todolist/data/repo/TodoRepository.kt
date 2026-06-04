package com.example.todolist.data.repo

import com.example.todolist.data.db.TodoDao
import com.example.todolist.data.model.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    suspend fun upsertTodo(todo: Todo) {
        todoDao.upsertTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }
}