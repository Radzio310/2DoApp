package com.example.todoapp2

import java.util.Date

data class Todo(
    var id: Int,
    var title: String,
    var createdAt: Date,
    var deadline: Date? = null,
    var isCompleted: Boolean = false,
    var isProject: Boolean = false // Dodajemy pole do rozróżnienia zadania od projektu
)
