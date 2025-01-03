import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todoapp2.R
import com.example.todoapp2.Todo
import com.example.todoapp2.TodoManager
import com.example.todoapp2.TodoManager.generateUniqueId
import com.example.todoapp2.TodoManager.saveProjectState
import com.example.todoapp2.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoListPage(viewModel: TodoViewModel, context: Context) {
    val todoList by viewModel.todoList.observeAsState()
    var inputText by remember { mutableStateOf("") }
    var selectedDeadline by remember { mutableStateOf<Date?>(null) }
    var hasDeadline by remember { mutableStateOf(false) }
    var isProject by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    var showModal by remember { mutableStateOf<Todo?>(null) } // Przechowuje projekt do wyświetlenia w modalnym oknie

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
                                                viewModel.addTodo(inputText, selectedDeadline, isProject)
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
                                viewModel.addTodo(inputText, null, isProject)
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
                                            viewModel.addTodo(inputText, selectedDeadline, isProject)
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
                            viewModel.addTodo(inputText, null, isProject)
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

        todoList?.let {
            LazyColumn(content = {
                itemsIndexed(it) { _, item ->
                    TodoItem(
                        item = item,
                        onClick = { if (item.isProject) showModal = item },
                        onDelete = { viewModel.deleteTodo(item.id) },
                        onMarkComplete = { viewModel.markAsCompleted(item.id) }
                    )
                }
            })
        } ?: Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Brak zadań - dodaj pierwsze",
            fontSize = 16.sp
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
}


@Composable
fun TodoItem(item: Todo, onClick: () -> Unit, onDelete: () -> Unit, onMarkComplete: () -> Unit) {
    val backgroundColor = when {
        item.isProject && item.isCompleted -> {
            Color(0xFF74d3ae)
        }
        item.isProject -> {
            // Dla projektu zachowujemy poprzednią logikę tła
            Color(0xFFD5BDAD)
        }
        item.isCompleted -> {
            // Dla wykonanych zadań tło jest wypełnione
            Color(0xFF2b9348) // Zielony dla wykonanego zadania
        }
        else -> {
            // Dla niewykonanych zadań tylko ramka, tło przezroczyste
            Color.Transparent
        }
    }

    val borderColor = when {
        item.isProject -> {
            // Dla projektu zachowujemy poprzednią logikę koloru ramki
            Color(0xFFD5BDAD)
        }
        item.isCompleted -> {
            // Ciemnozielony dla wykonanego zadania
            Color(0xFF2b9348)
        }
        else -> {
            // Kolor ramki dla niewykonanego zadania
            Color(0xFFb08968)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor) // Tło ustawione na przezroczyste lub pełne
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)) // Ramka o dynamicznym kolorze
            .padding(16.dp)
            .clickable(onClick = onClick), // Akcja kliknięcia
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        }
        IconButton(onClick = onMarkComplete) {
            Icon(
                painter = painterResource(id = if (item.isCompleted) R.drawable.ic_uncheck else R.drawable.ic_check),
                contentDescription = "Complete",
                tint = Color.White
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Delete",
                tint = Color.White
            )
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
    val tasks = remember { mutableStateListOf<Todo>().apply { addAll(project.tasks ?: emptyList()) } }

    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size.toFloat()) * 100 else 0f

    Dialog(onDismissRequest = {
        // Akcja wykonywana przy zamknięciu dialogu
        project.description = description
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

                Spacer(modifier = Modifier.height(8.dp))



                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Zadania:", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.6f),
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
                                        val updatedTask = task.copy(isCompleted = !task.isCompleted) // Tworzymy kopię z nowym stanem
                                        tasks[index] = updatedTask // Zastępujemy zadanie kopią w liście
                                    }
                                    TodoManager.markTaskAsCompleted(project.id, task.id, !task.isCompleted) // Zapisujemy zaktualizowany stan
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
                        project.tasks = tasks.toList().toMutableList()
                        saveProjectState(project) // Zapisanie stanu projektu
                        onClose() // Zamknięcie dialogu
                    }) {
                        Text("Zapisz i zamknij")
                    }
                }
            }
        }
    }
}

