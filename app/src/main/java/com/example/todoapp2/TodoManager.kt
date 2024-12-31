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
        todoList.add(
            Todo(
                id = generateUniqueId(),
                title = title,
                createdAt = Date.from(Instant.now()),
                deadline = deadline,
                isProject = isProject
            )
        )
        saveTodos()
    }

    fun deleteTodo(id: Int) {
        todoList.removeIf { it.id == id }
        saveTodos()
    }

    fun markAsCompleted(id: Int) {
        val todo = todoList.find { it.id == id }
        todo?.let {
            it.isCompleted = !it.isCompleted
            saveTodos()
        }
    }

    fun getProjectById(id: Int): Todo? {
        return todoList.find { it.id == id && it.isProject }
    }

    fun addTaskToProject(projectId: Int, taskTitle: String, deadline: Date? = null) {
        val project = getProjectById(projectId)
        project?.let {
            val task = Todo(
                id = generateUniqueId(),
                title = taskTitle,
                createdAt = Date.from(Instant.now()),
                deadline = deadline
            )
            it.tasks.add(task)
            saveTodos()
        } ?: throw IllegalArgumentException("Project with ID $projectId not found")
    }

    fun editProject(projectId: Int, newTitle: String, newDescription: String?, newDeadline: Date?) {
        val project = getProjectById(projectId)
        project?.let {
            it.title = newTitle
            it.deadline = newDeadline
            it.description = newDescription
            saveTodos()
        }
    }

    fun deleteTaskFromProject(projectId: Int, taskId: Int) {
        val project = getProjectById(projectId)
        project?.tasks?.removeIf { it.id == taskId }
        saveTodos()
    }

    fun markTaskAsCompleted(projectId: Int, taskId: Int) {
        val project = getProjectById(projectId)
        val task = project?.tasks?.find { it.id == taskId }
        task?.let {
            it.isCompleted = !it.isCompleted
            saveTodos()
        }
    }

    fun updateProject(
        projectId: Int,
        newDescription: String?,
        newDeadline: Date?,
        updatedTasks: List<Todo>?,
        onProjectCompletionDecision: Boolean
    ) {
        val project = getProjectById(projectId)
        project?.let {
            // Update project description and deadline
            it.description = newDescription
            it.deadline = newDeadline

            // Update tasks or add new tasks
            updatedTasks?.forEach { updatedTask ->
                val existingTask = it.tasks.find { task -> task.id == updatedTask.id }
                if (existingTask != null) {
                    // Do not modify the content of existing tasks, just update their status if needed
                    existingTask.isCompleted = updatedTask.isCompleted // Only update the status (completed or not)
                } else {
                    // If the task doesn't exist, add it as a new task
                    it.tasks.add(updatedTask)
                }
            }

            // Check if project is completed
            if (it.tasks.isEmpty()) {
                it.isCompleted = false // If no tasks, project is not complete
            } else if (it.tasks.all { task -> task.isCompleted }) {
                if (onProjectCompletionDecision) {
                    // Logic to decide what happens when all tasks are completed (e.g., finalizing the project)
                }
            }

            // Save the updated project and the tasks list
            saveProjectState(it)
            saveTodos()
        } ?: throw IllegalArgumentException("Project with ID $projectId not found")
    }



    public fun generateUniqueId(): Int {
        return (todoList.maxOfOrNull { it.id } ?: 0) + 1
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

    fun saveProjectState(project: Todo) {
        val json = gson.toJson(project)
        preferences.edit().putString("project_${project.id}", json).apply()
    }

    fun loadProjectState(projectId: Int): Todo? {
        val json = preferences.getString("project_${projectId}", null)
        return if (json != null) {
            gson.fromJson(json, Todo::class.java)
        } else {
            null
        }
    }
}

