@file:Suppress("DEPRECATION")

package com.example.todoapp2

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

object TodoManager {
    private lateinit var preferences: SharedPreferences
    private val gson = Gson()
    private val todoList = mutableListOf<Todo>()
    private var lastGeneratedId = 0
    private val labels = mutableListOf<Label>() // Lista dostępnych etykiet


    fun init(context: Context) {
        preferences = context.getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        loadLastGeneratedId() // Załaduj ID z pamięci
        loadTodos()
        loadLabels()
        fixDuplicateOrders()
    }

    fun getAllTodo(): List<Todo> {
        return todoList.sortedWith(compareBy<Todo> { !it.isCompleted }.thenBy { it.order })
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


    private fun fixDuplicateOrders() {
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

    fun cancelTaskReminders(context: Context, taskId: Int) {
        val workManager = androidx.work.WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("task_reminder_$taskId")
    }

    // Zarządzanie etykietami
    fun addLabel(name: String, color: Int): Boolean {
        if (labels.any { it.name == name }) return false
        labels.add(Label(name, color, true)) // Domyślnie nowa etykieta jest widoczna
        saveLabels() // Zapisz listę etykiet
        return true
    }

    fun getLabels(): List<Label> = labels

    private fun saveLabels() {
        val json = gson.toJson(labels)
        preferences.edit().putString("labels", json).apply()
    }

    private fun loadLabels() {
        val json = preferences.getString("labels", null)
        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<Label>>() {}.type
            val savedLabels: MutableList<Label> = gson.fromJson(json, type)
            labels.clear()
            labels.addAll(savedLabels)
        }
    }

    fun updateLabelVisibility(label: Label?, isVisible: Boolean) {
        val existingLabel = labels.find { it.name == label!!.name }
        existingLabel?.isLabelVisible = isVisible
        saveLabels() // Zapisz zmiany widoczności

        // Aktualizacja widoczności zadań
        todoList.forEach { todo ->
            if (todo.label?.name == label!!.name) {
                todo.label?.isLabelVisible = isVisible
            }
        }
        saveTodos()
    }




    fun removeLabel(label: Label) {
        labels.removeIf { it.name == label.name }
        saveLabels() // Zapisanie zmienionej listy etykiet
        // Usuń etykietę z przypisanych zadań
        todoList.forEach { todo ->
            if (todo.label?.name == label.name) {
                todo.label = null
            }
        }
        saveTodos() // Zapisanie zmienionej listy zadań
    }


    fun addTodo(context: Context, title: String, deadline: Date? = null, isProject: Boolean = false) {
        val maxOrder = todoList.maxOfOrNull { it.order } ?: 0
        val defaultNotifications = mutableListOf<Long>()

        // Dodaj domyślne powiadomienia
        defaultNotifications.add(TimeUnit.DAYS.toMillis(1)) // 1 dzień przed
        defaultNotifications.add(TimeUnit.HOURS.toMillis(3)) // 3 godziny przed
        defaultNotifications.add(TimeUnit.MINUTES.toMillis(1)) // 1 minuta przed

        val newTodo = Todo(
            id = generateUniqueId(),
            title = title,
            createdAt = Date.from(Instant.now()),
            deadline = deadline,
            isProject = isProject,
            order = maxOrder + 1,
            notifications = defaultNotifications
        )
        initializeDefaultValues(newTodo) // Ustaw domyślne wartości

        // Ustaw powiadomienia, jeśli istnieje deadline
        if (deadline != null) {
            defaultNotifications.forEach { offsetMillis ->
                val triggerTime = deadline.time - offsetMillis
                if (triggerTime > System.currentTimeMillis()) { // Ustaw powiadomienie tylko w przyszłości
                    NotificationScheduler.scheduleTaskReminder(
                        context = context,
                        taskId = newTodo.id,
                        title = newTodo.title,
                        deadline = deadline.time,
                        offsetMillis = offsetMillis,
                        tag = "task_reminder_${newTodo.id}_$offsetMillis"
                    )
                }
            }
        }
        todoList.add(newTodo)
        saveTodos()
    }


    fun scheduleTaskNotifications(context: Context, todo: Todo) {
        val deadline = todo.deadline // Przechowaj wartość lokalnie, aby uniknąć problemu z castingiem
        if (deadline != null) {
            NotificationScheduler.scheduleTaskReminder(
                context,
                todo.id,
                todo.title,
                deadline.time,
                deadline.time - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                "task_reminder_${todo.id}"
            )
            NotificationScheduler.scheduleTaskReminder(
                context,
                todo.id,
                todo.title,
                deadline.time,
                deadline.time - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3),
                "task_reminder_${todo.id}"
            )
            NotificationScheduler.scheduleTaskReminder(
                context,
                todo.id,
                todo.title,
                deadline.time,
                deadline.time - System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1),
                "task_reminder_${todo.id}"
            )
        }
    }

    fun getIncompleteTasksCount(): Int {
        return todoList.count { !it.isCompleted && !it.isProject }
    }

    fun getIncompleteProjectsCount(): Int {
        return todoList.count { !it.isCompleted && it.isProject }
    }


    fun deleteTodo(context: Context, id: Int) {
        NotificationScheduler.cancelTaskReminders(context, id) // Usuwanie powiadomień
        cancelTaskReminders(context, id)
        todoList.removeIf { it.id == id }
        saveTodos()
    }

    fun markAsCompleted(context: Context, id: Int) {
        val todo = todoList.find { it.id == id }
        todo?.let {
            it.isCompleted
            it.isCompleted = !it.isCompleted
            it.completedAt = if (it.isCompleted) Date() else null // Zapisz datę wykonania lub usuń ją
            saveTodos()

            if (it.isCompleted) {
                // Jeśli oznaczono jako wykonane, usuń powiadomienia
                NotificationScheduler.cancelTaskReminders(context, it.id)
            } else {
                // Jeśli odznaczono jako wykonane, przywróć powiadomienia
                it.deadline?.let { deadline ->
                    NotificationScheduler.scheduleTaskReminder(
                        context,
                        it.id,
                        it.title,
                        deadline.time,
                        deadline.time - System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                        "task_reminder_${it.id}"
                    )
                    NotificationScheduler.scheduleTaskReminder(
                        context,
                        it.id,
                        it.title,
                        deadline.time,
                        deadline.time - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3),
                        "task_reminder_${it.id}"
                    )
                    NotificationScheduler.scheduleTaskReminder(
                        context,
                        it.id,
                        it.title,
                        deadline.time,
                        0,
                        "task_reminder_${it.id}"
                    )
                }
            }
        }
    }


    private fun getProjectById(id: Int): Todo? {
        return todoList.find { it.id == id && it.isProject }
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

    fun removeTodoDeadline(context: Context, todoId: Int) {
        val existingTodo = todoList.find { it.id == todoId }
        existingTodo?.let {
            cancelTaskReminders(context, it.id) // Anuluj istniejące powiadomienia
            it.deadline = null
            saveTodos()
        }
    }

    fun updateTodoDeadline(context: Context, todo: Todo, newDeadline: Date?) {
        val existingTodo = todoList.find { it.id == todo.id }
        existingTodo?.let {
            cancelTaskReminders(context, it.id) // Anuluj istniejące powiadomienia
            it.deadline = newDeadline
            saveTodos()

            // Jeśli jest nowy deadline, zaplanuj powiadomienia
            if (newDeadline != null) {
                scheduleTaskNotifications(context, it)
            }
        }
    }


    fun updateProject(
        context: Context,
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
                Toast.makeText(context, "PROJEKT UKOŃCZONY! Gratulacje🎉", Toast.LENGTH_SHORT).show()
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




    fun saveTodos() {
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

            // Inicjalizacja brakujących wartości
            todoList.forEach { initializeDefaultValues(it) }
        }
    }


    private fun initializeDefaultValues(todo: Todo) {
        todo.notifications = todo.notifications // Ustaw puste powiadomienia, jeśli brak
        todo.areNotificationsDisabled =
            todo.areNotificationsDisabled // Domyślnie powiadomienia włączone
        todo.tasks = todo.tasks // Dla projektów ustaw pustą listę zadań
        todo.description = todo.description ?: "" // Opis domyślnie pusty
    }

    fun saveProjectState(project: Todo) {
        val json = gson.toJson(project)
        preferences.edit().putString("project_${project.id}", json).apply()
    }

    //STATYSTYKI UŻYTKOWNIKA

    fun getStatsForYear(year: Int): Map<String, Int> {
        val todosForYear = todoList.filter {
            it.createdAt.year == year - 1900 // Dopasowanie roku dodania
        }
        val completedTodosForYear = todoList.filter {
            it.completedAt?.year == year - 1900 // Dopasowanie roku wykonania
        }

        return mapOf(
            "tasksAdded" to todosForYear.count { !it.isProject },
            "tasksCompleted" to completedTodosForYear.count { !it.isProject },
            "projectsAdded" to todosForYear.count { it.isProject },
            "projectsCompleted" to completedTodosForYear.count { it.isProject }
        )
    }

    fun getStatsForMonth(year: Int, month: Int): Map<String, Int> {
        val todosForMonth = todoList.filter {
            it.createdAt.year == year - 1900 && it.createdAt.month == month - 1 // Dopasowanie roku i miesiąca dodania
        }
        val completedTodosForMonth = todoList.filter {
            it.completedAt?.year == year - 1900 && it.completedAt?.month == month - 1 // Dopasowanie roku i miesiąca wykonania
        }

        return mapOf(
            "tasksAdded" to todosForMonth.count { !it.isProject },
            "tasksCompleted" to completedTodosForMonth.count { !it.isProject },
            "projectsAdded" to todosForMonth.count { it.isProject },
            "projectsCompleted" to completedTodosForMonth.count { it.isProject }
        )
    }


    fun getStatsForCustomRange(start: Date, end: Date): Map<String, Int> {
        val todosInRange = todoList.filter {
            it.createdAt.time >= start.time && it.createdAt.time <= end.time
        }
        val completedTodosInRange = todoList.filter {
            it.completedAt != null && it.completedAt!!.time >= start.time && it.completedAt!!.time <= end.time
        }

        return mapOf(
            "tasksAdded" to todosInRange.count { !it.isProject },
            "tasksCompleted" to completedTodosInRange.count { !it.isProject },
            "projectsAdded" to todosInRange.count { it.isProject },
            "projectsCompleted" to completedTodosInRange.count { it.isProject }
        )
    }



}