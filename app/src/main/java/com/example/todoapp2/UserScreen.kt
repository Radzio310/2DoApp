package com.example.todoapp2

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class UserScreenOption(val label: String, val iconResId: Int) {
    data object Settings : UserScreenOption("Ustawienia", R.drawable.ic_settings)
    data object Statistics : UserScreenOption("Statystyki", R.drawable.ic_stats)
    data object Goals : UserScreenOption("Twoje cele", R.drawable.ic_goals)
    data object Notes : UserScreenOption("Moduł notatek", R.drawable.ic_notes)

    companion object {
        val allOptions = listOf(Settings, Statistics, Goals, Notes)
    }
}


@Composable
fun UserScreen(
    isVisible: Boolean,
    onClose: () -> Unit // Wywołanie zamykające ekran
) {
    var offsetY by remember { mutableFloatStateOf(0f) } // Przechowywanie przesunięcia w pionie
    var initialDragY by remember { mutableStateOf<Float?>(null) } // Pozycja początkowa palca
    var currentOption by remember { mutableStateOf<UserScreenOption?>(null) } // Stan aktywnej opcji

    if (currentOption != null) {
        // Jeśli jest aktywna opcja, pokaż odpowiedni widok
        when (currentOption) {
            is UserScreenOption.Statistics -> AnimatedStatisticsScreen(
                onClose = { currentOption = null }
            )
            is UserScreenOption.Settings -> SettingsScreen(onClose = { currentOption = null })
            is UserScreenOption.Goals -> GoalsScreen(onClose = { currentOption = null })
            is UserScreenOption.Notes -> NotesScreen(onClose = { currentOption = null })
            null -> {} // Ten przypadek uwzględnia wartość null, która nie wymaga akcji
        }
    } else {
        // Główne okno com.example.todoapp2.UserScreen
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 600) // Spowolnienie zamykania
            ) + fadeOut(
                animationSpec = tween(durationMillis = 600) // Spowolnienie zanikania
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { offset ->
                                initialDragY = offset.y // Zapamiętaj pozycję początkową palca
                            },
                            onDragEnd = {
                                // Sprawdź różnicę między pozycją początkową i końcową
                                if (initialDragY != null && offsetY > 300) { // Jeśli przesunięcie w dół jest wystarczające
                                    onClose() // Zamknij ekran
                                } else {
                                    offsetY = 0f // Wróć do pozycji początkowej
                                }
                                initialDragY = null // Zresetuj wartość
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount > 0) { // Tylko ruch w dół
                                    offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                                }
                            }
                        )
                    }
                    .clickable(
                        onClick = {}, // Blokowanie kliknięć
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Logo aplikacji
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo aplikacji",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Lista opcji
                    UserScreenOption.allOptions.forEach { option ->
                        UserOptionItem(
                            option = option,
                            onClick = { currentOption = option } // Ustaw aktualną opcję
                        )
                    }
                }

                // Strzałka w prawym dolnym rogu
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_hide),
                        contentDescription = "Wstecz",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserOptionItem(option: UserScreenOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = option.iconResId),
            contentDescription = option.label,
            modifier = Modifier.size(32.dp),
            tint = Color(0xFFb08968)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = option.label,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
fun AnimatedStatisticsScreen(onClose: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(
            initialOffsetX = { it }, // Start from the right edge
            animationSpec = tween(durationMillis = 1000) // Slow down enter animation
        ) + fadeIn(
            animationSpec = tween(durationMillis = 1000) // Slow down fade-in
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it }, // Exit to the right edge
            animationSpec = tween(durationMillis = 1000) // Slow down exit animation
        ) + fadeOut(
            animationSpec = tween(durationMillis = 1000) // Slow down fade-out
        )
    ) {
        StatisticsScreen(onClose = onClose)
    }
}


