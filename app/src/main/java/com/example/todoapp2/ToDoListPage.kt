import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
    var hasDeadline by remember { mutableStateOf(false) } // stan checkboxa
    var isProject by remember { mutableStateOf(false) } // stan przełącznika dla projektu
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        // Sekcja dodawania nowego zadania
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            // Pole do wpisywania zadania zajmujące 90% szerokości
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Dodaj zadanie...") },
                modifier = Modifier
                    .weight(0.9f) // Pole zajmuje 90% szerokości
                    .padding(end = 8.dp) // Odstęp od przycisku
            )

            // Przyciski dodawania zadania zajmujące 10% szerokości
            IconButton(
                onClick = {
                    if (inputText.isNotEmpty()) {
                        if (hasDeadline) {
                            // Pokazuje dialog do wyboru daty
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(Calendar.YEAR, year)
                                    calendar.set(Calendar.MONTH, month)
                                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    // Po wyborze daty pokazuje TimePicker
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                                            calendar.set(Calendar.MINUTE, minute)
                                            selectedDeadline = calendar.time
                                            // Dodanie zadania po ustawieniu deadline
                                            viewModel.addTodo(inputText, selectedDeadline, isProject)
                                            inputText = ""  // Czyszczenie pola tekstowego
                                            selectedDeadline = null  // Resetowanie deadline
                                            isProject = false // Resetowanie typu
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
                            // Dodanie zadania bez deadline
                            viewModel.addTodo(inputText, null, isProject)
                            inputText = ""  // Czyszczenie pola tekstowego
                            isProject = false // Resetowanie typu
                        }
                    } else {
                        Toast.makeText(context, "Wpisz treść zadania", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(0.1f) // Przycisk zajmuje 10% szerokości
                    .align(Alignment.CenterVertically) // Wyśrodkowanie przycisku
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Task",
                    tint = Color.White
                )
            }
        }

        // Checkbox do wyboru deadline oraz Switch do wyboru typu zadania/projektu w jednej linii
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox do wyboru deadline
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Deadline", fontSize = 12.sp)
                Checkbox(
                    checked = hasDeadline,
                    onCheckedChange = { hasDeadline = it }
                )
            }

            // Switch do wyboru typu zadania/projektu
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Zadanie", fontSize = 12.sp)
                IconToggleButton(
                    checked = !isProject,
                    onCheckedChange = { isProject = !it },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_task), // Ikona zadania (inline)
                        contentDescription = "Task",
                        tint = if (!isProject) Color(0xFFBDE0FE) else Color.Gray // Kolor zadania
                    )
                }

                Text(text = "Projekt", fontSize = 12.sp)
                IconToggleButton(
                    checked = isProject,
                    onCheckedChange = { isProject = it },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_project), // Ikona projektu (outline)
                        contentDescription = "Project",
                        tint = if (isProject) Color(0xFFD5BDAD) else Color.Gray // Kolor projektu
                    )
                }
            }
        }

        // Lista zadań
        todoList?.let {
            LazyColumn(content = {
                itemsIndexed(it) { _, item ->
                    TodoItem(
                        item = item,
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
}


@Composable
fun TodoItem(item: Todo, onDelete: () -> Unit, onMarkComplete: () -> Unit) {
    // Zmieniamy kolor tła w zależności od typu zadania (projekt)
    val backgroundColor = when {
        item.isProject -> Color(0xFFD5BDAD) // Kolor #d5bdaf dla Projektu
        item.isCompleted -> Color(0xFF4CAF50) // Zielony dla wykonanego zadania
        else -> Color(0xFFBDE0FE) // Kolor #bde0fe dla Zadania
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
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
            // Wyświetlenie terminu wykonania, jeśli istnieje
            item.deadline?.let {
                Text(
                    text = "Deadline: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(it)}",
                    fontSize = 12.sp,
                    color = Color.Yellow
                )
            }
        }
        // Przycisk oznaczenia jako wykonane (lub niewykonane)
        IconButton(onClick = onMarkComplete) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Complete",
                tint = Color.White
            )
        }
        // Przycisk usunięcia
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

