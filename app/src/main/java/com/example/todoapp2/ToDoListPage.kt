import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todoapp2.NotificationScheduler
import com.example.todoapp2.R
import com.example.todoapp2.Todo
import com.example.todoapp2.TodoManager
import com.example.todoapp2.TodoManager.generateUniqueId
import com.example.todoapp2.TodoManager.saveProjectState
import com.example.todoapp2.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun TodoListPage(viewModel: TodoViewModel, context: Context) {
    val todoList by viewModel.todoList.observeAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf<Date?>(null) }
    var hasDeadline by remember { mutableStateOf(false) }
    var isProject by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    var showModal by remember { mutableStateOf<Todo?>(null) } // Przechowuje projekt do wyświetlenia w modalnym oknie
    // Stan dla filtrowania
    var showTasks by remember { mutableStateOf(true) } // Pokaż zadania domyślnie
    var showProjects by remember { mutableStateOf(true) } // Pokaż projekty domyślnie

    var selectedTask by remember { mutableStateOf<Todo?>(null) }

    val draggingItemId = remember { mutableStateOf<Int?>(null) }
    val dragOffset = remember { mutableStateOf(0f) }

    var todoToDelete by remember { mutableStateOf<Todo?>(null) }



    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Dodaj zadanie...") },
                modifier = Modifier
                    .weight(0.9f)
                    .padding(end = 8.dp),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (inputText.isNotEmpty()) {
                            if (hasDeadline) {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        calendar.set(Calendar.YEAR, year)
                                        calendar.set(Calendar.MONTH, month)
                                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                calendar.set(Calendar.MINUTE, minute)
                                                selectedDeadline = calendar.time
                                                viewModel.addTodo(context, inputText, selectedDeadline, isProject)
                                                inputText = ""
                                                selectedDeadline = null
                                                isProject = false
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } else {
                                viewModel.addTodo(context, inputText, null, isProject)
                                inputText = ""
                                isProject = false
                            }
                        } else {
                            Toast.makeText(context, "Wpisz treść zadania", Toast.LENGTH_SHORT).show()
                        }
                    }
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done // Ustawienie akcji "Done" (Enter)
                )
            )


            IconButton(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        if (hasDeadline) {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            selectedDeadline = calendar.time
                                            viewModel.addTodo(context, inputText, selectedDeadline, isProject)
                                            inputText = ""
                                            selectedDeadline = null
                                            isProject = false
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        } else {
                            viewModel.addTodo(context, inputText, null, isProject)
                            inputText = ""
                            isProject = false
                        }
                    } else {
                        Toast.makeText(context, "Wpisz treść zadania", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(0.1f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Task",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Deadline", fontSize = 12.sp)
                Checkbox(
                    checked = hasDeadline,
                    onCheckedChange = { hasDeadline = it }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Zadanie", fontSize = 12.sp)
                IconToggleButton(
                    checked = !isProject,
                    onCheckedChange = { isProject = !it },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_task),
                        contentDescription = "Task",
                        tint = if (!isProject) Color(0xFFb08968) else Color.Gray
                    )
                }

                Text(text = "Projekt", fontSize = 12.sp)
                IconToggleButton(
                    checked = isProject,
                    onCheckedChange = { isProject = it },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_project),
                        contentDescription = "Project",
                        tint = if (isProject) Color(0xFFD5BDAD) else Color.Gray
                    )
                }
            }
        }

        // Dodanie linii oddzielającej
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Divider(
                modifier = Modifier
                    .height(2.dp),
                color = Color.Gray
            )
        }

        // Lista zadań
        todoList?.let {

            // Filtrujemy listę w oparciu o checkboxy
            val filteredList = todoList?.filter { todo ->
                (showTasks && !todo.isProject) || (showProjects && todo.isProject)
            }
            LazyColumn(
                content = {
                    itemsIndexed(filteredList ?: emptyList()) { index, item ->
                        AnimatedVisibility(
                            visible = true, // Elementy zawsze widoczne na liście
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            TodoItem(
                                item = item,
                                onClick = {
                                    if (item.isProject) showModal = item else selectedTask = item
                                },
                                onDelete = { todoToDelete = item },
                                onMarkComplete = { viewModel.markAsCompleted(context, item.id) },
                                onMoveUp = { id -> viewModel.moveItemUp(id) },
                                onMoveDown = { id -> viewModel.moveItemDown(id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 40.dp)
            )

        } ?: Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Brak zadań - dodaj pierwsze",
            fontSize = 16.sp
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        // Inne elementy mogą być tutaj, np. lista zadań itp.

        Spacer(modifier = Modifier.weight(1f)) // Wypełnia całą przestrzeń do dołu

        // Dolny pasek z checkboxami
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Zagięte górne rogi
                    .background(Color(0xFF090909)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zadania checkbox z marginesem
                Row(
                    modifier = Modifier.padding(start = 16.dp), // Dodaj margines od lewej
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Zadania", fontSize = 12.sp, color = Color.White)
                    Checkbox(
                        checked = showTasks,
                        onCheckedChange = { showTasks = it }
                    )
                }

                // Kalendarz w centrum
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar_view), // Zmien na odpowiednią ikonę kalendarza
                    contentDescription = "Calendar Icon",
                    tint = Color.Gray,
                    modifier = Modifier.size(30.dp) // Możesz dostosować rozmiar
                )

                // Projekty checkbox z marginesem
                Row(
                    modifier = Modifier.padding(end = 16.dp), // Dodaj margines od prawej
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Projekty", fontSize = 12.sp, color = Color.White)
                    Checkbox(
                        checked = showProjects,
                        onCheckedChange = { showProjects = it }
                    )
                }
            }
        }
    }

    showModal?.let { project ->
        ProjectDetailDialog(
            project = project,
            onClose = {
                showModal = null
                viewModel.updateProject(project, false)
                saveProjectState(project)
                Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
            },
            onSave = {
                // Zapisanie zmian projektu w ViewModel
                viewModel.updateProject(project, false)

                // Zapisanie stanu projektu w pamięci
                saveProjectState(project)

                showModal = null
            },
            onEdit = {

            }
        )
    }
    selectedTask?.let { task ->
        TaskDetailDialog(
            task = task,
            onClose = { selectedTask = null },
            onSave = { updatedTask ->
                viewModel.updateTodo(context, updatedTask) // Zapisz zmiany w ViewModel
                selectedTask = null // Zamknij modalne okno
            }
        )
    }

    todoToDelete?.let { todo ->
        CustomStyledAlertDialog(
            title = "Potwierdzenie usunięcia",
            message = "Czy na pewno chcesz usunąć \"${todo.title}\"?",
            onConfirm = {
                viewModel.deleteTodo(context, todo.id) // Usuń zadanie/projekt
                todoToDelete = null // Zamknij dialog
            },
            onDismiss = {
                todoToDelete = null // Zamknij dialog bez usuwania
            }
        )
    }


}

