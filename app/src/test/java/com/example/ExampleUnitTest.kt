package com.example

import com.example.data.model.RoutineSection
import com.example.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun formatMinuteOfDay_formatsMorningAfternoonMidnightCorrectly() {
        assertEquals("12:00 AM", TimeUtils.formatMinuteOfDay(0))
        assertEquals("06:30 AM", TimeUtils.formatMinuteOfDay(6 * 60 + 30))
        assertEquals("12:00 PM", TimeUtils.formatMinuteOfDay(12 * 60))
        assertEquals("05:45 PM", TimeUtils.formatMinuteOfDay(17 * 60 + 45))
    }

    @Test
    fun isMinuteWithinBlock_handlesNormalAndMidnightWrapRanges() {
        assertTrue(TimeUtils.isMinuteWithinBlock(currentMinute = 9 * 60, startMinute = 8 * 60, endMinute = 10 * 60))
        assertFalse(TimeUtils.isMinuteWithinBlock(currentMinute = 10 * 60, startMinute = 8 * 60, endMinute = 10 * 60))
        // Overnight block 11:30 PM - 12:30 AM
        assertTrue(TimeUtils.isMinuteWithinBlock(currentMinute = 23 * 60 + 45, startMinute = 23 * 60 + 30, endMinute = 30))
        assertTrue(TimeUtils.isMinuteWithinBlock(currentMinute = 15, startMinute = 23 * 60 + 30, endMinute = 30))
        assertFalse(TimeUtils.isMinuteWithinBlock(currentMinute = 30, startMinute = 23 * 60 + 30, endMinute = 30))
    }

    @Test
    fun routineSection_mapsHoursAccurately() {
        assertEquals(RoutineSection.MORNING, RoutineSection.fromStartMinute(6 * 60 + 30))
        assertEquals(RoutineSection.AFTERNOON, RoutineSection.fromStartMinute(13 * 60))
        assertEquals(RoutineSection.EVENING, RoutineSection.fromStartMinute(19 * 60))
    }

    @Test
    fun formatDurationSeconds_formatsMinutesAndHours() {
        assertEquals("25:00", TimeUtils.formatDurationSeconds(25 * 60))
        assertEquals("01:05:09", TimeUtils.formatDurationSeconds(3600 + 5 * 60 + 9))
    }
}
