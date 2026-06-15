package com.wellness

import android.content.Context
import android.net.Uri
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class HydrationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prefs = PrefsRepository(applicationContext)
        if (!prefs.isNotificationEnabled()) return Result.success()

        val now = System.currentTimeMillis()
        val lastRun = prefs.getLastHydrationTime()
        val intervalMillis = TimeUnit.MINUTES.toMillis(prefs.getWaterReminderInterval().toLong())

        // Skip if interval hasn't passed yet
        if (now - lastRun < intervalMillis) return Result.success()

        val soundUri = prefs.getNotificationSound().takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
        val vibrate = prefs.isVibrationEnabled()
        TestNotification.show(applicationContext, soundUri, vibrate)

        prefs.setLastHydrationTime(now)
        return Result.success()
    }
}
