package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.example.data.model.ReminderPriority
import com.example.data.model.RoutineSection
import kotlinx.coroutines.flow.Flow

class DayPulseConverters {
    @TypeConverter
    fun fromRoutineSection(section: RoutineSection): String = section.name

    @TypeConverter
    fun toRoutineSection(value: String): RoutineSection =
        runCatching { RoutineSection.valueOf(value) }.getOrDefault(RoutineSection.MORNING)

    @TypeConverter
    fun fromReminderPriority(priority: ReminderPriority): String = priority.name

    @TypeConverter
    fun toReminderPriority(value: String): ReminderPriority =
        runCatching { ReminderPriority.valueOf(value) }.getOrDefault(ReminderPriority.MEDIUM)
}

@Dao
interface DayPulseDao {
    // Routine Blocks
    @Query("SELECT * FROM routine_blocks ORDER BY startMinuteOfDay ASC, id ASC")
    fun observeRoutineBlocks(): Flow<List<RoutineBlockEntity>>

    @Query("SELECT COUNT(*) FROM routine_blocks")
    suspend fun getRoutineCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBlock(block: RoutineBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineBlocks(blocks: List<RoutineBlockEntity>)

    @Update
    suspend fun updateRoutineBlock(block: RoutineBlockEntity)

    @Query("DELETE FROM routine_blocks WHERE id = :id")
    suspend fun deleteRoutineBlockById(id: Long)

    @Query("UPDATE routine_blocks SET isCompleted = 0")
    suspend fun resetAllRoutineCompletions()

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueMinuteOfDay ASC, id DESC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT COUNT(*) FROM reminders")
    suspend fun getReminderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    // Quick Logs
    @Query("SELECT * FROM quick_logs ORDER BY createdAt DESC, id DESC")
    fun observeQuickLogs(): Flow<List<QuickLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickLog(log: QuickLogEntity): Long

    @Query("DELETE FROM quick_logs WHERE id = :id")
    suspend fun deleteQuickLogById(id: Long)

    // Daily State
    @Query("SELECT * FROM daily_state WHERE id = 1")
    fun observeDailyState(): Flow<DailyStateEntity?>

    @Query("SELECT * FROM daily_state WHERE id = 1")
    suspend fun getDailyStateOnce(): DailyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyState(state: DailyStateEntity)
}
