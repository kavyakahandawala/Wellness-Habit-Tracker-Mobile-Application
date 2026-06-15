package com.wellness.model

data class Mood(
    var emoji: String,
    var description: String,
    var timestamp: Long = System.currentTimeMillis()
)
