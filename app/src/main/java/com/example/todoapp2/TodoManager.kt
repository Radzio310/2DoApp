package com.example.todoapp2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.time.Instant
import java.util.Date
import java.util.UUID

object TodoManager {
    private lateinit var preferences: SharedPreferences
    private val gson = Gson()
    private val todoList = mutableListOf<Todo>()
    private var lastGeneratedId = 0;

    fun init(context: Context) {
        preferences = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        loadLastGeneratedId() // Załaduj ID z pamięci
        loadTodos()
        fixDuplicateOrders()
    }

    fun getAllTodo(): List<Todo> {
        return todoList.sortedWith(compareBy<Todo> { !it.isCompleted }.thenBy { it.order })
    }

    fun updateOrder(id: Int, newOrder: Int) {
        val todo = todoList.find { it.id == id }
        todo?.let {
            it.order = newOrder
            saveTodos()
        }
    }

    fun swapOrder(firstId: Int, secondId: Int) {
        val firstTodo = todoList.find { it.id == firstId }
        val secondTodo = todoList.find { it.id == secondId }

        if (firstTodo != null && secondTodo != null) {
            val tempOrder = firstTodo.order
            firstTodo.order = secondTodo.order
            secondTodo.order = tempOrder

            todoList.sortBy { it.order }
            saveTodos()
        }
    }

    fun canMoveDown(id: Int): Boolean {
        val todoListSorted = getAllTodo() // Upewnij się, że lista jest posortowana
        val currentIndex = todoListSorted.indexOfFirst { it.id == id }

        // Jeśli zadanie jest pierwsze w swojej grupie, nie można go przesunąć w górę
        if (currentIndex <= 0) return false

        val currentItem = todoListSorted[currentIndex]
        val aboveItem = todoListSorted[currentIndex - 1]

        // Można przesunąć w górę tylko w obrębie tej samej grupy (wykonane/niewykonane)
        return currentItem.isCompleted == aboveItem.isCompleted
    }

    fun canMoveUp(id: Int): Boolean {
        val todoListSorted = getAllTodo() // Upewnij się, że lista jest posortowana
        val currentIndex = todoListSorted.indexOfFirst { it.id == id }

        // Jeśli zadanie jest ostatnie w swojej grupie, nie można go przesunąć w dół
        if (currentIndex >= todoListSorted.size - 1) return false

        val currentItem = todoListSorted[currentIndex]
        val belowItem = todoListSorted[currentIndex + 1]

        // Można przesunąć w dół tylko w obrębie tej samej grupy (wykonane/niewykonane)
        return currentItem.isCompleted == belowItem.isCompleted
    }


    fun fixDuplicateOrders() {
        val seenOrders = mutableSetOf<Int>() // Zestaw przechowujący unikalne wartości order
        var maxOrder = todoList.maxOfOrNull { it.order } ?: 0 // Znajdź maksymalne order w liście

        todoList.forEach { todo ->
            if (!seenOrders.add(todo.order)) {
                // Jeśli order się powtarza, przypisz nową unikalną wartość
                maxOrder += 1
                todo.order = maxOrder
            }
        }

        // Posortuj listę według order
        todoList.sortBy { it.order }

        // Zapisz zmienioną listę
        saveTodos()
    }

    fun addTodo(title: String, deadline: Date? = null, isProject: Boolean = false) {
        // Znajdź największy istniejący `order`, jeśli lista nie jest pusta
        val maxOrder = todoList.maxOfOrNull { it.order } ?: 0

        // Dodaj nowe zadanie z `order = maxOrder + 1`
        val newTodo = Todo(
            id = generateUniqueId(),
            title = title,
            createdAt = Date.from(Instant.now()),
            deadline = deadline,
            isProject = isProject,
            order = maxOrder + 1
        )
        todoList.add(newTodo)

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

    fun markTaskAsCompleted(projectId: Int, taskId: Int, newCompletionState: Boolean) {
        val project = getProjectById(projectId)
        val task = project?.tasks?.find { it.id == taskId }
        task?.let {
            it.isCompleted = newCompletionState
            saveTodos()
            saveProjectState(project) // Ensure the updated project state is saved
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

    // Funkcja do generowania unikalnego ID dla zadania
    fun generateUniqueId(): Int {
        lastGeneratedId++ // Inkrementuje ID o 1
        saveLastGeneratedId() // Zapisz nowe ID
        return lastGeneratedId
    }

    // Zapisanie lastGeneratedId do SharedPreferences
    private fun saveLastGeneratedId() {
        preferences.edit().putInt("lastGeneratedId", lastGeneratedId).apply()
    }

    // Załadowanie lastGeneratedId z SharedPreferences
    private fun loadLastGeneratedId() {
        lastGeneratedId = preferences.getInt("lastGeneratedId", 0) // Domyślnie 0, jeśli nie ma zapisanego ID
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

