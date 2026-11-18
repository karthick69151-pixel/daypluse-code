package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ReminderPriority
import com.example.data.model.RoutineSection

@Entity(tableName = "routine_blocks")
data class RoutineBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val section: RoutineSection,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val dueMinuteOfDay: Int,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_logs")
data class QuickLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_state")
data class DailyStateEntity(
    @PrimaryKey val id: Int = 1,
    val scratchpadText: String = "",
    val customFocusText: String = "",
    val autoSyncFocus: Boolean = true,
    val waterGlasses: Int = 3,
    val waterGoal: Int = 8,
    val studyMinutes: Int = 90,
    val studyGoalMinutes: Int = 240,
    val darkModeOverride: Boolean? = null,
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