@Composable
fun CustomStyledAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFb08968), // Kolor ramki
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = Color(0xFF090909) // Tło dialogu
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nagłówek z ikoną
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo), // Zmień na swoje logo
                        contentDescription = "Logo",
                        tint = Color.Unspecified, // Ikona z normalnymi kolorami
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White), // Zmniejszony tekst
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Treść komunikatu
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Przyciski TAK i NIE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Przycisk TAK
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe5383b)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Usuń",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Przycisk NIE
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788)),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Zachowaj",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun TodoItem(
    item: Todo,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkComplete: () -> Unit,
    modifier: Modifier = Modifier,
    onMoveUp: (Int) -> Unit, // Przesunięcie w górę
    onMoveDown: (Int) -> Unit // Przesunięcie w dół
) {
    val targetBackgroundColor = when {
        item.isProject && item.isCompleted -> Color(0xFF74d3ae)
        item.isProject -> Color(0xFFD5BDAD)
        item.isCompleted -> Color(0xFF2b9348)
        else -> Color.Transparent
    }

    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor)

    val targetBorderColor = when {
        item.isProject && item.isCompleted -> Color(0xFF74d3ae)
        item.isProject -> Color(0xFFD5BDAD)
        item.isCompleted -> Color(0xFF2b9348)
        else -> Color(0xFFb08968)
    }

    val borderColor by animateColorAsState(targetValue = targetBorderColor)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(12.dp), // Zmniejszenie paddingu w wierszu
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolumna ikon strzałek do przesuwania
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(end = 8.dp)
                .width(24.dp)
        ) {
            IconButton(
                onClick = { onMoveUp(item.id) },
                enabled = TodoManager.canMoveUp(item.id), // W górę to "niższy" element w odwróconej kolejności
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_up),
                    contentDescription = "Move Up",
                    tint = if (TodoManager.canMoveUp(item.id)) Color.LightGray else Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = { onMoveDown(item.id) },
                enabled = TodoManager.canMoveDown(item.id), // W dół to "wyższy" element w odwróconej kolejności
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_down),
                    contentDescription = "Move Down",
                    tint = if (TodoManager.canMoveDown(item.id)) Color.LightGray else Color.Gray
                )
            }
        }


        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = SimpleDateFormat("HH:mm aa, dd/MM", Locale.ENGLISH).format(item.createdAt),
                fontSize = 10.sp,
                color = Color.Gray
            )
            Text(
                text = item.title,
                fontSize = 20.sp,
                color = Color.White
            )
            item.deadline?.let {
                Text(
                    text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(it)}",
                    fontSize = 12.sp,
                    color = Color(0xFFf4f0bb)
                )
            }

            // Pasek postępu dla projektów
            if (item.isProject) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = if (item.tasks.isNotEmpty()) {
                        val completedTasks = item.tasks.count { it.isCompleted }
                        completedTasks.toFloat() / item.tasks.size
                    } else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2b9348),
                    trackColor = Color(0xFFb08968)
                )
            }
        }
        IconButton(onClick = onMarkComplete) {
            Icon(
                painter = painterResource(id = if (item.isCompleted) R.drawable.ic_uncheck else R.drawable.ic_check),
                contentDescription = "Complete",
                tint = Color.White
            )
        }
        IconButton(onClick = onDelete) { // Użycie funkcji przekazanej jako parametr
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Delete",
                tint = Color.White
            )
        }

    }
}



