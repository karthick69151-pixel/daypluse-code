package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ReminderEntity
import com.example.data.local.RoutineBlockEntity
import com.example.data.model.ReminderPriority
import com.example.data.model.RoutineSection
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBlockEditorDialog(
    initialBlock: RoutineBlockEntity?,
    defaultSection: RoutineSection,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        notes: String,
        section: RoutineSection,
        startMinuteOfDay: Int,
        endMinuteOfDay: Int,
        isCompleted: Boolean
    ) -> Unit
) {
    val defaultStart = initialBlock?.startMinuteOfDay ?: when (defaultSection) {
        RoutineSection.MORNING -> 8 * 60
        RoutineSection.AFTERNOON -> 13 * 60
        RoutineSection.EVENING -> 19 * 60
    }
    val defaultEnd = initialBlock?.endMinuteOfDay ?: (defaultStart + 60)

    var title by remember { mutableStateOf(initialBlock?.title ?: "") }
    var notes by remember { mutableStateOf(initialBlock?.notes ?: "") }
    var startMinute by remember { mutableIntStateOf(defaultStart) }
    var endMinute by remember { mutableIntStateOf(defaultEnd) }
    var section by remember { mutableStateOf(initialBlock?.section ?: defaultSection) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBlock == null) "Add Time Block" else "Edit Time Block",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title") },
                    placeholder = { Text("e.g., Morning Workout") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_dialog_title_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g., 30 min run + stretch") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_dialog_notes_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { pickingStart = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("routine_dialog_start_time_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Start", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = TimeUtils.formatMinuteOfDay(startMinute),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { pickingEnd = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("routine_dialog_end_time_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("End", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = TimeUtils.formatMinuteOfDay(endMinute),
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Section",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoutineSection.entries.forEach { item ->
                            val shortLabel = when (item) {
                                RoutineSection.MORNING -> "Morning"
                                RoutineSection.AFTERNOON -> "Afternoon"
                                RoutineSection.EVENING -> "Evening"
                            }
                            FilterChip(
                                selected = section == item,
                                onClick = { section = item },
                                label = { Text(shortLabel, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialBlock?.id ?: 0L,
                        title,
                        notes,
                        section,
                        startMinute,
                        endMinute,
                        initialBlock?.isCompleted ?: false
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("routine_dialog_save_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (pickingStart) {
        TimePickerModal(
            initialMinuteOfDay = startMinute,
            title = "Select Start Time",
            onDismiss = { pickingStart = false },
            onConfirm = { picked ->
                startMinute = picked
                if (endMinute <= picked) {
                    endMinute = (picked + 30).coerceAtMost(23 * 60 + 59)
                }
                section = RoutineSection.fromStartMinute(picked)
                pickingStart = false
            }
        )
    }

    if (pickingEnd) {
        TimePickerModal(
            initialMinuteOfDay = endMinute,
            title = "Select End Time",
            onDismiss = { pickingEnd = false },
            onConfirm = { picked ->
                endMinute = picked
                pickingEnd = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditorDialog(
    initialReminder: ReminderEntity?,
    defaultDueMinute: Int,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        priority: ReminderPriority,
        dueMinuteOfDay: Int,
        isCompleted: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var priority by remember { mutableStateOf(initialReminder?.priority ?: ReminderPriority.MEDIUM) }
    var dueMinute by remember { mutableIntStateOf(initialReminder?.dueMinuteOfDay ?: defaultDueMinute) }
    var pickingTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialReminder == null) "New Reminder" else "Edit Reminder",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder") },
                    placeholder = { Text("e.g., Call dentist at 3 PM") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_dialog_title_input")
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReminderPriority.entries.forEach { item ->
                            FilterChip(
                                selected = priority == item,
                                onClick = { priority = item },
                                label = { Text(item.label) },
                                modifier = Modifier.testTag("reminder_priority_${item.name.lowercase()}")
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { pickingTime = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reminder_dialog_due_time_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Due Time: ")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = TimeUtils.formatMinuteOfDay(dueMinute),
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialReminder?.id ?: 0L,
                        title,
                        priority,
                        dueMinute,
                        initialReminder?.isCompleted ?: false
                    )
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("reminder_dialog_save_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (pickingTime) {
        TimePickerModal(
            initialMinuteOfDay = dueMinute,
            title = "Select Due Time",
            onDismiss = { pickingTime = false },
            onConfirm = { picked ->
                dueMinute = picked
                pickingTime = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(
    initialMinuteOfDay: Int,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val normalized = ((initialMinuteOfDay % 1440) + 1440) % 1440
    val state = rememberTimePickerState(
        initialHour = normalized / 60,
        initialMinute = normalized % 60,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                TimePicker(state = state)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(state.hour * 60 + state.minute) }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