@Composable
fun StatisticsScreen(onClose: () -> Unit) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    var selectedYear by remember { mutableIntStateOf(currentYear) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }
    var customRange by remember { mutableStateOf<Pair<Date, Date>?>(null) }

    val statsForSelected = remember(selectedYear, selectedMonth) {
        TodoManager.getStatsForMonth(selectedYear, selectedMonth)
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 600)
        ) + fadeOut(animationSpec = tween(durationMillis = 600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0) onClose()
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Nagłówek z logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo aplikacji",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tytuł sekcji
                Text(
                    text = "Statystyki użytkownika",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Wybór roku
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear-- }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_previous),
                            contentDescription = "Poprzedni rok",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "$selectedYear",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    IconButton(onClick = { selectedYear++ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_next),
                            contentDescription = "Następny rok",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wybór miesiąca
                MonthScrollList(selectedMonth, onMonthSelected = { selectedMonth = it })

                Spacer(modifier = Modifier.height(16.dp))

                // Wyświetlanie statystyk
                StatsGraph(stats = statsForSelected, title = "$selectedYear, ${getMonthName(selectedMonth)}")

                Spacer(modifier = Modifier.height(24.dp))

                // Wybór zakresu czasu
                Button(
                    onClick = { showDatePickerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "Wybierz zakres czasu", color = Color.White)
                }

                // Wyświetlanie statystyk dla zakresu
                customRange?.let { (start, end) ->
                    val statsForRange = TodoManager.getStatsForCustomRange(start, end)
                    Spacer(modifier = Modifier.height(16.dp))
                    StatsGraph(
                        stats = statsForRange,
                        title = "${formatDate(start)} - ${formatDate(end)}"
                    )
                }
            }

            // Przyciski w prawym dolnym rogu
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_hide),
                    contentDescription = "Wstecz",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    // Dialog wyboru zakresu dat
    if (showDatePickerDialog) {
        DateRangePickerDialog(
            onDismiss = { showDatePickerDialog = false },
            onConfirm = { startDate, endDate ->
                customRange = Pair(startDate, endDate)
                showDatePickerDialog = false
            }
        )
    }
}


@Composable
fun MonthScrollList(selectedMonth: Int, onMonthSelected: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((1..12).toList()) { month ->
            Button(
                onClick = { onMonthSelected(month) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMonth == month) Color(0xFFb08968) else Color.Gray
                )
            ) {
                Text(text = getMonthName(month))
            }
        }
    }
}

@Composable
fun DateRangePickerDialog(onDismiss: () -> Unit, onConfirm: (Date, Date) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, Color(0xFFb08968), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF121212)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo aplikacji",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text("Wybierz zakres dat", fontSize = 20.sp, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))

                // Wybór daty początkowej
                Button(
                    onClick = {
                        val datePicker = android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                startDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                        .fillMaxWidth(0.85f)
                        .height(36.dp)
                ) {
                    Text(
                        text = startDate?.let { "Od: ${formatDate(it)}" } ?: "Data początkowa",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Wybór daty końcowej
                Button(
                    onClick = {
                        val datePicker = android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                endDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .border(2.dp, Color(0xFFb08968), RoundedCornerShape(25.dp))
                        .fillMaxWidth(0.85f)
                        .height(36.dp)
                ) {
                    Text(
                        text = endDate?.let { "Do: ${formatDate(it)}" } ?: "Data końcowa",
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Potwierdzenie
                Button(
                    onClick = {
                        if (startDate == null || endDate == null) {
                            Toast.makeText(
                                context,
                                "Proszę wybrać obie daty",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (startDate!! > endDate!!) {
                            Toast.makeText(
                                context,
                                "Data końcowa nie może być wcześniejsza niż początkowa",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onConfirm(startDate!!, endDate!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52b788)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Zastosuj", color = Color.White)
                }
            }
        }
    }
}

fun getMonthName(month: Int): String {
    val months = listOf(
        "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec",
        "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień"
    )
    return months[month - 1]
}



@Composable
fun StatsGraph(stats: Map<String, Int>?, title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(text = title, fontSize = 16.sp, color = Color.LightGray, modifier = Modifier.padding(2.dp))
        if (stats == null) {
            Text(
                text = "Brak danych do wyświetlenia.",
                fontSize = 18.sp,
                color = Color.LightGray
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressRow("Zadania", stats["tasksAdded"] ?: 0, stats["tasksCompleted"] ?: 0)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressRow("Projekty", stats["projectsAdded"] ?: 0, stats["projectsCompleted"] ?: 0)
            }

        }
    }
}

@Composable
fun ProgressRow(
    label: String, // Etykieta paska
    total: Int, // Liczba całkowita, np. liczba zadań
    completed: Int, // Liczba ukończonych zadań
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = "$completed / $total",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFb08968)) // Tło paska
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2b9348)) // Pasek postępu
            )
        }
    }
}


fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}



@Composable
fun SettingsScreen(onClose: () -> Unit) {

}

@Composable
fun GoalsScreen(onClose: () -> Unit) {

}

@Composable
fun NotesScreen(onClose: () -> Unit) {

}