@Composable
fun TaskDetailDialog(
    task: Todo,
    onClose: () -> Unit,
    onSave: (Todo) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var deadline by remember { mutableStateOf(task.deadline) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }


    Dialog(onDismissRequest = {
        try {
            if (title.isNotBlank()) {
                onSave(task.copy(title = title, deadline = deadline)) // Zapisz zmiany
            } else {
                Toast.makeText(context, "Tytuł nie może być pusty!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("TaskDetailDialog", "Błąd podczas zamykania dialogu: ${e.message}")
        } finally {
            onClose() // Zamknij dialog
        }
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Edycja tytułu zadania
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł zadania") }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        // Otwórz dialog do ustawienia powiadomień
                        showNotificationSettingsDialog = true
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification_on),
                            contentDescription = "Ustaw powiadomienia",
                            tint = if (notificationsEnabled) Color.Green else Color.Gray
                        )
                    }
                    IconButton(onClick = {
                        notificationsEnabled = !notificationsEnabled
                        if (!notificationsEnabled) {
                            TodoManager.cancelTaskReminders(context, task.id)
                        } else {
                            TodoManager.scheduleTaskNotifications(context, task)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification_off),
                            contentDescription = "Wycisz powiadomienia",
                            tint = if (notificationsEnabled) Color.Gray else Color.Red
                        )
                    }
                }

                if (showNotificationSettingsDialog && task.notifications != null) {
                    NotificationSettingsDialog(
                        notifications = task.notifications,
                        onAddReminder = { timeValue, timeUnit ->
                            val reminderTime = task.deadline!!.time - timeUnit.toMillis(timeValue.toLong())
                            NotificationScheduler.scheduleTaskReminder(
                                context = context,
                                taskId = task.id,
                                title = task.title,
                                deadline = task.deadline!!.time,
                                offsetMillis = reminderTime,
                                tag = "task_reminder_${task.id}"
                            )
                        },
                        onDismiss = { showNotificationSettingsDialog = false }
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Wyświetlanie i edycja deadline’u
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Jeśli deadline jest ustawiony, wyświetl go
                    if (deadline != null) {
                        Text(
                            text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(deadline)}",
                            modifier = Modifier.weight(1f),
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "Brak deadline’u",
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                    }

                    // Ikona do wyboru deadline’u
                    IconButton(onClick = {
                        // Wybór daty
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                // Po wyborze daty pokaż TimePickerDialog
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        calendar.set(Calendar.MINUTE, minute)
                                        deadline = calendar.time // Zaktualizuj deadline
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = "Dodaj deadline")
                    }

                    // Ikona do usuwania deadline’u
                    IconButton(onClick = {
                        deadline = null // Usuń deadline
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_clear_calendar), // Dodaj odpowiednią ikonę
                            contentDescription = "Usuń deadline"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Przycisk zapisu
                    Button(
                        onClick = {
                            onSave(task.copy(title = title, deadline = deadline)) // Zapisz zmiany
                            Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
                            onClose()
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Zapisz i zamknij")
                    }
                }
            }
        }
    }

}




