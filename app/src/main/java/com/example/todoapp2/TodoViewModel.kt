package com.example.todoapp2

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
        _todoList.value = TodoManager.getAllTodo().reversed() // Przypisanie nowej listy do LiveData
    }

    fun addTodo(title: String, deadline: Date?, isProject: Boolean) {
        TodoManager.addTodo(title, deadline, isProject)
        getAllTodo()
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
}
