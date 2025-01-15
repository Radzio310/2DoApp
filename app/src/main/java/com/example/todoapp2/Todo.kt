package com.example.todoapp2

import java.util.Date

data class Todo(
    var id: Int,
    var title: String,
    var createdAt: Date,
    var deadline: Date? = null,
    var isCompleted: Boolean = false,
    var isProject: Boolean = false, // Pole rozróżniające zadania i projekty
    var description: String? = null, // Opis projektu
    var tasks: MutableList<Todo> = mutableListOf(), // Lista zadań wewnątrz projektu
    var order: Int = 0, // kolejność zadań
    val notifications: MutableList<Pair<Long, String>> = mutableListOf()
)

