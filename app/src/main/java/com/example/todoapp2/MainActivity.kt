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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Uprawnienia do powiadomień zostały przyznane.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Uprawnienia do powiadomień są wymagane do poprawnego działania aplikacji.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sprawdź i poproś o uprawnienia
        checkAndRequestNotificationPermission()

        // Inicjalizacja zarządzania danymi (np. lokalna baza danych lub inne źródło danych)
        TodoManager.init(this)
        createNotificationChannels(this)

        // Uruchom codzienne powiadomienia
        NotificationScheduler.scheduleDailySummary(this)


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

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Uprawnienie przyznane, nic nie rób
                }
                else -> {
                    // Poproś o uprawnienie
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }


    private fun createNotificationChannels(context: Context) {
        val channels = listOf(
            NotificationChannel(
                "task_reminders",
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming tasks and projects"
            },
            NotificationChannel(
                "daily_summary",
                "Daily Summary",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Summary of tasks and projects for the day"
            }
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannels(channels)
    }

}

