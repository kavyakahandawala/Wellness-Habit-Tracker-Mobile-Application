package com.wellness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

class HydrationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = PrefsRepository(context)
        if (prefs.isNotificationEnabled()) {
            val soundUri = prefs.getNotificationSound().takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
            val vibrate = prefs.isVibrationEnabled()
            // Show notification
            TestNotification.show(context, soundUri, vibrate)
        }
    }
}
