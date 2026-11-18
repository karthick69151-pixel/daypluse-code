package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.local.DailyStateEntity
import com.example.data.local.QuickLogEntity
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.StudyAccent
import com.example.ui.theme.WaterAccent
import com.example.util.TimeUtils
import java.util.Locale

@Composable
fun NotesAndHabitsScreen(
    dailyState: DailyStateEntity,
    quickLogs: List<QuickLogEntity>,
    onAdjustWater: (Int) -> Unit,
    onAdjustStudyMinutes: (Int) -> Unit,
    onUpdateScratchpad: (String) -> Unit,
    onAddQuickLog: (String) -> Unit,
    onDeleteQuickLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var scratchpadDraft by rememberSaveable { mutableStateOf(dailyState.scratchpadText) }
    var lastSavedScratchpad by remember { mutableStateOf(dailyState.scratchpadText) }
    var thoughtInput by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(dailyState.scratchpadText) {
        if (dailyState.scratchpadText != lastSavedScratchpad) {
            scratchpadDraft = dailyState.scratchpadText
            lastSavedScratchpad = dailyState.scratchpadText
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "habits_header") {
            Text(
                text = "Daily Habits",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item(key = "habit_water_card") {
            WaterHabitCard(
                glasses = dailyState.waterGlasses,
                goal = dailyState.waterGoal,
                onDecrease = { onAdjustWater(-1) },
                onIncrease = { onAdjustWater(1) }
            )
        }

        item(key = "habit_study_card") {
            StudyHabitCard(
                studyMinutes = dailyState.studyMinutes,
                goalMinutes = dailyState.studyGoalMinutes,
                onDecrease = { onAdjustStudyMinutes(-15) },
                onIncrease = { onAdjustStudyMinutes(15) }
            )
        }

        item(key = "scratchpad_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Quick Scratchpad",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        if (scratchpadDraft.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    scratchpadDraft = ""
                                    lastSavedScratchpad = ""
                                    onUpdateScratchpad("")
                                },
                                modifier = Modifier.testTag("clear_scratchpad_button")
                            ) {
                                Text("Clear")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = scratchpadDraft,
                        onValueChange = { updated ->
                            scratchpadDraft = updated
                            lastSavedScratchpad = updated
                            onUpdateScratchpad(updated)
                        },
                        placeholder = {
                            Text("Jot priorities, links, or reminders — saved automatically on device...")
                        },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scratchpad_input")
                    )
                }
            }
        }

        item(key = "quick_log_composer") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Daily Thought Log",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = thoughtInput,
                        onValueChange = { thoughtInput = it },
                        placeholder = { Text("Capture a quick thought or win...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (thoughtInput.isNotBlank()) {
                                    onAddQuickLog(thoughtInput)
                                    thoughtInput = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_log_input")
                    )

                    FilledTonalIconButton(
                        onClick = {
                            if (thoughtInput.isNotBlank()) {
                                onAddQuickLog(thoughtInput)
                                thoughtInput = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(50.dp)
                            .testTag("add_quick_log_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Add thought to daily log"
                        )
                    }
                }
            }
        }

        if (quickLogs.isEmpty()) {
            item(key = "quick_logs_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "No thoughts logged today yet. Capture small wins or ideas above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(
                items = quickLogs,
                key = { "log_${it.id}" }
            ) { entry ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_log_item_${entry.id}"),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = TimeUtils.formatTimestamp(entry.createdAt),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { onDeleteQuickLog(entry.id) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_quick_log_${entry.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete log entry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaterHabitCard(
    glasses: Int,
    goal: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (glasses.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "water_progress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        tint = WaterAccent
                    )
                    Column {
                        Text(
                            text = "Water Intake",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "$glasses / $goal glasses",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("water_count_text")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("water_decrease_button")
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease water intake")
                    }
                    FilledTonalIconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("water_increase_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add one glass of water")
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = WaterAccent,
                trackColor = WaterAccent.copy(alpha = 0.16f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun StudyHabitCard(
    studyMinutes: Int,
    goalMinutes: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (studyMinutes.toFloat() / goalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "study_progress"
    )
    val hoursText = String.format(Locale.getDefault(), "%.2fh", studyMinutes / 60f)
    val goalText = String.format(Locale.getDefault(), "%.1fh", goalMinutes / 60f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        tint = StudyAccent
                    )
                    Column {
                        Text(
                            text = "Study & Focus Hours",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "$hoursText / $goalText ($studyMinutes min)",
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("study_hours_text")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("study_decrease_button")
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Subtract 15 study minutes")
                    }
                    FilledTonalIconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("study_increase_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add 15 study minutes")
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = StudyAccent,
                trackColor = StudyAccent.copy(alpha = 0.16f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
