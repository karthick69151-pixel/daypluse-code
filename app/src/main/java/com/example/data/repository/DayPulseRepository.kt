package com.example.data.repository

import com.example.data.local.DailyStateEntity
import com.example.data.local.DayPulseDao
import com.example.data.local.QuickLogEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.RoutineBlockEntity
import com.example.data.model.ReminderPriority
import com.example.data.model.RoutineSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DayPulseRepository(private val dao: DayPulseDao) {

    val routineBlocks: Flow<List<RoutineBlockEntity>> = dao.observeRoutineBlocks()
    val reminders: Flow<List<ReminderEntity>> = dao.observeReminders()
    val quickLogs: Flow<List<QuickLogEntity>> = dao.observeQuickLogs()
    val dailyState: Flow<DailyStateEntity> = dao.observeDailyState().map { it ?: DailyStateEntity() }

    suspend fun ensureInitialData() {
        val existingState = dao.getDailyStateOnce()
        if (existingState != null) return

        dao.upsertDailyState(
            DailyStateEntity(
                id = 1,
                scratchpadText = "Top 3 priorities for today:\n• Ship the main milestone before lunch\n• Stay hydrated between focus blocks\n• Unwind screen-free after 9:30 PM",
                customFocusText = "Deep Work & Planning",
                autoSyncFocus = true,
                waterGlasses = 2,
                waterGoal = 8,
                studyMinutes = 60,
                studyGoalMinutes = 240
            )
        )

        if (dao.getRoutineCount() == 0) {
            dao.insertRoutineBlocks(defaultRoutineBlocks())
        }
        if (dao.getReminderCount() == 0) {
            dao.insertReminders(defaultReminders())
        }
    }

    suspend fun saveRoutineBlock(block: RoutineBlockEntity) {
        if (block.id == 0L) {
            dao.insertRoutineBlock(block)
        } else {
            dao.updateRoutineBlock(block)
        }
    }

    suspend fun deleteRoutineBlock(id: Long) = dao.deleteRoutineBlockById(id)

    suspend fun resetRoutineCompletions() = dao.resetAllRoutineCompletions()

    suspend fun saveReminder(reminder: ReminderEntity) {
        if (reminder.id == 0L) {
            dao.insertReminder(reminder)
        } else {
            dao.updateReminder(reminder)
        }
    }

    suspend fun deleteReminder(id: Long) = dao.deleteReminderById(id)

    suspend fun addQuickLog(content: String) {
        val trimmed = content.trim()
        if (trimmed.isNotEmpty()) {
            dao.insertQuickLog(QuickLogEntity(content = trimmed))
        }
    }

    suspend fun deleteQuickLog(id: Long) = dao.deleteQuickLogById(id)

    suspend fun updateDailyState(transform: (DailyStateEntity) -> DailyStateEntity) {
        val current = dao.getDailyStateOnce() ?: DailyStateEntity()
        val updated = transform(current).copy(lastUpdatedAt = System.currentTimeMillis())
        dao.upsertDailyState(updated)
    }

    private fun defaultRoutineBlocks(): List<RoutineBlockEntity> = listOf(
        RoutineBlockEntity(
            title = "Morning Hydration & Sunlight",
            notes = "500ml water + 10 min stretch",
            section = RoutineSection.MORNING,
            startMinuteOfDay = 6 * 60 + 30,
            endMinuteOfDay = 7 * 60,
            isCompleted = true
        ),
        RoutineBlockEntity(
            title = "Morning Workout & Shower",
            notes = "Mobility + 25m cardio",
            section = RoutineSection.MORNING,
            startMinuteOfDay = 7 * 60,
            endMinuteOfDay = 8 * 60,
            isCompleted = true
        ),
        RoutineBlockEntity(
            title = "Deep Focus Sprint #1",
            notes = "High-leverage project work",
            section = RoutineSection.MORNING,
            startMinuteOfDay = 8 * 60,
            endMinuteOfDay = 10 * 60 + 30,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Inbox, Standup & Quick Tasks",
            notes = "Clear messages & triage priorities",
            section = RoutineSection.MORNING,
            startMinuteOfDay = 10 * 60 + 30,
            endMinuteOfDay = 12 * 60,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Lunch & Midday Walk",
            notes = "Step away from screens",
            section = RoutineSection.AFTERNOON,
            startMinuteOfDay = 12 * 60,
            endMinuteOfDay = 13 * 60,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Study & Skill Building",
            notes = "Reading, notes, or coding practice",
            section = RoutineSection.AFTERNOON,
            startMinuteOfDay = 13 * 60,
            endMinuteOfDay = 15 * 60 + 30,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Wrap-up & Tomorrow Prep",
            notes = "Review checklist & log progress",
            section = RoutineSection.AFTERNOON,
            startMinuteOfDay = 15 * 60 + 30,
            endMinuteOfDay = 17 * 60,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Dinner & Family / Downtime",
            notes = "Cook & recharge",
            section = RoutineSection.EVENING,
            startMinuteOfDay = 18 * 60,
            endMinuteOfDay = 20 * 60,
            isCompleted = false
        ),
        RoutineBlockEntity(
            title = "Evening Reading & Journal",
            notes = "Wind down before sleep",
            section = RoutineSection.EVENING,
            startMinuteOfDay = 20 * 60 + 30,
            endMinuteOfDay = 22 * 60,
            isCompleted = false
        )
    )

    private fun defaultReminders(): List<ReminderEntity> = listOf(
        ReminderEntity(
            title = "Send weekly progress summary",
            priority = ReminderPriority.HIGH,
            dueMinuteOfDay = 11 * 60 + 30,
            isCompleted = false
        ),
        ReminderEntity(
            title = "Refill water bottle & stretch neck",
            priority = ReminderPriority.MEDIUM,
            dueMinuteOfDay = 14 * 60,
            isCompleted = false
        ),
        ReminderEntity(
            title = "Pick up groceries on the way home",
            priority = ReminderPriority.LOW,
            dueMinuteOfDay = 17 * 60 + 45,
            isCompleted = false
        )
    )
}
