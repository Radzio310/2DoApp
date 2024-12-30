package com.example.todoapp2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier



@Composable
fun ProjectDetailDialog(
    project: Todo,
    onClose: () -> Unit,
    onSave: (Todo) -> Unit
) {
    var description by remember { mutableStateOf(project.description ?: "") }
    var deadline by remember { mutableStateOf(project.deadline) }
    var tasks by remember { mutableStateOf(project.tasks ?: mutableListOf()) }

    val completedTasks = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) (completedTasks.toFloat() / tasks.size.toFloat()) * 100 else 0f

    Dialog(onDismissRequest = onClose) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Projekt: ${project.title}", fontSize = 20.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis projektu") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { project.description = description }) {
                        Icon(painter = painterResource(id = R.drawable.ic_edit), contentDescription = "Edytuj")
                    }
                }

                Text(text = "Postęp: ${String.format("%.2f", progress)}%", fontSize = 16.sp)
                LinearProgressIndicator(progress = progress / 100f)

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Zadania:", fontSize = 16.sp)
                LazyColumn {
                    items(tasks) { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { task.isCompleted = !task.isCompleted }
                            )
                            Text(
                                text = task.title,
                                modifier = Modifier.weight(1f),
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { /* Usuwanie zadania */ }) {
                                Icon(painter = painterResource(id = R.drawable.delete), contentDescription = "Usuń")
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Dodaj zadanie */ },
                    placeholder = { Text("Dodaj zadanie") }
                )
                IconButton(onClick = { /* Dodaj nowe zadanie */ }) {
                    Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = "Dodaj zadanie")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Deadline: ${deadline?.let { SimpleDateFormat("dd/MM/yyyy").format(it) } ?: "Brak"}",
                        fontSize = 14.sp
                    )
                    IconButton(onClick = { /* Otwórz DatePickerDialog */ }) {
                        Icon(painter = painterResource(id = R.drawable.ic_calendar), contentDescription = "Edytuj deadline")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = onClose) {
                        Text("Zamknij")
                    }
                    Button(onClick = {
                        project.description = description
                        project.deadline = deadline
                        project.tasks = tasks
                        onSave(project)
                        onClose()
                    }) {
                        Text("Zapisz")
                    }
                }
            }
        }
    }
}

