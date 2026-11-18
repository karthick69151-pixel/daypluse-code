package com.example.ui

import com.example.data.local.DailyStateEntity
import com.example.data.local.QuickLogEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.RoutineBlockEntity
import com.example.data.model.RoutineSection
import com.example.util.ClockSnapshot
import com.example.util.TimeUtils

enum class MainTab(val label: String) {
    ROUTINE("Routine"),
    REMINDERS("Reminders"),
    NOTES_HABITS("Notes & Habits")
}

enum class ReminderFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    HIGH("High"),
    COMPLETED("Done")
}

enum class FocusTimerMode(val label: String) {
    COUNTDOWN("Countdown"),
    STOPWATCH("Stopwatch")
}

data class FocusTimerState(
    val mode: FocusTimerMode = FocusTimerMode.COUNTDOWN,
    val presetMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false
) {
    val displaySeconds: Int
        get() = if (mode == FocusTimerMode.COUNTDOWN) remainingSeconds else elapsedSeconds
}

data class DayPulseUiState(
    val clock: ClockSnapshot = TimeUtils.currentSnapshot(),
    val routineBlocks: List<RoutineBlockEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val quickLogs: List<QuickLogEntity> = emptyList(),
    val dailyState: DailyStateEntity = DailyStateEntity(),
    val activeBlock: RoutineBlockEntity? = null,
    val focusTimer: FocusTimerState = FocusTimerState(),
    val selectedTab: MainTab = MainTab.ROUTINE,
    val reminderFilter: ReminderFilter = ReminderFilter.ALL,
    val editingRoutineBlock: RoutineBlockEntity? = null,
    val showRoutineDialog: Boolean = false,
    val prefillSection: RoutineSection = RoutineSection.MORNING,
    val editingReminder: ReminderEntity? = null,
    val showReminderDialog: Boolean = false,
    val isLoaded: Boolean = false
) {
    val greeting: String
        get() = when {
            clock.hour24 < 12 -> "Good Morning"
            clock.hour24 < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }

    val totalRoutineCount: Int get() = routineBlocks.size
    val completedRoutineCount: Int get() = routineBlocks.count { it.isCompleted }
    val completionProgress: Float
        get() = if (totalRoutineCount == 0) 0f else completedRoutineCount.toFloat() / totalRoutineCount.toFloat()
    val completionPercentage: Int
        get() = (completionProgress * 100).toInt().coerceIn(0, 100)

    val displayedFocusText: String
        get() = if (dailyState.autoSyncFocus && activeBlock != null) {
            activeBlock.title
        } else {
            dailyState.customFocusText
        }

    val filteredReminders: List<ReminderEntity>
        get() = when (reminderFilter) {
            ReminderFilter.ALL -> reminders
            ReminderFilter.PENDING -> reminders.filter { !it.isCompleted }
            ReminderFilter.HIGH -> reminders.filter {
                !it.isCompleted && it.priority == com.example.data.model.ReminderPriority.HIGH
            }
            ReminderFilter.COMPLETED -> reminders.filter { it.isCompleted }
        }

    val pendingReminderCount: Int get() = reminders.count { !it.isCompleted }
}
