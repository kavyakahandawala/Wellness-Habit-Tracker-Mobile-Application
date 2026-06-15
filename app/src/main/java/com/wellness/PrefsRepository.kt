package com.wellness

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wellness.model.Habit
import com.wellness.model.Mood
import java.text.SimpleDateFormat
import java.util.*

class PrefsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // -------------------- Habits --------------------
    private fun habitsKey(): String {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return "habits_${today.format(System.currentTimeMillis())}"
    }

    fun saveHabits(habits: List<Habit>?) {
        val json = gson.toJson(habits ?: emptyList<Habit>())
        prefs.edit().putString(habitsKey(), json).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(habitsKey(), null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Habit>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // -------------------- Moods --------------------
    private fun moodsKey(): String = "moods"

    fun saveMoods(moods: List<Mood>?) {
        val json = gson.toJson(moods ?: emptyList<Mood>())
        prefs.edit().putString(moodsKey(), json).apply()
    }

    fun loadMoods(): MutableList<Mood> {
        val json = prefs.getString(moodsKey(), null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Mood>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // -------------------- Settings --------------------
    fun getWaterReminderInterval(): Int = prefs.getInt("water_interval", 60).coerceAtLeast(15)
    fun setWaterReminderInterval(minutes: Int) {
        prefs.edit().putInt("water_interval", minutes.coerceAtLeast(15)).apply()
    }

    fun isNotificationEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun isVibrationEnabled(): Boolean = prefs.getBoolean("vibration_enabled", true)
    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun getNotificationSound(): String = prefs.getString("notification_sound", "") ?: ""
    fun setNotificationSound(uri: String?) {
        prefs.edit().putString("notification_sound", uri ?: "").apply()
    }

    // -------------------- Daily Reminder Time --------------------
    fun setDailyReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt("reminder_hour", hour.coerceIn(0, 23))
            .putInt("reminder_minute", minute.coerceIn(0, 59))
            .apply()
    }

    fun getDailyReminderHour(): Int = prefs.getInt("reminder_hour", 9).coerceIn(0, 23)
    fun getDailyReminderMinute(): Int = prefs.getInt("reminder_minute", 0).coerceIn(0, 59)

    // -------------------- Last Hydration Time --------------------
    private val PREFS_LAST_HYDRATION = "last_hydration_time"

    fun getLastHydrationTime(): Long {
        return prefs.getLong(PREFS_LAST_HYDRATION, 0L)
    }

    fun setLastHydrationTime(time: Long) {
        prefs.edit().putLong(PREFS_LAST_HYDRATION, time).apply()
    }
}
