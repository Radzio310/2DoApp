package com.example.todoapp2

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    isVisible: Boolean,
    onClose: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var initialDragY by remember { mutableStateOf<Float?>(null) }
    var viewType by remember { mutableStateOf(CalendarViewType.Day) }
    val currentDate = remember { mutableStateOf(Calendar.getInstance().time) }
    val context = LocalContext.current
    val isCalendarVisible = remember { mutableStateOf(isVisible) }
    var oneSlide = 0

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(600)) + fadeOut(animationSpec = tween(600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { initialDragY = it.y },
                        onDragEnd = {
                            if (initialDragY != null && offsetY > 300) {
                                onClose()
                            } else {
                                offsetY = 0f
                            }
                            initialDragY = null
                        },
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0) {
                                offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {oneSlide = 0},
                        onHorizontalDrag = { _, dragAmount ->
                            // Obsługujemy przesunięcie /8*02palcem, maksymalnie o 1 dzień/tydzień/miesiąc
                            if (dragAmount > 50 && oneSlide == 0) {
                                navigateToPrevious(viewType, currentDate)
                                oneSlide += 1
                            } else if (dragAmount < -50 && oneSlide == 0) {
                                navigateToNext(viewType, currentDate)
                                oneSlide += 1
                            }
                        },
                        onDragEnd = {
                            // Nic nie musimy robić na zakończenie gestu, przesunięcie obsłużono w onHorizontalDrag
                        }
                    )
                }


        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo aplikacji",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Unspecified
                    )
                    Text(
                        text = "2 DO - Kalendarz",
                        fontSize = 18.sp,
                        color = Color(0xFFc38e70),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { Toast.makeText(context, "Integracja z Kalendarzem Google możliwa już niedługo", Toast.LENGTH_SHORT).show() }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_calendar),
                            contentDescription = "Google Calendar",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CalendarViewType.values().forEach { type ->
                        Button(
                            onClick = { viewType = type },
                            colors = ButtonDefaults.buttonColors(containerColor = if (viewType == type) Color(0xFF52b788) else Color.Transparent)
                        ) {
                            Text(text = type.displayName, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navigateToPrevious(viewType, currentDate) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_previous),
                            contentDescription = "Poprzednia data",
                            tint = Color.White
                        )
                    }
                    Text(text = getDateRange(viewType, currentDate.value), fontSize = 16.sp, color = Color(0xFFe6ccb2))
                    IconButton(onClick = { navigateToNext(viewType, currentDate) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_next),
                            contentDescription = "Następna data",
                            tint = Color.White
                        )
                    }
                }
                when (viewType) {
                    CalendarViewType.Day -> DayView(TodoViewModel(), date = currentDate.value, isCalendarVisible)
                    CalendarViewType.Week -> WeekView(TodoViewModel(), date = currentDate.value) { selectedDate -> viewType = CalendarViewType.Day; currentDate.value = selectedDate }
                    CalendarViewType.Month -> MonthView(TodoViewModel(), date = currentDate.value) { selectedDate -> viewType = CalendarViewType.Day; currentDate.value = selectedDate }
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(64.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_hide),
                    contentDescription = "Zamknij kalendarz",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Button(
                onClick = {
                    currentDate.value = Calendar.getInstance().time
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(2.dp, Color(0xFFc38e70)), // Brązowa ramka
                shape = RoundedCornerShape(16.dp), // Zaokrąglone rogi
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Wróć do dzisiaj",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp) // Odsunięcie tekstu od ikony
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_today),
                    contentDescription = "Przejdź do dzisiaj",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}




@Composable
fun DayView(viewModel: TodoViewModel, date: Date, isCalendarVisible: MutableState<Boolean>) {
    // Pobranie zadań na wybrany dzień
    val todos = viewModel.todoList.value?.filter { todo ->
        todo.deadline?.let { deadline -> isSameDay(deadline, date) } == true
    } ?: emptyList()

    val context = LocalContext.current
    var showModal by remember { mutableStateOf<Todo?>(null) } // Przechowuje zadanie/projekt do wyświetlenia w modalnym oknie
    var todoToDelete by remember { mutableStateOf<Todo?>(null) } // Przechowuje zadanie/projekt do usunięcia

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (todos.isEmpty()) {
                // Wyświetl komunikat, jeśli brak zadań
                item {
                    Text(
                        text = "Brak zadań na ten dzień",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Wyświetlanie listy zadań
                items(todos) { todo ->
                    TodoItem(
                        item = todo,
                        onClick = {
                            showModal = todo // Otwórz modalne okno szczegółów zadania/projektu
                        },
                        onDelete = {
                            todoToDelete = todo // Otwórz modal potwierdzenia usunięcia
                        },
                        onMarkComplete = {
                            viewModel.markAsCompleted(context, todo.id) // Oznacz jako wykonane
                            viewModel.getAllTodo()
                            isCalendarVisible.value = !isCalendarVisible.value
                            isCalendarVisible.value = !isCalendarVisible.value
                        },
                        onMoveDown = {
                            Toast.makeText(
                                context,
                                "Przesuwanie niemożliwe w widoku kalendarza",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onMoveUp = {
                            Toast.makeText(
                                context,
                                "Przesuwanie niemożliwe w widoku kalendarza",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    // Wyświetl okno szczegółów zadania/projektu
    showModal?.let { todo ->
        if (todo.isProject) {
            ProjectDetailDialog(
                project = todo,
                onClose = {
                    showModal = null // Zamknij okno szczegółów
                },
                onSave = { updatedProject ->
                    viewModel.updateProject(context, updatedProject, false) // Zapisz zmiany w projekcie
                    showModal = null // Zamknij okno szczegółów
                }
            )
        } else {
            TaskDetailDialog(
                task = todo,
                onClose = { showModal = null }, // Zamknij okno szczegółów
                onSave = { updatedTask ->
                    viewModel.updateTodo(context, updatedTask, updatedTask.deadline) // Zapisz zmiany w zadaniu
                    showModal = null // Zamknij okno szczegółów
                }
            )
        }
    }

    // Wyświetl modal potwierdzenia usunięcia
    todoToDelete?.let { todo ->
        CustomStyledAlertDialog(
            title = "Potwierdzenie usunięcia",
            message = "Czy na pewno chcesz usunąć \"${todo.title}\"?",
            onConfirm = {
                viewModel.deleteTodo(context, todo.id) // Usuń zadanie/projekt
                todoToDelete = null // Zamknij dialog
                viewModel.getAllTodo()
                isCalendarVisible.value = !isCalendarVisible.value
                isCalendarVisible.value = !isCalendarVisible.value
            },
            onDismiss = {
                todoToDelete = null // Zamknij dialog bez usuwania
            }
        )
    }
}



@Composable
fun WeekView(viewModel: TodoViewModel, date: Date, onDaySelected: (Date) -> Unit) {
    val startOfWeek = getStartOfWeek(date)
    val daysOfWeek = (0..6).map { Date(startOfWeek.time + it * 24 * 60 * 60 * 1000) }
    val currentDay = Calendar.getInstance().time // Pobranie dzisiejszej daty

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp)) {
        Row(modifier = Modifier.weight(1f)) {
            daysOfWeek.forEach { day ->
                val todos = viewModel.todoList.value?.filter { todo ->
                    todo.deadline?.let { deadline -> isSameDay(deadline, day) } == true
                } ?: emptyList()

                val isToday = isSameDay(day, currentDay)

                Column(
                    modifier = Modifier
                        .weight(1f) // Każdy dzień ma równą szerokość
                        .clickable { onDaySelected(day) }
                        .background(
                            if (isToday) Color(0xFF52B788) else Color(0xFF1C1C1C), // Zielone tło dla dzisiejszego dnia
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = if (isToday) 2.dp else 0.dp,
                            color = if (isToday) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 2.dp, vertical = 1.dp), // Minimalny padding
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Wyświetl dzień tygodnia (np. "Pon")
                    Text(
                        text = SimpleDateFormat("EEE", Locale.getDefault()).format(day),
                        color = if (isToday) Color.Black else Color.White, // Czarny dla dzisiejszego dnia
                        fontSize = 12.sp
                    )
                    // Wyświetl numer dnia (np. "12")
                    Text(
                        text = SimpleDateFormat("dd", Locale.getDefault()).format(day),
                        color = if (isToday) Color.Black else Color.White,
                        fontSize = 12.sp
                    )

                    todos.take(7).forEach { todo ->
                        val (labelColor, borderColor) = if (todo.label == null) {
                            if (todo.isProject) 0x00000000 to 0xFFD5BDAD.toInt() // Projekty bez etykiety
                            else 0x00000000 to 0xFFb08968.toInt() // Zadania bez etykiety
                        } else {
                            todo.label!!.color to todo.label!!.color // Zadania/projekty z etykietą
                        }
                        val textColor = getContrastingTextColor(labelColor)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp) // Minimalny padding między zadaniami
                                .background(
                                    color = Color(labelColor),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(borderColor),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(
                                    horizontal = 2.dp,
                                    vertical = 1.dp
                                ) // Minimalny padding wewnętrzny
                        ) {
                            Text(
                                text = todo.title,
                                color = Color(textColor),
                                fontSize = 10.sp, // Zmniejszony rozmiar tekstu
                                maxLines = 3, // Pozwól na maksymalnie 3 linie
                                softWrap = true, // Włącz zawijanie tekstu
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun MonthView(viewModel: TodoViewModel, date: Date, onDaySelected: (Date) -> Unit) {
    val daysInMonth = getDaysInMonth(date)
    val startOfMonth = Calendar.getInstance().apply {
        time = daysInMonth.first()
    }
    val firstDayOfWeek = startOfMonth.get(Calendar.DAY_OF_WEEK)
    val daysBeforeStart = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

    val daysGrid = buildList {
        repeat(daysBeforeStart) { add(null) }
        addAll(daysInMonth)
    }

    val daysOfWeek = listOf("pon", "wt", "śr", "czw", "pt", "sob", "niedz") // Dni tygodnia
    val currentDay = Calendar.getInstance().time // Bieżący dzień

    Column(modifier = Modifier.fillMaxSize()) {
        // Wiersz z nazwami dni tygodnia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Cienka linia oddzielająca dni tygodnia od kalendarza
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        // Siatka dni miesiąca
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f)
            ) {
                items(daysGrid) { day ->
                    if (day == null) {
                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .background(Color.Transparent)
                                .size(40.dp)
                        )
                    } else {
                        val todos = viewModel.todoList.value?.filter { todo ->
                            todo.deadline?.let { deadline -> isSameDay(deadline, day) } == true
                        } ?: emptyList()

                        val isToday = isSameDay(day, currentDay)

                        Column(
                            modifier = Modifier
                                .padding(1.dp)
                                .clickable { onDaySelected(day) }
                                .background(
                                    if (isToday) Color(0xFF52B788) else Color(0xFF1C1C1C),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    width = if (isToday) 2.dp else 0.dp,
                                    color = if (isToday) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 2.dp, vertical = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = SimpleDateFormat("dd", Locale.getDefault()).format(day),
                                color = if (isToday) Color.Black else Color.White,
                                fontSize = 12.sp
                            )

                            todos.take(3).forEach { todo ->
                                val (labelColor, borderColor) = if (todo.label == null) {
                                    if (todo.isProject) 0x00000000 to 0xFFD5BDAD.toInt()
                                    else 0x00000000 to 0xFFb08968.toInt()
                                } else {
                                    todo.label!!.color to todo.label!!.color
                                }
                                val textColor = getContrastingTextColor(labelColor)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp)
                                        .background(
                                            color = Color(labelColor),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(borderColor),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = todo.title,
                                        color = Color(textColor),
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        softWrap = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun getContrastingTextColor(backgroundColor: Int): Int {
    val red = (backgroundColor shr 16) and 0xFF
    val green = (backgroundColor shr 8) and 0xFF
    val blue = backgroundColor and 0xFF
    val brightness = (red * 299 + green * 587 + blue * 114) / 1000 // Wzór na jasność
    return if (brightness > 128) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() // Czarny lub biały
}


fun getDateRange(viewType: CalendarViewType, date: Date): String {
    return when (viewType) {
        CalendarViewType.Day -> SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(date)
        CalendarViewType.Week -> {
            val start = getStartOfWeek(date)
            val end = Date(start.time + 6 * 24 * 60 * 60 * 1000)
            "${SimpleDateFormat("dd MMM", Locale.getDefault()).format(start)} - ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(end)}"
        }
        CalendarViewType.Month -> {
            val formattedMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
            val correctedMonth = when (formattedMonth.split(" ")[0]) {
                "stycznia" -> "Styczeń"
                "lutego" -> "Luty"
                "marca" -> "Marzec"
                "kwietnia" -> "Kwiecień"
                "maja" -> "Maj"
                "czerwca" -> "Czerwiec"
                "lipca" -> "Lipiec"
                "sierpnia" -> "Sierpień"
                "września" -> "Wrzesień"
                "października" -> "Październik"
                "listopada" -> "Listopad"
                "grudnia" -> "Grudzień"
                else -> formattedMonth.split(" ")[0].replaceFirstChar { it.uppercase() }
            }
            "$correctedMonth ${formattedMonth.split(" ")[1]}"
        }
    }
}




// Utility functions
fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun navigateToPrevious(viewType: CalendarViewType, date: MutableState<Date>) {
    val cal = Calendar.getInstance().apply { time = date.value }
    when (viewType) {
        CalendarViewType.Day -> cal.add(Calendar.DAY_OF_YEAR, -1)
        CalendarViewType.Week -> cal.add(Calendar.WEEK_OF_YEAR, -1)
        CalendarViewType.Month -> cal.add(Calendar.MONTH, -1)
    }
    date.value = cal.time
}

fun navigateToNext(viewType: CalendarViewType, date: MutableState<Date>) {
    val cal = Calendar.getInstance().apply { time = date.value }
    when (viewType) {
        CalendarViewType.Day -> cal.add(Calendar.DAY_OF_YEAR, 1)
        CalendarViewType.Week -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        CalendarViewType.Month -> cal.add(Calendar.MONTH, 1)
    }
    date.value = cal.time
}

fun getStartOfWeek(date: Date): Date {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    return cal.time
}

fun getDaysInMonth(date: Date): List<Date> {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val days = mutableListOf<Date>()
    val month = cal.get(Calendar.MONTH)
    while (cal.get(Calendar.MONTH) == month) {
        days.add(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

enum class CalendarViewType(val displayName: String) {
    Day("Dzienny"),
    Week("Tygodniowy"),
    Month("Miesięczny")
}