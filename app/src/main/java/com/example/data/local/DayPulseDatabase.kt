package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RoutineBlockEntity::class,
        ReminderEntity::class,
        QuickLogEntity::class,
        DailyStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DayPulseConverters::class)
abstract class DayPulseDatabase : RoomDatabase() {
    abstract fun dayPulseDao(): DayPulseDao

    companion object {
        @Volatile
        private var instance: DayPulseDatabase? = null

        fun getInstance(context: Context): DayPulseDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DayPulseDatabase::class.java,
                    "daypulse_local.db"
                ).build().also { instance = it }
            }
        }
    }
}
