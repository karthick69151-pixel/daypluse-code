package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.DayPulseUiState
import com.example.ui.FocusTimerMode
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.util.TimeUtils

@Composable
fun HeroFocusDashboard(
    uiState: DayPulseUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onFocusTextChange: (String) -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onSelectStopwatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focusDraft by rememberSaveable { mutableStateOf(uiState.displayedFocusText) }
    var isTimerExpanded by rememberSaveable { mutableStateOf(true) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.displayedFocusText, uiState.dailyState.autoSyncFocus) {
        if (focusDraft != uiState.displayedFocusText) {
            focusDraft = uiState.displayedFocusText
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = uiState.completionProgress,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "daily_progress"
    )

    val syncBg by animateColorAsState(
        targetValue = if (uiState.dailyState.autoSyncFocus) {
            Color(0xFFD45B20)
        } else {
            Color.White.copy(alpha = 0.14f)
        },
        label = "sync_chip_bg"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Hero Banner Artwork with deep contrast scrim
            Image(
                painter = painterResource(id = R.drawable.img_focus_banner),
                contentDescription = "DayPulse header artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xD9121824),
                                Color(0xE8161E2C),
                                Color(0xF5121824)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Row: Greeting, Date & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.greeting,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFFFF8F0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                color = Color(0xFF236B58).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Auto-saved locally",
                                        tint = Color(0xFFBDF5E6),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Saved",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFBDF5E6)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.clock.formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFBAC4D6)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isTimerExpanded = !isTimerExpanded },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toggle_focus_panel_button")
                        ) {
                            Icon(
                                imageVector = if (isTimerExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = if (isTimerExpanded) "Collapse focus panel" else "Expand focus panel",
                                tint = Color(0xFFE6ECF5)
                            )
                        }
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDarkTheme) "Switch to light theme" else "Switch to dark theme",
                                tint = Color(0xFFF6B26B)
                            )
                        }
                    }
                }

                // Live Ticking Digital Clock + Progress Percentage Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = uiState.clock.formattedClock,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = (-0.5).sp,
                        color = Color(0xFFFFFFFF),
                        modifier = Modifier.testTag("live_digital_clock")
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${uiState.completionPercentage}% Done",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFF48E53),
                            modifier = Modifier.testTag("progress_percentage_text")
                        )
                        Text(
                            text = "${uiState.completedRoutineCount}/${uiState.totalRoutineCount} blocks",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBAC4D6)
                        )
                    }
                }

                // Daily Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .testTag("daily_progress_bar"),
                    color = Color(0xFFF48E53),
                    trackColor = Color.White.copy(alpha = 0.16f),
                    strokeCap = StrokeCap.Round
                )

                // Current Activity ("I'm Doing Now") + Focus Timer
                AnimatedVisibility(visible = isTimerExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Label + Auto-Sync Toggle
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
                                    imageVector = Icons.Outlined.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFF48E53),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "I'M DOING NOW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFF48E53)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(syncBg)
                                    .clickable { onToggleAutoSync(!uiState.dailyState.autoSyncFocus) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("auto_sync_chip"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Sync,
                                    contentDescription = "Auto-sync with schedule",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (uiState.dailyState.autoSyncFocus) "Auto-Sync ON" else "Manual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        // Editable Current Activity Input
                        BasicTextField(
                            value = focusDraft,
                            onValueChange = { updated ->
                                focusDraft = updated
                                onFocusTextChange(updated)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            ),
                            cursorBrush = SolidColor(Color(0xFFF48E53)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("current_focus_input"),
                            decorationBox = { innerTextField ->
                                if (focusDraft.isEmpty()) {
                                    Text(
                                        text = "Type what you're doing right now...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF8C97AB)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Focus Timer Controls
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
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = "Focus timer",
                                    tint = Color(0xFFBAC4D6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = TimeUtils.formatDurationSeconds(uiState.focusTimer.displaySeconds),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = if (uiState.focusTimer.isRunning) Color(0xFF5CD1B2) else Color.White,
                                    modifier = Modifier.testTag("focus_timer_display")
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Play / Pause button
                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (uiState.focusTimer.isRunning) Color(0xFF236B58)
                                            else Color(0xFFD45B20)
                                        )
                                        .clickable(onClick = onToggleTimer)
                                        .testTag("focus_timer_play_pause"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.focusTimer.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (uiState.focusTimer.isRunning) "Pause focus timer" else "Start focus timer",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Reset button
                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable(onClick = onResetTimer)
                                        .testTag("focus_timer_reset"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Reset focus timer",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Timer Presets Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 25, 45).forEach { mins ->
                                val selected = uiState.focusTimer.mode == FocusTimerMode.COUNTDOWN &&
                                    uiState.focusTimer.presetMinutes == mins
                                TimerModeChip(
                                    label = "${mins}m",
                                    selected = selected,
                                    onClick = { onSelectPreset(mins) }
                                )
                            }
                            TimerModeChip(
                                label = "Stopwatch",
                                selected = uiState.focusTimer.mode == FocusTimerMode.STOPWATCH,
                                onClick = onSelectStopwatch
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Color.White.copy(alpha = 0.24f)
                else Color.White.copy(alpha = 0.08f)
            )
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFFF48E53) else Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Color(0xFFBAC4D6)
        )
    }
}
