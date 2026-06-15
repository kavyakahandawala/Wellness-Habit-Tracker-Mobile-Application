package com.wellness

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    private lateinit var prefs: PrefsRepository
    private lateinit var edtWaterInterval: EditText
    private lateinit var switchNotifications: Switch
    private lateinit var switchVibration: Switch
    private lateinit var btnSelectSound: Button
    private lateinit var btnTestNotification: Button
    private lateinit var btnPickTime: Button
    private lateinit var btnSaveInterval: Button
    private var selectedSoundUri: Uri? = null

    companion object {
        private const val REQUEST_RINGTONE = 101
        private const val REQUEST_NOTIF_PERMISSION = 102
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        prefs = PrefsRepository(requireContext())

        edtWaterInterval = view.findViewById(R.id.edtWaterInterval)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchVibration = view.findViewById(R.id.switchVibration)
        btnSelectSound = view.findViewById(R.id.btnSelectSound)
        btnTestNotification = view.findViewById(R.id.btnTestNotification)
        btnPickTime = view.findViewById(R.id.btnPickTime)
        btnSaveInterval = view.findViewById(R.id.btnSaveInterval)

        loadSavedSettings()
        checkNotificationPermission()

        // --- Sound Picker ---
        btnSelectSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Sound")
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
            startActivityForResult(intent, REQUEST_RINGTONE)
        }

        // --- Test Notification ---
        btnTestNotification.setOnClickListener {
            if (switchNotifications.isChecked) {
                TestNotification.show(requireContext(), selectedSoundUri, switchVibration.isChecked)
            } else {
                Toast.makeText(requireContext(), "Enable notifications first", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Pick Daily Reminder Time ---
        btnPickTime.setOnClickListener { pickDailyReminderTime() }

        // --- Save Interval Button ---
        btnSaveInterval.setOnClickListener {
            val intervalInput = edtWaterInterval.text.toString().toIntOrNull()
            if (intervalInput == null || intervalInput < 15) {
                Toast.makeText(requireContext(), "Please enter an interval of at least 15 minutes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val interval = intervalInput.coerceAtLeast(15)
            prefs.setWaterReminderInterval(interval)
            prefs.setNotificationEnabled(switchNotifications.isChecked)
            prefs.setVibrationEnabled(switchVibration.isChecked)
            selectedSoundUri?.let { prefs.setNotificationSound(it.toString()) }

            // Reset last run time to now to prevent immediate notification
            prefs.setLastHydrationTime(System.currentTimeMillis())

            // Cancel previous hydration reminders
            WorkManager.getInstance(requireContext()).cancelUniqueWork("hydration_reminder")

            // Schedule new reminder only if notifications enabled
            if (switchNotifications.isChecked) {
                scheduleHydrationReminder(interval)
                Toast.makeText(requireContext(), "Interval reminder saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadSavedSettings() {
        edtWaterInterval.setText(prefs.getWaterReminderInterval().toString())
        switchNotifications.isChecked = prefs.isNotificationEnabled()
        switchVibration.isChecked = prefs.isVibrationEnabled()
        val savedSound = prefs.getNotificationSound()
        selectedSoundUri = if (savedSound.isNotEmpty()) Uri.parse(savedSound) else null
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIF_PERMISSION)
            }
        }
    }

    private fun pickDailyReminderTime() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                prefs.setDailyReminderTime(hour, minute)

                // Schedule daily alarm immediately after picking time
                scheduleDailyHydrationReminder(cal)

                Toast.makeText(requireContext(), "Daily reminder set at %02d:%02d".format(hour, minute), Toast.LENGTH_SHORT).show()
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun scheduleHydrationReminder(intervalMinutes: Int) {
        val workRequest = PeriodicWorkRequestBuilder<HydrationWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES).build()
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "hydration_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun scheduleDailyHydrationReminder(cal: Calendar) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationAlarmReceiver::class.java)

        // Safe PendingIntent for all API levels
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (cal.timeInMillis < System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RINGTONE && resultCode == Activity.RESULT_OK) {
            val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedSoundUri = uri
            Toast.makeText(requireContext(), "Sound selected", Toast.LENGTH_SHORT).show()
        }
    }
}
