package com.example.todoapp2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.time.Instant
import java.util.Date

object TodoManager {
    private lateinit var preferences: SharedPreferences
    private val gson = Gson()
    private val todoList = mutableListOf<Todo>()

    fun init(context: Context) {
        preferences = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        loadTodos()
    }

    fun getAllTodo(): List<Todo> {
        return todoList.sortedByDescending { it.isCompleted }
    }

    fun addTodo(title: String, deadline: Date? = null, isProject: Boolean = false) {
        todoList.add(Todo(System.currentTimeMillis().toInt(), title, Date.from(Instant.now()), deadline, isProject = isProject))
        saveTodos()
    }

    fun deleteTodo(id: Int) {
        todoList.removeIf { it.id == id }
        saveTodos()
    }

    fun markAsCompleted(id: Int) {
        val todo = todoList.find { it.id == id }
        todo?.let {
            // Przełączanie stanu 'isCompleted'
            it.isCompleted = !it.isCompleted
            saveTodos()
        }
    }

    private fun saveTodos() {
        val json = gson.toJson(todoList)
        preferences.edit().putString("todos", json).apply()
    }

    private fun loadTodos() {
        val json = preferences.getString("todos", null)
        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<Todo>>() {}.type
            val savedList: MutableList<Todo> = gson.fromJson(json, type)
            todoList.clear()
            todoList.addAll(savedList)
        }
    }
}
