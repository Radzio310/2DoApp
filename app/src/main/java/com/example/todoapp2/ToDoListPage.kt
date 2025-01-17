import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import com.example.todoapp2.TodoManager.removeTodoDeadline
import com.example.todoapp2.TodoManager.saveProjectState
import com.example.todoapp2.TodoManager.updateTodoDeadline
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

                                                // Sprawdzenie, czy deadline jest w przeszłości
                                                if (selectedDeadline!!.before(Date())) {
                                                    Toast.makeText(context, "Wybrany czas już minął!", Toast.LENGTH_SHORT).show()
                                                }

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

                                            // Sprawdzenie, czy deadline jest w przeszłości
                                            if (selectedDeadline!!.before(Date())) {
                                                Toast.makeText(context, "Wybrany czas już minął!", Toast.LENGTH_SHORT).show()
                                            }

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
            HorizontalDivider(
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

        CustomBottomBar(
            showTasks = showTasks,
            showProjects = showProjects,
            onTasksToggle = { showTasks = !showTasks },
            onProjectsToggle = { showProjects = !showProjects }
        )
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
            task = selectedTask!!,
            onClose = { selectedTask = null },
            onSave = { updatedTask ->
                viewModel.updateTodo(context, updatedTask, selectedTask?.deadline) // Przekazanie oldDeadline
                selectedTask = null
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
fun CustomBottomBar(
    showTasks: Boolean,
    showProjects: Boolean,
    onTasksToggle: () -> Unit,
    onProjectsToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) // Zaokrąglenie górnych rogów
            .background(Color(0xFF090909)) // Kolor tła
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lewa część z "Wyświetl: " i ikonami zadań/projektów
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Wyświetl:",
                fontSize = 14.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Ikona zadania
            Icon(
                painter = painterResource(id = R.drawable.ic_task),
                contentDescription = "Zadania",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onTasksToggle() },
                tint = if (showTasks) Color(0xFFb08968) else Color.Gray
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Ikona projektu
            Icon(
                painter = painterResource(id = R.drawable.ic_project),
                contentDescription = "Projekty",
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onProjectsToggle() },
                tint = if (showProjects) Color(0xFFD5BDAD) else Color.Gray
            )
        }

        // Środkowa część z ikoną kalendarza
        Icon(
            painter = painterResource(id = R.drawable.ic_calendar_view),
            contentDescription = "Kalendarz",
            modifier = Modifier.size(30.dp),
            tint = Color.Gray
        )

        // Prawa część z ikoną użytkownika
        Icon(
            painter = painterResource(id = R.drawable.ic_user),
            contentDescription = "Użytkownik",
            modifier = Modifier
                .size(30.dp),
            tint = Color.Gray // Na razie wyszarzona i nieaktywna
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
    // Przechowywanie animacji w kontekście konkretnego zadania
    var isAnimatingCompletion by remember(item.id) { mutableStateOf(false) }

    val targetBackgroundColor by animateColorAsState(
        targetValue = when {
            isAnimatingCompletion -> Color(0xFF2b9348) // Docelowy kolor po wykonaniu
            item.isProject && item.isCompleted -> Color(0xFF74d3ae)
            item.isProject -> Color(0xFFD5BDAD)
            item.isCompleted -> Color(0xFF2b9348)
            else -> Color.Transparent
        }
    )

    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor)

    val targetBorderColor = when {
        item.isProject && item.isCompleted -> Color(0xFF74d3ae)
        item.isProject -> Color(0xFFD5BDAD)
        item.isCompleted -> Color(0xFF2b9348)
        else -> Color(0xFFb08968)
    }

    val borderColor by animateColorAsState(targetValue = targetBorderColor)

    LaunchedEffect(isAnimatingCompletion) {
        if (isAnimatingCompletion) {
            // Poczekaj na zakończenie animacji
            kotlinx.coroutines.delay(300) // Czas animacji (300ms)
            onMarkComplete() // Oznacz jako wykonane po zakończeniu animacji
        }
    }



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
        IconButton(onClick = { isAnimatingCompletion = true }) {
            Icon(
                painter = painterResource(
                    id = if (item.isCompleted) R.drawable.ic_uncheck else R.drawable.ic_check
                ),
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailDialog(
    task: Todo,
    onClose: () -> Unit,
    onSave: (Todo) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var deadline by remember { mutableStateOf(task.deadline) }
    var areNotificationsDisabled by remember { mutableStateOf(task.areNotificationsDisabled) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val oldDeadline = deadline
    var showAddNotificationDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        onSave(task.copy(title = title, deadline = deadline)) // Zapisz zmiany
        Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
        onClose() // Zamknij dialog
    }) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFb08968),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = Color(0xFF090909) // Tło dialogu
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Nagłówek z logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo), // Logo aplikacji
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Szczegóły zadania",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    // Ikony powiadomień
                    IconButton(
                        onClick = {
                            if (!areNotificationsDisabled) {
                                showAddNotificationDialog = true
                            } else {
                                Toast.makeText(context, "Wyciszenie powiadomień jest aktywne. Wyłącz, aby dodać powiadomienia.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !areNotificationsDisabled // Wyłącz, gdy powiadomienia są wyciszone
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification_add),
                            contentDescription = "Dodaj powiadomienia",
                            tint = if (!areNotificationsDisabled) Color(0xFF52b788) else Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            areNotificationsDisabled = !areNotificationsDisabled
                            if (areNotificationsDisabled) {
                                NotificationScheduler.cancelTaskReminders(context, task.id)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification_off),
                            contentDescription = "Wycisz powiadomienia",
                            tint = if (areNotificationsDisabled) Color(0xFFc1121f) else Color.Gray
                        )
                    }
                }
                // Modalne okno dodawania powiadomienia
                if (showAddNotificationDialog) {
                    val oldNotifications = task.notifications.toList()
                    AddNotificationDialog(
                        onDismiss = { showAddNotificationDialog = false },
                        initialNotifications = task.notifications,
                        oldNotifications = oldNotifications,
                        onSaveChanges = { updatedNotifications ->
                            if (updatedNotifications != oldNotifications) {
                                NotificationScheduler.cancelTaskReminders(context, task.id)

                                updatedNotifications.forEach { offset ->
                                    val triggerTime = task.deadline?.time?.minus(offset)
                                    if (triggerTime != null && triggerTime > System.currentTimeMillis()) {
                                        NotificationScheduler.scheduleTaskReminder(
                                            context,
                                            task.id,
                                            task.title,
                                            task.deadline!!.time,
                                            triggerTime - System.currentTimeMillis(),
                                            "task_reminder_${task.id}_${offset}"
                                        )
                                    }
                                }
                                task.notifications.clear()
                                task.notifications.addAll(updatedNotifications)

                            }
                        },
                    )
                }



                Spacer(modifier = Modifier.height(16.dp))

                // Edycja tytułu zadania
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł zadania", color = Color.LightGray) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFb08968),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Wyświetlanie i edycja deadline’u
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (deadline != null) {
                        Text(
                            text = "Deadline:\n$${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(deadline)}",
                            fontSize = 14.sp,
                            color = Color(0xFFf4f0bb), // Dopasowany kolor
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = "Brak deadline’u",
                            color = Color.LightGray
                        )
                    }

                    IconButton(onClick = {
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
                                        deadline = calendar.time

                                        // Sprawdzenie, czy deadline jest w przeszłości
                                        if (deadline!!.before(Date())) {
                                            Toast.makeText(
                                                context,
                                                "Wybrany czas już minął!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

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
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Dodaj deadline",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { deadline = null }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_clear_calendar),
                            contentDescription = "Usuń deadline",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Przycisk "Zapisz i zamknij"
                Button(
                    onClick = {
                        // Tworzymy kopię zadania z aktualnym stanem
                        val updatedTask = task.copy(
                            title = title,
                            deadline = deadline,
                            areNotificationsDisabled = areNotificationsDisabled,
                            notifications = task.notifications.toMutableList()
                        )

                        if (areNotificationsDisabled) {
                            NotificationScheduler.cancelTaskReminders(context, task.id)
                        } else {
                            task.notifications.forEach { offset ->
                                val triggerTime = deadline?.time?.minus(offset)
                                if (triggerTime != null && triggerTime > System.currentTimeMillis()) {
                                    NotificationScheduler.scheduleTaskReminder(
                                        context,
                                        task.id,
                                        task.title,
                                        deadline!!.time,
                                        triggerTime - System.currentTimeMillis(),
                                        "task_reminder_${task.id}_${offset}"
                                    )
                                }
                            }
                        }

                        // Zapisujemy zmiany zadania
                        onSave(updatedTask)

                        // Wyświetlamy powiadomienie Toast
                        Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()

                        // Zamykamy dialog
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(text = "Zapisz i zamknij", color = Color.White)
                }
            }
        }
    }
}


@Composable
fun AddNotificationDialog(
    onDismiss: () -> Unit,
    initialNotifications: List<Long>,
    oldNotifications: List<Long>,
    onSaveChanges: (List<Long>) -> Unit
) {
    var amount by remember { mutableStateOf(1) }
    var unit by remember { mutableStateOf("minut") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(initialNotifications.toMutableList()) }
    val context = LocalContext.current

    Dialog(onDismissRequest = {
        if (notifications != oldNotifications) {
            Toast.makeText(context, "Zaktualizowano przypomnienia", Toast.LENGTH_SHORT).show()
        }
        onSaveChanges(notifications)
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = Color(0xFF090909), // Tło dialogu
            modifier = Modifier
                .padding(16.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFb08968),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nagłówek z logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo), // Logo aplikacji
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Przypomnienia", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista przypomnień
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                        .padding(bottom = 16.dp)
                ) {
                    items(notifications.reversed()) { notification ->
                        val timeText = when {
                            notification >= TimeUnit.DAYS.toMillis(1) -> "${notification / TimeUnit.DAYS.toMillis(1)} dni przed"
                            notification >= TimeUnit.HOURS.toMillis(1) -> "${notification / TimeUnit.HOURS.toMillis(1)} godzin przed"
                            else -> "${notification / TimeUnit.MINUTES.toMillis(1)} minut przed"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeText,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                notifications = notifications.toMutableList().also { it.remove(notification) }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = "Usuń przypomnienie",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wybór liczby i jednostki
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { if (amount > 1) amount-- }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_remove),
                            contentDescription = "Zmniejsz",
                            tint = Color(0xFFb08968)
                        )
                    }
                    Text(amount.toString(), fontSize = 24.sp, color = Color.White)
                    IconButton(onClick = { amount++ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = "Zwiększ",
                            tint = Color(0xFFb08968)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { isDropdownExpanded = true } // Rozwiń listę przy kliknięciu
                            .padding(vertical = 8.dp)
                    ) {
                        Text(unit, fontSize = 18.sp, color = Color(0xFFb08968))
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dropdown), // Ikona rozwijanego menu
                            contentDescription = "Rozwiń listę",
                            tint = Color(0xFFb08968),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("przed deadlinem", color = Color.White)
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFF090909))
                            .border(1.dp, Color(0xFFb08968), RoundedCornerShape(8.dp))
                            .align(Alignment.Center),
                    ) {
                        listOf("minut", "godzin", "dni").forEach {
                            DropdownMenuItem(
                                onClick = {
                                    unit = it
                                    isDropdownExpanded = false
                                },
                                text = {
                                    Text(it, color = Color(0xFFb08968))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF090909))
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val offsetMillis = when (unit) {
                            "minut" -> TimeUnit.MINUTES.toMillis(amount.toLong())
                            "godzin" -> TimeUnit.HOURS.toMillis(amount.toLong())
                            "dni" -> TimeUnit.DAYS.toMillis(amount.toLong())
                            else -> 0L
                        }
                        if (!notifications.contains(offsetMillis)) {
                            notifications = notifications.toMutableList().also { it.add(offsetMillis) }
                            Toast.makeText(context, "Dodano przypomnienie", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Już istnieje takie przypomnienie", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                ) {
                    Text("Dodaj przypomnienie", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveChanges(notifications)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788))
                ) {
                    Text("Zapisz zmiany", color = Color.White)
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
    val oldDeadline = deadline

    val tasks = remember { mutableStateListOf<Todo>().apply { addAll(project.tasks ?: emptyList()) } }
    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size.toFloat()) * 100 else 0f

    var showAddNotificationDialog by remember { mutableStateOf(false) }
    var areNotificationsDisabled by remember { mutableStateOf(project.areNotificationsDisabled) }

    Dialog(onDismissRequest = {
        project.description = description
        project.deadline = deadline
        project.tasks = tasks.toList().toMutableList()
        project.areNotificationsDisabled = areNotificationsDisabled
        saveProjectState(project) // Zapisz projekt
        onClose() // Wywołanie zamknięcia
    }) {
        Surface(
            modifier = Modifier
                .padding(4.dp)
                .border(
                    width = 2.dp,
                    color = Color(0xFFb08968),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = Color(0xFF090909)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Logo i ikony w pierwszym wierszu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp), // Minimalny odstęp od górnej ramki
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo po lewej stronie
                    Icon(
                        painter = painterResource(id = R.drawable.logo_2),
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )

                    // Ikony powiadomień po prawej stronie, bliżej siebie
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp) // Mniejszy odstęp między ikonami
                    ) {
                        IconButton(
                            onClick = {
                                if (!areNotificationsDisabled) {
                                    showAddNotificationDialog = true
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Wyciszenie powiadomień jest aktywne. Wyłącz, aby dodać powiadomienia.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !areNotificationsDisabled
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification_add),
                                contentDescription = "Edytuj powiadomienia",
                                tint = if (!areNotificationsDisabled) Color(0xFF52b788) else Color.Gray
                            )
                        }

                        IconButton(
                            onClick = {
                                areNotificationsDisabled = !areNotificationsDisabled
                                if (areNotificationsDisabled) {
                                    NotificationScheduler.cancelTaskReminders(context, project.id)
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification_off),
                                contentDescription = "Wycisz powiadomienia",
                                tint = if (areNotificationsDisabled) Color(0xFFc1121f) else Color.Gray
                            )
                        }
                    }
                }

                // Tytuł projektu
                Text(
                    text = project.title,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp) // Odstęp od logo i ikon
                )

                Spacer(modifier = Modifier.height(4.dp)) // Minimalny odstęp dla estetyki

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
                            text = "Deadline:\n${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(deadline)}",
                            fontSize = 14.sp,
                            color = Color(0xFFf4f0bb), // Dopasowany kolor
                            modifier = Modifier.weight(1f)
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

                                        // Sprawdzenie, czy deadline jest w przeszłości
                                        if (deadline!!.before(Date())) {
                                            Toast.makeText(context, "Wybrany czas już minął!", Toast.LENGTH_SHORT).show()
                                        }
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
                        removeTodoDeadline(context, project.id)
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
                                        val updatedTask = task.copy(
                                            isCompleted = !task.isCompleted,
                                            notifications = task.notifications ?: mutableListOf() // Upewnij się, że nie jest null
                                        )
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
                    // Przycisk "Zapisz i zamknij"
                    Button(
                        onClick = {
                            project.description = description
                            project.deadline = deadline
                            project.areNotificationsDisabled = areNotificationsDisabled // Zapisz stan wyciszenia
                            project.tasks = tasks.toList().toMutableList()
                            if (oldDeadline != deadline) {
                                updateTodoDeadline(context, project, deadline)
                            }
                            onSave(project)
                            saveProjectState(project) // Zapisz stan projektu
                            onClose() // Zamknij dialog
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text(text = "Zapisz i zamknij", color = Color.White)
                    }
                }
            }
        }
        // Dialog edycji przypomnień
        if (showAddNotificationDialog) {
            val oldNotifications = project.notifications.toList()
            AddNotificationDialog(
                onDismiss = {
                    showAddNotificationDialog = false
                    if (project.notifications != oldNotifications) {
                        Toast.makeText(context, "Zaktualizowano przypomnienia", Toast.LENGTH_SHORT).show()
                    }
                },
                initialNotifications = project.notifications,
                oldNotifications = oldNotifications,
                onSaveChanges = { updatedNotifications ->
                    if (updatedNotifications != oldNotifications) {
                        NotificationScheduler.cancelTaskReminders(context, project.id)
                        updatedNotifications.forEach { offset ->
                            val triggerTime = project.deadline?.time?.minus(offset)
                            if (triggerTime != null && triggerTime > System.currentTimeMillis()) {
                                NotificationScheduler.scheduleTaskReminder(
                                    context,
                                    project.id,
                                    project.title,
                                    project.deadline!!.time,
                                    triggerTime - System.currentTimeMillis(),
                                    "project_reminder_${project.id}_${offset}"
                                )
                            }
                        }
                        project.notifications.clear()
                        project.notifications.addAll(updatedNotifications)
                        saveProjectState(project)
                    }
                }
            )
        }
    }
}

