package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeroFocusDashboard
import com.example.ui.components.NotesAndHabitsScreen
import com.example.ui.components.ReminderEditorDialog
import com.example.ui.components.RemindersScreen
import com.example.ui.components.RoutineBlockEditorDialog
import com.example.ui.components.RoutineScreen
import com.example.ui.theme.DayPulseTheme

@Composable
fun DayPulseApp(
    viewModel: DayPulseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = uiState.dailyState.darkModeOverride ?: systemDark

    if (uiState.selectedTab != MainTab.ROUTINE) {
        BackHandler {
            viewModel.selectTab(MainTab.ROUTINE)
        }
    }

    DayPulseTheme(darkTheme = isDarkTheme) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 720.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            MainTab.entries.forEach { tab ->
                                val selected = uiState.selectedTab == tab
                                val icon = when (tab) {
                                    MainTab.ROUTINE -> if (selected) Icons.Filled.Schedule else Icons.Outlined.Schedule
                                    MainTab.REMINDERS -> if (selected) Icons.Filled.Notifications else Icons.Outlined.Notifications
                                    MainTab.NOTES_HABITS -> if (selected) Icons.Filled.EditNote else Icons.Outlined.EditNote
                                }
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        if (tab == MainTab.REMINDERS && uiState.pendingReminderCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge {
                                                        Text(uiState.pendingReminderCount.toString())
                                                    }
                                                }
                                            ) {
                                                Icon(icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = tab.label)
                                        }
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    when (uiState.selectedTab) {
                        MainTab.ROUTINE -> {
                            ExtendedFloatingActionButton(
                                onClick = { viewModel.openNewRoutineDialog() },
                                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                                text = { Text("Add Block") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("fab_add_routine")
                            )
                        }
                        MainTab.REMINDERS -> {
                            ExtendedFloatingActionButton(
                                onClick = { viewModel.openNewReminderDialog() },
                                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                                text = { Text("Add Reminder") },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("fab_add_reminder")
                            )
                        }
                        MainTab.NOTES_HABITS -> Unit
                    }
                }
            ) { innerPadding ->
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            MainTab.entries.forEach { tab ->
                                val selected = uiState.selectedTab == tab
                                val icon = when (tab) {
                                    MainTab.ROUTINE -> if (selected) Icons.Filled.Schedule else Icons.Outlined.Schedule
                                    MainTab.REMINDERS -> if (selected) Icons.Filled.Notifications else Icons.Outlined.Notifications
                                    MainTab.NOTES_HABITS -> if (selected) Icons.Filled.EditNote else Icons.Outlined.EditNote
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = { Icon(icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                                )
                            }
                        }

                        // Expanded Split Dashboard
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HeroFocusDashboard(
                                uiState = uiState,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkMode(isDarkTheme) },
                                onFocusTextChange = viewModel::updateCustomFocusText,
                                onToggleAutoSync = viewModel::setAutoSyncFocus,
                                onToggleTimer = viewModel::toggleFocusTimer,
                                onResetTimer = viewModel::resetFocusTimer,
                                onSelectPreset = viewModel::selectTimerPreset,
                                onSelectStopwatch = viewModel::selectStopwatchMode
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(18.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1.15f)
                                        .fillMaxHeight()
                                ) {
                                    RoutineScreen(
                                        blocks = uiState.routineBlocks,
                                        activeBlockId = uiState.activeBlock?.id,
                                        onToggleComplete = viewModel::toggleRoutineCompletion,
                                        onEditBlock = viewModel::openEditRoutineDialog,
                                        onDeleteBlock = viewModel::deleteRoutineBlock,
                                        onFocusBlock = viewModel::focusOnBlock,
                                        onAddBlockInSection = viewModel::openNewRoutineDialog,
                                        onResetDay = viewModel::resetRoutineForNewDay
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(0.95f)
                                        .fillMaxHeight()
                                ) {
                                    if (uiState.selectedTab == MainTab.REMINDERS) {
                                        RemindersScreen(
                                            reminders = uiState.filteredReminders,
                                            selectedFilter = uiState.reminderFilter,
                                            isDarkTheme = isDarkTheme,
                                            onSelectFilter = viewModel::selectReminderFilter,
                                            onToggleComplete = viewModel::toggleReminderCompletion,
                                            onEditReminder = viewModel::openEditReminderDialog,
                                            onDeleteReminder = viewModel::deleteReminder,
                                            onAddReminder = viewModel::openNewReminderDialog
                                        )
                                    } else {
                                        NotesAndHabitsScreen(
                                            dailyState = uiState.dailyState,
                                            quickLogs = uiState.quickLogs,
                                            onAdjustWater = viewModel::adjustWaterGlasses,
                                            onAdjustStudyMinutes = viewModel::adjustStudyMinutes,
                                            onUpdateScratchpad = viewModel::updateScratchpad,
                                            onAddQuickLog = viewModel::addQuickLog,
                                            onDeleteQuickLog = viewModel::deleteQuickLog
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Compact Portrait Layout
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 600.dp)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HeroFocusDashboard(
                                uiState = uiState,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDarkMode(isDarkTheme) },
                                onFocusTextChange = viewModel::updateCustomFocusText,
                                onToggleAutoSync = viewModel::setAutoSyncFocus,
                                onToggleTimer = viewModel::toggleFocusTimer,
                                onResetTimer = viewModel::resetFocusTimer,
                                onSelectPreset = viewModel::selectTimerPreset,
                                onSelectStopwatch = viewModel::selectStopwatchMode
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                when (uiState.selectedTab) {
                                    MainTab.ROUTINE -> {
                                        RoutineScreen(
                                            blocks = uiState.routineBlocks,
                                            activeBlockId = uiState.activeBlock?.id,
                                            onToggleComplete = viewModel::toggleRoutineCompletion,
                                            onEditBlock = viewModel::openEditRoutineDialog,
                                            onDeleteBlock = viewModel::deleteRoutineBlock,
                                            onFocusBlock = viewModel::focusOnBlock,
                                            onAddBlockInSection = viewModel::openNewRoutineDialog,
                                            onResetDay = viewModel::resetRoutineForNewDay
                                        )
                                    }
                                    MainTab.REMINDERS -> {
                                        RemindersScreen(
                                            reminders = uiState.filteredReminders,
                                            selectedFilter = uiState.reminderFilter,
                                            isDarkTheme = isDarkTheme,
                                            onSelectFilter = viewModel::selectReminderFilter,
                                            onToggleComplete = viewModel::toggleReminderCompletion,
                                            onEditReminder = viewModel::openEditReminderDialog,
                                            onDeleteReminder = viewModel::deleteReminder,
                                            onAddReminder = viewModel::openNewReminderDialog
                                        )
                                    }
                                    MainTab.NOTES_HABITS -> {
                                        NotesAndHabitsScreen(
                                            dailyState = uiState.dailyState,
                                            quickLogs = uiState.quickLogs,
                                            onAdjustWater = viewModel::adjustWaterGlasses,
                                            onAdjustStudyMinutes = viewModel::adjustStudyMinutes,
                                            onUpdateScratchpad = viewModel::updateScratchpad,
                                            onAddQuickLog = viewModel::addQuickLog,
                                            onDeleteQuickLog = viewModel::deleteQuickLog
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.showRoutineDialog) {
                RoutineBlockEditorDialog(
                    initialBlock = uiState.editingRoutineBlock,
                    defaultSection = uiState.prefillSection,
                    onDismiss = viewModel::dismissRoutineDialog,
                    onSave = viewModel::saveRoutineBlock
                )
            }

            if (uiState.showReminderDialog) {
                ReminderEditorDialog(
                    initialReminder = uiState.editingReminder,
                    defaultDueMinute = (uiState.clock.minuteOfDay + 60) % 1440,
                    onDismiss = viewModel::dismissReminderDialog,
                    onSave = viewModel::saveReminder
                )
            }
        }
    }
}
