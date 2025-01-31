package com.example.todoapp2

import android.content.Context
import androidx.work.*
//import java.text.SimpleDateFormat
import java.util.Calendar
//import java.util.Date
//import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Przypomnienie"
        //val deadline = inputData.getLong("deadline", 0)
        val taskId = inputData.getInt("taskId", 0)
        val offsetMillis = inputData.getLong("offsetMillis", 0)

        val timeLeftText = formatTimeLeft(offsetMillis)

        val message = "Zostało Ci $timeLeftText. Do roboty wariacie! 😁"

        NotificationHelper.sendTaskReminder(applicationContext, "\uD83D\uDD14 $title", message, taskId)
        return Result.success()
    }

    // Funkcja pomocnicza do przekształcenia offsetMillis w czytelną formę
    private fun formatTimeLeft(offsetMillis: Long): String {
        return when {
            offsetMillis >= TimeUnit.DAYS.toMillis(2) -> "${offsetMillis / TimeUnit.DAYS.toMillis(1)} dni"
            offsetMillis >= TimeUnit.DAYS.toMillis(1) -> "1 dzień"
            offsetMillis >= TimeUnit.HOURS.toMillis(5) -> "${offsetMillis / TimeUnit.HOURS.toMillis(1)} godzin"
            offsetMillis >= TimeUnit.HOURS.toMillis(1) -> "${offsetMillis / TimeUnit.HOURS.toMillis(1)} godziny"
            offsetMillis >= TimeUnit.MINUTES.toMillis(2) -> "${offsetMillis / TimeUnit.MINUTES.toMillis(1)} minut"
            else -> "1 minutę"
        }
    }

}

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val taskCount = inputData.getInt("taskCount", 0)
        val projectCount = inputData.getInt("projectCount", 0)

        // Wywołanie powiadomienia
        NotificationHelper.sendDailySummary(applicationContext, taskCount, projectCount)
        return Result.success()
    }
}


object NotificationScheduler {
    fun scheduleTaskReminder(
        context: Context,
        taskId: Int,
        title: String,
        deadline: Long,
        offsetMillis: Long,
        tag: String
    ) {
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(offsetMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "title" to title,
                    "deadline" to deadline,
                    "taskId" to taskId,
                    "offsetMillis" to offsetMillis // Przekazujemy czas przed deadline
                )
            )
            .addTag(tag) // Dodanie unikalnego tagu
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }


    fun scheduleDailySummary(context: Context) {
        val incompleteTasksCount = TodoManager.getIncompleteTasksCount()
        val incompleteProjectsCount = TodoManager.getIncompleteProjectsCount()

        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "taskCount" to incompleteTasksCount,
                    "projectCount" to incompleteProjectsCount
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }


    private fun calculateInitialDelay(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (now.after(this)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    fun cancelTaskReminders(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_reminder_$taskId")
    }

}
