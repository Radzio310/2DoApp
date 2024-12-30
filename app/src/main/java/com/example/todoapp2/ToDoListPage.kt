import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp2.R
import com.example.todoapp2.Todo
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
                    .padding(end = 8.dp)
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
                        tint = if (!isProject) Color(0xFFBDE0FE) else Color.Gray
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
        ProjectModal(project = project, onDismiss = { showModal = null })
    }
}

@Composable
fun TodoItem(item: Todo, onClick: () -> Unit, onDelete: () -> Unit, onMarkComplete: () -> Unit) {
    val backgroundColor = when {
        item.isProject && item.isCompleted -> Color(0xFF2E7D32) // Ciemnozielony dla wykonanego projektu
        item.isProject -> Color(0xFFD5BDAD)
        item.isCompleted -> Color(0xFF4CAF50)
        else -> Color(0xFFBDE0FE)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = SimpleDateFormat("HH:mm aa, dd/MM", Locale.ENGLISH).format(item.createdAt),
                fontSize = 10.sp,
                color = Color.LightGray
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
                    color = Color.Yellow
                )
            }
        }
        IconButton(onClick = onMarkComplete) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
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
fun ProjectModal(project: Todo, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = project.title, fontSize = 20.sp)
        },
        text = {
            Column {
                Text(text = "Szczegóły projektu:")
                project.deadline?.let {
                    Text(text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(it)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}
