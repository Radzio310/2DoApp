import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todoapp2.CalendarScreen
import com.example.todoapp2.Label
import com.example.todoapp2.NotificationScheduler
import com.example.todoapp2.R
import com.example.todoapp2.Todo
import com.example.todoapp2.TodoManager
import com.example.todoapp2.TodoManager.generateUniqueId
import com.example.todoapp2.TodoManager.removeTodoDeadline
import com.example.todoapp2.TodoManager.saveProjectState
import com.example.todoapp2.TodoManager.updateLabelVisibility
import com.example.todoapp2.TodoManager.updateTodoDeadline
import com.example.todoapp2.TodoViewModel
import com.example.todoapp2.UserScreen
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
    var showWithoutLabel by remember { mutableStateOf(true) } // Domyślnie pokaż zadania bez etykiety

    var selectedTask by remember { mutableStateOf<Todo?>(null) }

    var todoToDelete by remember { mutableStateOf<Todo?>(null) }

    var isUserScreenVisible by remember { mutableStateOf(false) }

    var isCalendarVisible by remember { mutableStateOf(false) }


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
                                                        Toast.makeText(
                                                            context,
                                                            "Wybrany czas już minął!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }

                                                    viewModel.addTodo(
                                                        context,
                                                        inputText,
                                                        selectedDeadline,
                                                        isProject
                                                    )
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
                                Toast.makeText(context, "Wpisz treść zadania", Toast.LENGTH_SHORT)
                                    .show()
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
                                                    Toast.makeText(
                                                        context,
                                                        "Wybrany czas już minął!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                viewModel.addTodo(
                                                    context,
                                                    inputText,
                                                    selectedDeadline,
                                                    isProject
                                                )
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
                            Toast.makeText(context, "Wpisz treść zadania", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    modifier = Modifier
                        .weight(0.1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add_task),
                        contentDescription = "Add Task",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Deadline", fontSize = 12.sp)
                    Checkbox(
                        checked = hasDeadline,
                        onCheckedChange = { hasDeadline = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFb08968),
                            uncheckedColor = Color.Gray
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ikona dla zadania
                    IconButton(
                        onClick = { isProject = false }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_task),
                            contentDescription = "Zadanie",
                            tint = if (!isProject) Color(0xFFb08968) else Color.Gray
                        )
                    }

                    // Ikona zmieniająca się zależnie od wybranego trybu
                    Icon(
                        painter = painterResource(id = if (!isProject) R.drawable.ic_switch_project else R.drawable.ic_switch_task),
                        contentDescription = if (isProject) "Projekt" else "Zadanie",
                        tint = Color(0xFFddb892),
                        modifier = Modifier
                            .size(32.dp) // Rozmiar ikony między przełącznikami
                    )

                    // Ikona dla projektu
                    IconButton(
                        onClick = { isProject = true }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_project),
                            contentDescription = "Projekt",
                            tint = if (isProject) Color(0xFFD5BDAD) else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Pole tekstowe ze stałą szerokością
                    Text(
                        text = if (isProject) "Projekt" else "Zadanie",
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .width(150.dp) // Stała szerokość dla estetyki
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .border(2.dp, Color(0xFFb08968), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        textAlign = TextAlign.Center
                    )
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
                    val isTaskVisible = showTasks && !todo.isProject
                    val isProjectVisible = showProjects && todo.isProject
                    val isLabelVisible = todo.label?.isLabelVisible
                        ?: showWithoutLabel // Uwzględnij widoczność etykiety

                    isLabelVisible && (isTaskVisible || isProjectVisible)
                }




                LazyColumn(
                    content = {
                        itemsIndexed(filteredList ?: emptyList()) { _, item ->
                            AnimatedVisibility(
                                visible = true, // Elementy zawsze widoczne na liście
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it })
                            ) {
                                TodoItem(
                                    item = item,
                                    onClick = {
                                        if (item.isProject) showModal = item else selectedTask =
                                            item
                                    },
                                    onDelete = { todoToDelete = item },
                                    onMarkComplete = {
                                        viewModel.markAsCompleted(
                                            context,
                                            item.id
                                        )
                                    },
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
            viewModel = TodoViewModel(),
            showTasks = showTasks,
            showProjects = showProjects,
            showWithoutLabel = showWithoutLabel,
            availableLabels = viewModel.labels.value ?: emptyList(), // Użycie dostępnych etykiet
            onTasksToggle = { showTasks = !showTasks },
            onProjectsToggle = { showProjects = !showProjects },
            onShowWithoutLabelToggle = { showWithoutLabel = !showWithoutLabel },
            onUserScreenToggle = { isUserScreenVisible = true },
            onCalendarToggle = { isCalendarVisible = true }
        )

    }

    showModal?.let { project ->
        ProjectDetailDialog(
            project = project,
            onClose = {
                showModal = null
                viewModel.updateProject(context, project, false)
                saveProjectState(project)
                Toast.makeText(context, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
            },
            onSave = {
                // Zapisanie zmian projektu w ViewModel
                viewModel.updateProject(context, project, false)

                // Zapisanie stanu projektu w pamięci
                saveProjectState(project)

                showModal = null
            },
        )
    }
    selectedTask?.let { _ ->
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

    UserScreen(
        isVisible = isUserScreenVisible,
        onClose = { isUserScreenVisible = false } // Zamknięcie UserScreen
    )

    CalendarScreen(
        isVisible = isCalendarVisible,
        onClose = { isCalendarVisible = false } // Zamknięcie CalendarScreen
    )


}

@Composable
fun CustomBottomBar(
    viewModel: TodoViewModel,
    showTasks: Boolean,
    showProjects: Boolean,
    showWithoutLabel: Boolean,
    availableLabels: List<Label>,
    onTasksToggle: () -> Unit,
    onProjectsToggle: () -> Unit,
    onShowWithoutLabelToggle: () -> Unit,
    onUserScreenToggle: () -> Unit,
    onCalendarToggle: () -> Unit
) {
    var showEditViewModal by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFF090909))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikona kalendarza po lewej
        IconButton(
            onClick = { onCalendarToggle() }, // Wywołanie przekazanej funkcji
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_calendar_view),
                contentDescription = "Kalendarz",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }


        // Przycisk "Wyświetl" z ikoną
        Button(
            onClick = { showEditViewModal = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_view), // Ikona widoku
                contentDescription = "Wyświetl",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Wyświetl", fontSize = 14.sp, color = Color.White)
        }

        // Ikona użytkownika po prawej stronie
        IconButton(
            onClick = onUserScreenToggle, // Ustaw widoczność com.example.todoapp2.UserScreen na true
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = "Użytkownik",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Wyświetl modal edycji widoku, jeśli aktywne
    if (showEditViewModal) {
        EditViewModal(
            viewModel = viewModel,
            showTasks = showTasks,
            showProjects = showProjects,
            showWithoutLabel = showWithoutLabel,
            availableLabels = availableLabels,
            onClose = { showEditViewModal = false },
            onTasksToggle = onTasksToggle,
            onProjectsToggle = onProjectsToggle,
            onShowWithoutLabelToggle = onShowWithoutLabelToggle
        )
    }
}


@Composable
fun EditViewModal(
    viewModel: TodoViewModel,
    showTasks: Boolean,
    showProjects: Boolean,
    showWithoutLabel: Boolean,
    availableLabels: List<Label>,
    onClose: () -> Unit,
    onTasksToggle: () -> Unit,
    onProjectsToggle: () -> Unit,
    onShowWithoutLabelToggle: () -> Unit
) {
    Dialog(onDismissRequest = { onClose() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp,
            color = Color(0xFF090909),
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color(0xFFb08968), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nagłówek
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edycja widoku", color = Color.White, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkbox dla zadań
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_task),
                        contentDescription = "Zadanie",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                    Text("Zadania", color = Color.White, fontSize = 14.sp)
                    Checkbox(
                        checked = showTasks,
                        onCheckedChange = {
                            onTasksToggle()
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFb08968),
                            uncheckedColor = Color.Gray
                        )
                    )
                }

                // Checkbox dla projektów
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_project),
                        contentDescription = "Projekt",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                    Text("Projekty", color = Color.White, fontSize = 14.sp)
                    Checkbox(
                        checked = showProjects,
                        onCheckedChange = { onProjectsToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFb08968),
                            uncheckedColor = Color.Gray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nagłówek dla etykiet
                Text("Wyświetl kategorie po etykietach", color = Color.White, fontSize = 14.sp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight(0.5f),
                    content = {
                        // Opcja "BEZ ETYKIETY"
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp), // Dostosowano odstępy do pozostałych checkboxów
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                                        .border(2.dp, Color(0xFFb08968), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp), // Dopasowanie paddingu
                                    verticalAlignment = Alignment.CenterVertically // Zapewnienie wyrównania pionowego
                                ) {
                                    Text(
                                        text = "BEZ ETYKIETY",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                                Checkbox(
                                    checked = showWithoutLabel,
                                    onCheckedChange = {
                                        onShowWithoutLabelToggle()
                                        onTasksToggle()
                                        onTasksToggle()
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFb08968),
                                        uncheckedColor = Color.Gray
                                    )
                                )
                            }
                        }

                        items(availableLabels) { label ->
                            var isChecked by remember(label.name) { mutableStateOf(label.isLabelVisible) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .background(Color(label.color), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label.name,
                                        color = if (label.color == Color.Black.toArgb()) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { newChecked ->
                                        isChecked = newChecked //aktualizacja checkboxa
                                        updateLabelVisibility(label, newChecked)
                                        onTasksToggle()
                                        onTasksToggle()
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFb08968),
                                        uncheckedColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Przycisk zamknięcia
                Button(
                    onClick = { onClose() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788)),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Wróć do 2DO listy", color = Color.White)
                }
            }
        }
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
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    var isAnimatingCompletion by remember(item.id) { mutableStateOf(false) }

    val targetBackgroundColor by animateColorAsState(
        targetValue = when {
            isAnimatingCompletion -> Color(0xFF2b9348)
            item.isProject && item.isCompleted -> Color(0xFF74d3ae)
            item.isProject -> Color(0xFFD5BDAD)
            item.isCompleted -> Color(0xFF2b9348)
            else -> Color.Transparent
        }, label = ""
    )

    val targetBorderColor = when {
        item.isProject && item.isCompleted -> Color(0xFF74d3ae)
        item.isProject -> Color(0xFFD5BDAD)
        item.isCompleted -> Color(0xFF2b9348)
        else -> Color(0xFFb08968)
    }

    LaunchedEffect(isAnimatingCompletion) {
        if (isAnimatingCompletion) {
            kotlinx.coroutines.delay(300)
            onMarkComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(targetBackgroundColor)
            .border(2.dp, targetBorderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        // Etykieta w prawym górnym rogu
        item.label?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(it.color), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = it.name,
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    enabled = TodoManager.canMoveUp(item.id),
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
                    enabled = TodoManager.canMoveDown(item.id),
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_down),
                        contentDescription = "Move Down",
                        tint = if (TodoManager.canMoveDown(item.id)) Color.LightGray else Color.Gray
                    )
                }
            }

            // Kolumna z informacjami o zadaniu
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm aa, dd/MM", Locale.ENGLISH).format(item.createdAt),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = item.title,
                    fontSize = 20.sp,
                    color = Color.White
                )
                item.deadline?.let {
                    Text(
                        text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(it)}",
                        fontSize = 11.sp,
                        color = Color(0xFFf4f0bb)
                    )
                }

                // Pasek postępu dla projektów
                if (item.isProject) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (item.tasks.isNotEmpty()) {
                                val completedTasks = item.tasks.count { it.isCompleted }
                                completedTasks.toFloat() / item.tasks.size
                            } else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2b9348),
                        trackColor = Color(0xFFb08968),
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(
                    if (item.label != null && item.title.length <= 40) 75.dp else 0.dp
                )
            )

            // Ikona oznaczenia jako ukończone
            IconButton(onClick = { isAnimatingCompletion = true }) {
                Icon(
                    painter = painterResource(
                        id = if (item.isCompleted) R.drawable.ic_uncheck else R.drawable.ic_check
                    ),
                    contentDescription = "Complete",
                    tint = Color.White
                )
            }

            // Ikona usuwania zadania
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
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
    var showLabelPicker by remember { mutableStateOf(false) }


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


                LabelSection(
                    label = task.label,
                    onEditLabel = { showLabelPicker = true },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Edycja tytułu zadania
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł zadania", color = Color.LightGray) },
                    colors = textFieldColors(
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
            if (showLabelPicker) {
                LabelPickerDialog(
                    viewModel = TodoViewModel(),
                    onLabelSelected = { selectedLabel ->
                        task.label = selectedLabel // Zapisz wybraną etykietę
                        showLabelPicker = false
                    },
                    onDismiss = {
                        showLabelPicker = false // Zamknij dialog bez zmian
                    }
                )
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
                            tint = Color(0xFFb08968),
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
) {
    var description by remember { mutableStateOf(project.description ?: "") }
    var isEditingDescription by remember { mutableStateOf(false) }
    var deadline by remember { mutableStateOf(project.deadline) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val oldDeadline = deadline
    val oldNotifications = project.notifications

    val tasks = remember { mutableStateListOf<Todo>().apply { addAll(project.tasks) } }
    val completedTasks = tasks.count(Todo::isCompleted)
    val progress = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size.toFloat()) * 100 else 0f

    var showAddNotificationDialog by remember { mutableStateOf(false) }
    var areNotificationsDisabled by remember { mutableStateOf(project.areNotificationsDisabled) }

    var showLabelPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        project.description = description
        project.deadline = deadline
        project.tasks = tasks.toList().toMutableList()
        project.areNotificationsDisabled = areNotificationsDisabled
        // Aktualizacja powiadomień
        if (deadline == null || areNotificationsDisabled) {
            NotificationScheduler.cancelTaskReminders(context, project.id)
        } else {
            NotificationScheduler.cancelTaskReminders(context, project.id)
            project.notifications.forEach { offset ->
                val triggerTime = deadline!!.time - offset
                if (triggerTime > System.currentTimeMillis()) {
                    NotificationScheduler.scheduleTaskReminder(
                        context,
                        project.id,
                        project.title,
                        deadline!!.time,
                        triggerTime - System.currentTimeMillis(),
                        "project_reminder_${project.id}_${offset}"
                    )
                }
            }
        }

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

                LabelSection(
                    label = project.label,
                    onEditLabel = { showLabelPicker = true },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                if (showLabelPicker) {
                    LabelPickerDialog(
                        viewModel = TodoViewModel(),
                        onLabelSelected = { selectedLabel ->
                            project.label = selectedLabel // Przypisz wybraną etykietę
                            showLabelPicker = false
                        },
                        onDismiss = {
                            showLabelPicker = false // Zamknij dialog bez zmian
                        }
                    )
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
                                            notifications = task.notifications
                                        )
                                        tasks[index] = updatedTask
                                    }
                                    TodoManager.markTaskAsCompleted(project.id, task.id, !task.isCompleted)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFb08968),
                                    uncheckedColor = Color.Gray
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
                LinearProgressIndicator(
                    progress = { progress / 100f },
                )

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
            AddNotificationDialog(
                onDismiss = { showAddNotificationDialog = false },
                initialNotifications = project.notifications,
                oldNotifications = oldNotifications,
                onSaveChanges = { updatedNotifications ->
                    if (updatedNotifications != oldNotifications) {
                        NotificationScheduler.cancelTaskReminders(context, project.id)

                        updatedNotifications.forEach { offset ->
                            val triggerTime = deadline?.time?.minus(offset)
                            if (triggerTime != null && triggerTime > System.currentTimeMillis()) {
                                NotificationScheduler.scheduleTaskReminder(
                                    context,
                                    project.id,
                                    project.title,
                                    deadline!!.time,
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

@Composable
fun LabelSection(
    label: Label?,
    onEditLabel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (label != null) {
            Box(
                modifier = Modifier
                    .background(color = Color(label.color), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label.name,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.clickable { onEditLabel() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Dodaj etykietę",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Dodaj etykietę",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        IconButton(onClick = { onEditLabel() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edytuj etykietę"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelPickerDialog(
    viewModel: TodoViewModel,
    onLabelSelected: (Label?) -> Unit,
    onDismiss: () -> Unit
) {
    var isCreatingLabel by remember { mutableStateOf(false) }
    var newLabelName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFa2d2ff)) } // Domyślny kolor
    val labels = remember { mutableStateListOf<Label>().apply { addAll(viewModel.labels.value ?: emptyList()) } }
    val context = LocalContext.current

    val availableColors = listOf(
        Color(0xFFa2d2ff), Color(0xFFbdb2ff), Color(0xFFffc8dd), Color(0xFF2a9d8f),
        Color(0xFF219ebc), Color(0xFFc7f9cc), Color(0xFFef233c), Color(0xFFffd166),
        Color(0xFF9c6644), Color(0xFFff9100), Color(0xFFa5a5a5), Color(0xFFbb8588)
    )

    if (isCreatingLabel) {
        // Dialog tworzenia etykiety
        Dialog(onDismissRequest = { isCreatingLabel = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
                color = Color(0xFF090909),
                modifier = Modifier
                    .padding(16.dp)
                    .border(2.dp, Color(0xFFb08968), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Utwórz etykietę", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newLabelName,
                        onValueChange = { newLabelName = it.take(24) },
                        label = { Text("Nazwa etykiety", color = Color.Gray) },
                        colors = textFieldColors(containerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableColors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(color, RoundedCornerShape(20.dp))
                                    .border(
                                        2.dp,
                                        if (color == selectedColor) Color.White else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (newLabelName.isNotEmpty() && labels.none { it.name == newLabelName }) {
                                val newLabel = Label(name = newLabelName, color = selectedColor.toArgb())
                                labels.add(newLabel) // Dodanie etykiety do lokalnej listy
                                viewModel.addLabel(newLabelName, selectedColor.toArgb()) // Zapis w ViewModel
                                Toast.makeText(context, "Etykieta dodana", Toast.LENGTH_SHORT).show()
                                isCreatingLabel = false
                            } else {
                                Toast.makeText(context, "Etykieta o tej nazwie już istnieje", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                    ) {
                        Text("Dodaj etykietę", color = Color.White)
                    }

                }
            }
        }
    } else {
        // Dialog wyboru etykiety
        Dialog(onDismissRequest = { onDismiss() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
                color = Color(0xFF090909),
                modifier = Modifier
                    .padding(16.dp)
                    .border(2.dp, Color(0xFFb08968), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wybierz etykietę", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    BoxWithConstraints {
                        val maxHeight = maxHeight * 0.6f // Maksymalnie 50% wysokości ekranu

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(labels) { label ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .clickable { onLabelSelected(label) }
                                        .background(Color(label.color), RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label.name,
                                        color = Color.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { labels.remove(label); viewModel.removeLabel(label) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.delete),
                                            contentDescription = "Usuń etykietę",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { isCreatingLabel = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                    ) {
                        Text("Utwórz etykietę", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { onLabelSelected(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe63946)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Text("Usuń etykietę", color = Color.White)
                    }
                }
            }
        }
    }
}