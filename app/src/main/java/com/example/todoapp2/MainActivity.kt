package com.example.todoapp2

import TodoListPage
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.example.todoapp2.ui.theme.ToDoApp2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja zarządzania danymi (np. lokalna baza danych lub inne źródło danych)
        TodoManager.init(this)

        // Pobranie ViewModelu dla Todo
        val todoViewModel = ViewModelProvider(this)[TodoViewModel::class.java]

        // Ustawienie interfejsu użytkownika
        setContent {
            ToDoApp2Theme {
                // Powierzchnia kontenera korzystająca z tła z motywu aplikacji
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Główna strona listy zadań
                    TodoListPage(
                        viewModel = todoViewModel,
                        context = this
                    )
                }
            }
        }
    }
}

