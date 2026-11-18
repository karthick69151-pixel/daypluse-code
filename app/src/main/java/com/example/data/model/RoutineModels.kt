package com.example.data.model

enum class RoutineSection(
    val displayTitle: String,
    val timeHint: String
) {
    MORNING("Morning Routine", "05:00 AM – 11:59 AM"),
    AFTERNOON("Afternoon / Work & Study", "12:00 PM – 04:59 PM"),
    EVENING("Evening / Night", "05:00 PM – 11:59 PM");

    companion object {
        fun fromStartMinute(startMinuteOfDay: Int): RoutineSection {
            val normalized = ((startMinuteOfDay % 1440) + 1440) % 1440
            return when {
                normalized < 12 * 60 -> MORNING
                normalized < 17 * 60 -> AFTERNOON
                else -> EVENING
            }
        }
    }
}

enum class ReminderPriority(val label: String) {
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}
