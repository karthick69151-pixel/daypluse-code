package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyStateEntity
import com.example.data.local.DayPulseDatabase
import com.example.data.local.QuickLogEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.RoutineBlockEntity
import com.example.data.model.ReminderPriority
import com.example.data.model.RoutineSection
import com.example.data.repository.DayPulseRepository
import com.example.util.ClockSnapshot
import com.example.util.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DayPulseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DayPulseRepository(
        DayPulseDatabase.getInstance(application).dayPulseDao()
    )

    private val clockFlow = MutableStateFlow(TimeUtils.currentSnapshot())
    private val focusTimerFlow = MutableStateFlow(FocusTimerState())
    private val localControlFlow = MutableStateFlow(LocalUiControls())

    private data class LocalUiControls(
        val selectedTab: MainTab = MainTab.ROUTINE,
        val reminderFilter: ReminderFilter = ReminderFilter.ALL,
        val editingRoutineBlock: RoutineBlockEntity? = null,
        val showRoutineDialog: Boolean = false,
        val prefillSection: RoutineSection = RoutineSection.MORNING,
        val editingReminder: ReminderEntity? = null,
        val showReminderDialog: Boolean = false,
        val isSeeded: Boolean = false
    )

    private data class DbBundle(
        val blocks: List<RoutineBlockEntity>,
        val reminders: List<ReminderEntity>,
        val logs: List<QuickLogEntity>,
        val dailyState: DailyStateEntity
    )

    private val dbBundleFlow = combine(
        repository.routineBlocks,
        repository.reminders,
        repository.quickLogs,
        repository.dailyState
    ) { blocks, reminders, logs, dailyState ->
        DbBundle(blocks, reminders, logs, dailyState)
    }

    val uiState: StateFlow<DayPulseUiState> = combine(
        clockFlow,
        dbBundleFlow,
        focusTimerFlow,
        localControlFlow
    ) { clock, db, timer, controls ->
        val activeBlock = findActiveBlock(db.blocks, clock)
        DayPulseUiState(
            clock = clock,
            routineBlocks = db.blocks,
            reminders = db.reminders,
            quickLogs = db.logs,
            dailyState = db.dailyState,
            activeBlock = activeBlock,
            focusTimer = timer,
            selectedTab = controls.selectedTab,
            reminderFilter = controls.reminderFilter,
            editingRoutineBlock = controls.editingRoutineBlock,
            showRoutineDialog = controls.showRoutineDialog,
            prefillSection = controls.prefillSection,
            editingReminder = controls.editingReminder,
            showReminderDialog = controls.showReminderDialog,
            isLoaded = controls.isSeeded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayPulseUiState()
    )

    private var trackedMinuteAccumulator = 0

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
            localControlFlow.update { it.copy(isSeeded = true) }
        }
        startLiveClockAndTimerLoop()
    }

    private fun startLiveClockAndTimerLoop() {
        viewModelScope.launch {
            while (true) {
                clockFlow.value = TimeUtils.currentSnapshot()
                tickFocusTimerIfRunning()
                delay(1_000L)
            }
        }
    }

    private suspend fun tickFocusTimerIfRunning() {
        val current = focusTimerFlow.value
        if (!current.isRunning) return

        trackedMinuteAccumulator++
        if (trackedMinuteAccumulator >= 60) {
            trackedMinuteAccumulator = 0
            repository.updateDailyState { state ->
                state.copy(studyMinutes = state.studyMinutes + 1)
            }
        }

        if (current.mode == FocusTimerMode.COUNTDOWN) {
            if (current.remainingSeconds > 1) {
                focusTimerFlow.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
            } else {
                focusTimerFlow.value = current.copy(remainingSeconds = 0, isRunning = false)
            }
        } else {
            focusTimerFlow.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
        }
    }

    private fun findActiveBlock(
        blocks: List<RoutineBlockEntity>,
        clock: ClockSnapshot
    ): RoutineBlockEntity? {
        val minute = clock.minuteOfDay
        return blocks.firstOrNull { block ->
            TimeUtils.isMinuteWithinBlock(
                currentMinute = minute,
                startMinute = block.startMinuteOfDay,
                endMinute = block.endMinuteOfDay
            )
        }
    }

    // Navigation & Filters
    fun selectTab(tab: MainTab) {
        localControlFlow.update { it.copy(selectedTab = tab) }
    }

    fun selectReminderFilter(filter: ReminderFilter) {
        localControlFlow.update { it.copy(reminderFilter = filter) }
    }

    // Theme Override
    fun toggleDarkMode(currentlyDark: Boolean) {
        viewModelScope.launch {
            repository.updateDailyState { it.copy(darkModeOverride = !currentlyDark) }
        }
    }

    // Current Focus Banner & Timer
    fun updateCustomFocusText(text: String) {
        viewModelScope.launch {
            repository.updateDailyState {
                it.copy(customFocusText = text, autoSyncFocus = false)
            }
        }
    }

    fun setAutoSyncFocus(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateDailyState { it.copy(autoSyncFocus = enabled) }
        }
    }

    fun focusOnBlock(block: RoutineBlockEntity) {
        viewModelScope.launch {
            repository.updateDailyState {
                it.copy(customFocusText = block.title, autoSyncFocus = false)
            }
            if (!focusTimerFlow.value.isRunning) {
                focusTimerFlow.update { it.copy(isRunning = true) }
            }
        }
    }

    fun toggleFocusTimer() {
        focusTimerFlow.update { state ->
            if (state.mode == FocusTimerMode.COUNTDOWN && state.remainingSeconds == 0) {
                state.copy(remainingSeconds = state.presetMinutes * 60, isRunning = true)
            } else {
                state.copy(isRunning = !state.isRunning)
            }
        }
    }

    fun resetFocusTimer() {
        trackedMinuteAccumulator = 0
        focusTimerFlow.update { state ->
            state.copy(
                remainingSeconds = state.presetMinutes * 60,
                elapsedSeconds = 0,
                isRunning = false
            )
        }
    }

    fun selectTimerPreset(minutes: Int) {
        trackedMinuteAccumulator = 0
        focusTimerFlow.value = FocusTimerState(
            mode = FocusTimerMode.COUNTDOWN,
            presetMinutes = minutes,
            remainingSeconds = minutes * 60,
            elapsedSeconds = 0,
            isRunning = false
        )
    }

    fun selectStopwatchMode() {
        trackedMinuteAccumulator = 0
        focusTimerFlow.value = FocusTimerState(
            mode = FocusTimerMode.STOPWATCH,
            presetMinutes = 25,
            remainingSeconds = 25 * 60,
            elapsedSeconds = 0,
            isRunning = false
        )
    }

    // Routine Block CRUD
    fun openNewRoutineDialog(section: RoutineSection = RoutineSection.fromStartMinute(clockFlow.value.minuteOfDay)) {
        localControlFlow.update {
            it.copy(
                editingRoutineBlock = null,
                prefillSection = section,
                showRoutineDialog = true
            )
        }
    }

    fun openEditRoutineDialog(block: RoutineBlockEntity) {
        localControlFlow.update {
            it.copy(
                editingRoutineBlock = block,
                prefillSection = block.section,
                showRoutineDialog = true
            )
        }
    }

    fun dismissRoutineDialog() {
        localControlFlow.update { it.copy(showRoutineDialog = false, editingRoutineBlock = null) }
    }

    fun saveRoutineBlock(
        id: Long,
        title: String,
        notes: String,
        section: RoutineSection,
        startMinuteOfDay: Int,
        endMinuteOfDay: Int,
        isCompleted: Boolean
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            repository.saveRoutineBlock(
                RoutineBlockEntity(
                    id = id,
                    title = cleanTitle,
                    notes = notes.trim(),
                    section = section,
                    startMinuteOfDay = startMinuteOfDay,
                    endMinuteOfDay = endMinuteOfDay,
                    isCompleted = isCompleted
                )
            )
            dismissRoutineDialog()
        }
    }

    fun toggleRoutineCompletion(block: RoutineBlockEntity) {
        viewModelScope.launch {
            repository.saveRoutineBlock(block.copy(isCompleted = !block.isCompleted))
        }
    }

    fun deleteRoutineBlock(id: Long) {
        viewModelScope.launch {
            repository.deleteRoutineBlock(id)
        }
    }

    fun resetRoutineForNewDay() {
        viewModelScope.launch {
            repository.resetRoutineCompletions()
        }
    }

    // Reminder CRUD
    fun openNewReminderDialog() {
        localControlFlow.update {
            it.copy(editingReminder = null, showReminderDialog = true)
        }
    }

    fun openEditReminderDialog(reminder: ReminderEntity) {
        localControlFlow.update {
            it.copy(editingReminder = reminder, showReminderDialog = true)
        }
    }

    fun dismissReminderDialog() {
        localControlFlow.update { it.copy(showReminderDialog = false, editingReminder = null) }
    }

    fun saveReminder(
        id: Long,
        title: String,
        priority: ReminderPriority,
        dueMinuteOfDay: Int,
        isCompleted: Boolean
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        viewModelScope.launch {
            repository.saveReminder(
                ReminderEntity(
                    id = id,
                    title = cleanTitle,
                    priority = priority,
                    dueMinuteOfDay = dueMinuteOfDay,
                    isCompleted = isCompleted
                )
            )
            dismissReminderDialog()
        }
    }

    fun toggleReminderCompletion(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.saveReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    // Scratchpad, Quick Logs & Habits
    fun updateScratchpad(text: String) {
        viewModelScope.launch {
            repository.updateDailyState { it.copy(scratchpadText = text) }
        }
    }

    fun addQuickLog(content: String) {
        viewModelScope.launch {
            repository.addQuickLog(content)
        }
    }

    fun deleteQuickLog(id: Long) {
        viewModelScope.launch {
            repository.deleteQuickLog(id)
        }
    }

    fun adjustWaterGlasses(delta: Int) {
        viewModelScope.launch {
            repository.updateDailyState { state ->
                state.copy(waterGlasses = (state.waterGlasses + delta).coerceIn(0, 20))
            }
        }
    }

    fun adjustStudyMinutes(deltaMinutes: Int) {
        viewModelScope.launch {
            repository.updateDailyState { state ->
                state.copy(studyMinutes = (state.studyMinutes + deltaMinutes).coerceIn(0, 1440))
            }
        }
    }
}
