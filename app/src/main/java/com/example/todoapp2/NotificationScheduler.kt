package com.example.todoapp2

import android.content.Context
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Przypomnienie"
        val deadline = inputData.getString("deadline") ?: ""
        val taskId = inputData.getInt("taskId", 0)
        NotificationHelper.sendTaskReminder(applicationContext, "Przypomnienie o zadaniu ($deadline)", "$title", taskId)
        return Result.success()
    }
}

class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val taskCount = inputData.getInt("taskCount", 0)
        val projectCount = inputData.getInt("projectCount", 0)
        NotificationHelper.sendDailySummary(applicationContext, taskCount, projectCount)
        return Result.success()
    }
}

object NotificationScheduler {
    fun scheduleTaskReminder(context: Context, taskId: Int, title: String, deadline: Long, offsetMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(offsetMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "title" to title,
                "deadline" to SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(deadline)),
                "taskId" to taskId
            ))
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun scheduleDailySummary(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_summary",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            //set(Calendar.HOUR_OF_DAY, 8)
            //set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            if (now.after(this)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

}
