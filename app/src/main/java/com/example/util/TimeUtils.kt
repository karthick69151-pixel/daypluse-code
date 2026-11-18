package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ClockSnapshot(
    val epochMillis: Long,
    val hour24: Int,
    val minute: Int,
    val second: Int,
    val formattedClock: String,
    val formattedDate: String,
    val dateKey: String
) {
    val minuteOfDay: Int get() = hour24 * 60 + minute
}

object TimeUtils {
    fun currentSnapshot(): ClockSnapshot {
        val now = Calendar.getInstance()
        val millis = now.timeInMillis
        val date = Date(millis)
        val clockFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return ClockSnapshot(
            epochMillis = millis,
            hour24 = now.get(Calendar.HOUR_OF_DAY),
            minute = now.get(Calendar.MINUTE),
            second = now.get(Calendar.SECOND),
            formattedClock = clockFormat.format(date),
            formattedDate = dateFormat.format(date),
            dateKey = keyFormat.format(date)
        )
    }

    fun formatTimestamp(epochMillis: Long): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(epochMillis))
    }

    fun formatMinuteOfDay(minuteOfDay: Int): String {
        val normalized = ((minuteOfDay % 1440) + 1440) % 1440
        val hour24 = normalized / 60
        val minute = normalized % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
    }

    fun formatDurationSeconds(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val hours = safe / 3600
        val minutes = (safe % 3600) / 60
        val seconds = safe % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    fun isMinuteWithinBlock(
        currentMinute: Int,
        startMinute: Int,
        endMinute: Int
    ): Boolean {
        if (startMinute == endMinute) return currentMinute == startMinute
        return if (startMinute < endMinute) {
            currentMinute in startMinute until endMinute
        } else {
            currentMinute >= startMinute || currentMinute < endMinute
        }
    }
}
