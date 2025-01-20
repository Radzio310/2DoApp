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
    var notifications: MutableList<Long> = mutableListOf(), // Lista powiadomień (czas w milisekundach przed deadline)
    var areNotificationsDisabled: Boolean = false, // Czy powiadomienia są wyłączone
    var label: Label? = null, // Dodane pole na etykietę
    var isVisible: Boolean = true
)

data class Label(
    val name: String,
    val color: Int, // Kolor w formacie ARGB
    var isLabelVisible: Boolean = true
)

