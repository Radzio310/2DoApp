package com.example.todoapp2

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class TodoViewModel : ViewModel() {
    private var _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>> = _todoList

    init {
        getAllTodo() // Załaduj listę przy starcie aplikacji
    }

    fun getAllTodo() {
        _todoList.value = TodoManager.getAllTodo().reversed() // Pobierz listę z odpowiednią kolejnością
    }

    fun addTodo(context: Context, title: String, deadline: Date?, isProject: Boolean) {
        TodoManager.addTodo(context, title, deadline, isProject)
        getAllTodo() // Odśwież listę
    }

    fun deleteTodo(id: Int) {
        TodoManager.deleteTodo(id)
        getAllTodo()
    }

    fun markAsCompleted(id: Int) {
        TodoManager.markAsCompleted(id)
        getAllTodo()
    }

    fun updateProject(updatedProject: Todo, onProjectCompletionDecision: Boolean) {
        TodoManager.updateProject(
            updatedProject.id,
            updatedProject.description,
            updatedProject.deadline,
            updatedProject.tasks,
            onProjectCompletionDecision
        )
        getAllTodo() // Odśwież listę
    }

    fun addTaskToProject(projectId: Int, task: Todo) {
        TodoManager.addTaskToProject(projectId, task.title, task.deadline)
        getAllTodo()
    }

    fun editProject(projectId: Int, newTitle: String, newDescription: String?, newDeadline: Date?) {
        TodoManager.editProject(projectId, newTitle, newDescription, newDeadline)
        getAllTodo()
    }

    fun deleteTaskFromProject(projectId: Int, taskId: Int) {
        TodoManager.deleteTaskFromProject(projectId, taskId)
        getAllTodo()
    }

    fun saveProjectState(project: Todo) {
        TodoManager.saveProjectState(project)
        getAllTodo() // Odświeżenie listy po zapisaniu
    }

    fun updateOrder(id: Int, newOrder: Int) {
        TodoManager.updateOrder(id, newOrder)
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


    fun updateTodo(context: Context, updatedTodo: Todo) {
        val existingTodo = TodoManager.getAllTodo().find { it.id == updatedTodo.id }
        existingTodo?.let {
            it.title = updatedTodo.title
            it.deadline = updatedTodo.deadline
            TodoManager.saveTodos()

            // Anuluj stare powiadomienia
            TodoManager.cancelTaskReminders(context, it.id)

            // Zaplanuj nowe powiadomienia dla zmienionego deadline'u
            if (it.deadline != null) {
                TodoManager.scheduleTaskNotifications(context, it)
            }

            getAllTodo() // Odśwież listę
        }
    }





}
