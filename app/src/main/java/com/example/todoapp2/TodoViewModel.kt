package com.example.todoapp2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class TodoViewModel : ViewModel() {
    private var _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>> = _todoList

    private var _labels = MutableLiveData<List<Label>>()
    val labels: LiveData<List<Label>> = _labels
    private var _selectedLabels = MutableLiveData<List<Label?>>()

    init {
        getAllTodo() // Załaduj listę przy starcie aplikacji
        loadLabels()
        // Ustaw wszystkie etykiety z zadań jako domyślnie wybrane
        val allLabels = TodoManager.getAllTodo().mapNotNull { it.label }.distinct()
        _selectedLabels.value = listOf(null) + (_labels.value.orEmpty() + allLabels).distinct()
    }


    private fun getAllTodo() {
        _todoList.value = TodoManager.getAllTodo().reversed() // Pobierz listę z odpowiednią kolejnością
    }

    fun addLabel(name: String, color: Int) {
        if (TodoManager.addLabel(name, color)) {
            loadLabels()
        }
    }


    fun removeLabel(label: Label) {
        TodoManager.removeLabel(label)
        loadLabels() // Odśwież listę etykiet
        getAllTodo() // Odśwież listę zadań
    }


    private fun loadLabels() {
        _labels.value = TodoManager.getLabels()
    }

    fun addTodo(context: Context, title: String, deadline: Date?, isProject: Boolean) {
        TodoManager.addTodo(context, title, deadline, isProject)
        getAllTodo() // Odśwież listę
    }

    fun deleteTodo(context: Context, id: Int) {
        TodoManager.deleteTodo(context, id)
        getAllTodo()
    }

    fun markAsCompleted(context: Context, id: Int) {
        TodoManager.markAsCompleted(context, id)
        getAllTodo()
    }

    fun updateProject(context: Context, updatedProject: Todo, onProjectCompletionDecision: Boolean) {
        TodoManager.updateProject(
            context,
            updatedProject.id,
            updatedProject.description,
            updatedProject.deadline,
            updatedProject.tasks,
            onProjectCompletionDecision
        )
        getAllTodo()
    }


    fun moveItemUp(id: Int) {
        if (TodoManager.canMoveUp(id)) {
            val index = TodoManager.getAllTodo().indexOfFirst { it.id == id }
            val belowId = TodoManager.getAllTodo()[index + 1].id // W górę = niższy indeks w odwróconej liście
            TodoManager.swapOrder(id, belowId)
            getAllTodo()
        }
    }

    fun moveItemDown(id: Int) {
        if (TodoManager.canMoveDown(id)) {
            val index = TodoManager.getAllTodo().indexOfFirst { it.id == id }
            val aboveId = TodoManager.getAllTodo()[index - 1].id // W dół = wyższy indeks w odwróconej liście
            TodoManager.swapOrder(id, aboveId)
            getAllTodo()
        }
    }


    fun updateTodo(context: Context, updatedTodo: Todo, oldDeadline: Date?) {
        val existingTodo = TodoManager.getAllTodo().find { it.id == updatedTodo.id }
        existingTodo?.let {
            it.title = updatedTodo.title
            it.deadline = updatedTodo.deadline
            it.areNotificationsDisabled = updatedTodo.areNotificationsDisabled
            it.notifications = updatedTodo.notifications.toMutableList()
            TodoManager.saveTodos()

            if(it.deadline == null || it.areNotificationsDisabled) {
                // Anuluj stare powiadomienia
                TodoManager.cancelTaskReminders(context, it.id)
            }

            // Zaplanuj nowe powiadomienia tylko jeśli deadline się zmienił
            if (oldDeadline != updatedTodo.deadline && updatedTodo.deadline != null && !it.areNotificationsDisabled) {
                TodoManager.scheduleTaskNotifications(context, it)
            }

            getAllTodo() // Odśwież listę
        }
    }

}