@Composable
fun ProjectDetailDialog(
    project: Todo,
    onClose: () -> Unit,
    onSave: (Todo) -> Unit,
    onEdit: () -> Unit,
) {
    var description by remember { mutableStateOf(project.description ?: "") }
    var isEditingDescription by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf(project.deadline) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val tasks = remember { mutableStateListOf<Todo>().apply { addAll(project.tasks ?: emptyList()) } }
    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size.toFloat()) * 100 else 0f

    Dialog(onDismissRequest = {
        project.description = description
        project.deadline = deadline
        project.tasks = tasks.toList().toMutableList()
        saveProjectState(project) // Zapisz projekt
        onClose() // Wywołanie zamknięcia
    }) {
        Surface(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = project.title,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Opis projektu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis projektu") },
                        modifier = Modifier.weight(1f),
                        enabled = isEditingDescription
                    )
                    IconButton(onClick = { isEditingDescription = !isEditingDescription }) {
                        Icon(
                            painter = painterResource(id = if (isEditingDescription)
                                R.drawable.ic_edit_end else R.drawable.ic_edit
                            ),
                            contentDescription = if (isEditingDescription) "Zakończ edycję" else "Edytuj"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Wybór deadline’u
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (deadline != null) {
                        Text(
                            text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(deadline)}",
                            modifier = Modifier.weight(1f),
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "Brak deadline’u",
                            modifier = Modifier.weight(1f),
                            color = Color.LightGray
                        )
                    }

                    IconButton(onClick = {
                        // Wybór daty
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                // Wybór godziny
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        calendar.set(Calendar.MINUTE, minute)
                                        deadline = calendar.time // Zaktualizuj deadline
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(painterResource(id = R.drawable.ic_calendar), contentDescription = "Dodaj deadline")
                    }

                    // Usunięcie deadline’u
                    IconButton(onClick = {
                        deadline = null // Usuń deadline
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_clear_calendar), // Dodaj odpowiednią ikonę
                            contentDescription = "Usuń deadline"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wyświetlanie zadań w projekcie
                Text(text = "Zadania:", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.5f),
                ) {
                    items(tasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (task.isCompleted) Color(0xFFD5BDAD) else Color.Transparent)
                                .border(
                                    1.dp,
                                    Color(0xFFD5BDAD),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = {
                                    val index = tasks.indexOf(task)
                                    if (index != -1) {
                                        val updatedTask = task.copy(isCompleted = !task.isCompleted)
                                        tasks[index] = updatedTask
                                    }
                                    TodoManager.markTaskAsCompleted(project.id, task.id, !task.isCompleted)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkmarkColor = if (task.isCompleted) Color.White else Color(0xFFD5BDAD)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp
                            )
                            IconButton(onClick = {
                                tasks.remove(task)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = "Usuń"
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var newTaskTitle by remember { mutableStateOf("") }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Dodaj zadanie") },
                        modifier = Modifier.weight(1f),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (newTaskTitle.isNotEmpty()) {
                                    // Tworzymy nowe zadanie
                                    val newTask = Todo(
                                        title = newTaskTitle,
                                        createdAt = Date(),
                                        id = generateUniqueId()
                                    )
                                    // Dodajemy zadanie do listy zadań projektu
                                    tasks.add(newTask)
                                    newTaskTitle = "" // Czyścimy pole tekstowe po dodaniu zadania
                                }
                            }
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done // Ustawienie akcji "Done" (Enter)
                        )
                    )

                    IconButton(onClick = {
                        if (newTaskTitle.isNotEmpty()) {
                            val newTask = Todo(
                                title = newTaskTitle,
                                createdAt = Date(),
                                id = generateUniqueId()
                            )
                            tasks.add(newTask)
                            newTaskTitle = ""
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Dodaj zadanie"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Postęp: $completedTasks / ${tasks.size}", fontSize = 16.sp)
                LinearProgressIndicator(progress = progress / 100f)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        project.description = description
                        project.deadline = deadline
                        project.tasks = tasks.toList().toMutableList()
                        onSave(project)
                        saveProjectState(project) // Zapisz stan projektu
                        onClose() // Zamknij dialog
                    }) {
                        Text("Zapisz i zamknij")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsDialog(
    notifications: List<Pair<Long, String>>, // Lista obecnych powiadomień
    onAddReminder: (Int, TimeUnit) -> Unit, // Dodanie powiadomienia
    onDismiss: () -> Unit
) {
    var timeValue by remember { mutableStateOf(1) }
    var timeUnit by remember { mutableStateOf(TimeUnit.DAYS) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        TodoManager.saveTodos()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Powiadomienia", style = MaterialTheme.typography.titleMedium)

                LazyColumn {
                    items(notifications) { notification ->
                        Text(text = notification.second)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dodaj przypomnienie:")
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = timeValue.toString(),
                            onValueChange = { timeValue = it.toIntOrNull() ?: 1 },
                            modifier = Modifier.width(64.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Text(
                                text = "Wybierz jednostkę czasu:",
                                modifier = Modifier
                                    .clickable { expanded = true } // Rozwijanie menu
                                    .padding(8.dp)
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }, // Zamknięcie menu
                                modifier = Modifier.wrapContentSize()
                            ) {
                                TimeUnit.values().forEach { unit ->
                                    DropdownMenuItem(
                                        onClick = {
                                            timeUnit = unit
                                            expanded = false
                                        },
                                        text = {
                                            Text(text = unit.name.lowercase())
                                        }
                                    )
                                }
                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    onAddReminder(timeValue, timeUnit)
                }) {
                    Text("Dodaj powiadomienie")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("Zamknij")
                }
            }
        }
    }
}
