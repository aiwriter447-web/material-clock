package app.materialclock.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.GroupRemove
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.data.WeekStart
import app.materialclock.data.order
import app.materialclock.ui.DOCK_HEIGHT
import app.materialclock.ui.dockFrostedGlass
import app.materialclock.ui.sheets.systemFirstDay
import java.time.DayOfWeek

private val AlarmDeleteColor = Color(0xFFB3261E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmsScreen(
    alarms: List<Alarm>,
    groups: List<AlarmGroup>,
    weekStart: WeekStart,
    onSelectionChange: (Boolean) -> Unit,
    onToggle: (Long) -> Unit,
    onToggleGroup: (Long, Boolean) -> Unit,
    onTogglePin: (Long) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (Alarm) -> Unit,
    onDeleteSelected: (Set<Long>, Set<Long>) -> Unit,
    onSetEnabledSelected: (Set<Long>, Set<Long>, Boolean) -> Unit,
    onUngroupSelected: (Set<Long>, Set<Long>) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val order = remember(weekStart) { weekStart.order(systemFirstDay()) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current

    var is24Hour by remember {
        mutableStateOf(android.text.format.DateFormat.is24HourFormat(context))
    }

    val adjustedContentPadding = remember(contentPadding, layoutDirection) {
        PaddingValues(
            top = contentPadding.calculateTopPadding() + 10.dp,
            bottom = contentPadding.calculateBottomPadding(),
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection)
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                is24Hour = android.text.format.DateFormat.is24HourFormat(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val byGroup = remember(alarms) {
        alarms.groupBy { it.groupId }
    }

    var selectedAlarms by remember { mutableStateOf(emptySet<Long>()) }
    var selectedGroups by remember { mutableStateOf(emptySet<Long>()) }

    val inSelectionMode =
        selectedAlarms.isNotEmpty() || selectedGroups.isNotEmpty()

    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(inSelectionMode) {
        onSelectionChange(inSelectionMode)
    }

    BackHandler(enabled = inSelectionMode) {
        selectedAlarms = emptySet()
        selectedGroups = emptySet()
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showBatchDeleteConfirm = false
            },
            title = {
                Text(
                    text = "Delete Items?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the selected ${selectedAlarms.size} alarms and ${selectedGroups.size} groups?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Surface(
                    onClick = {
                        onDeleteSelected(selectedAlarms, selectedGroups)
                        selectedAlarms = emptySet()
                        selectedGroups = emptySet()
                        showBatchDeleteConfirm = false
                    },
                    shape = CircleShape,
                    color = AlarmDeleteColor,
                    contentColor = Color.White,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 11.dp
                        )
                    )
                }
            },
            dismissButton = {
                Surface(
                    onClick = {
                        showBatchDeleteConfirm = false
                    },
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = 1.25.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = 22.dp,
                            vertical = 11.dp
                        )
                    )
                }
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columnCount = when {
            maxWidth >= 840.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 1
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier.fillMaxSize(),
            contentPadding = adjustedContentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (groups.isNotEmpty()) {
                item(
                    key = "group-cards",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "groups"
                ) {
                    GroupCardsSection(
                        groups = groups,
                        byGroup = byGroup,
                        selectedGroups = selectedGroups,
                        inSelectionMode = inSelectionMode,
                        onToggleGroup = onToggleGroup,
                        onToggleGroupSelect = { groupId ->
                            selectedGroups =
                                if (groupId in selectedGroups) {
                                    selectedGroups - groupId
                                } else {
                                    selectedGroups + groupId
                                }
                        }
                    )
                }
            }

            items(
                items = alarms,
                key = { it.id },
                contentType = { "alarm" }
            ) { alarm ->
                val isSelected = alarm.id in selectedAlarms

                AlarmRow(
                    alarm = alarm,
                    order = order,
                    is24Hour = is24Hour,
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    onToggle = {
                        onToggle(alarm.id)
                    },
                    onEdit = {
                        onEdit(alarm)
                    },
                    onToggleSelect = {
                        selectedAlarms =
                            if (isSelected) {
                                selectedAlarms - alarm.id
                            } else {
                                selectedAlarms + alarm.id
                            }
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
                        placementSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
                        fadeOutSpec = spring(dampingRatio = 0.86f, stiffness = 380f)
                    )
                )
            }
        }

        AnimatedVisibility(
            visible = inSelectionMode,
            enter =
                fadeIn(
                    spring(dampingRatio = 0.82f, stiffness = 360f)
                ) +
                slideInVertically(
                    spring(dampingRatio = 0.78f, stiffness = 340f)
                ) { it } +
                scaleIn(
                    spring(dampingRatio = 0.78f, stiffness = 340f),
                    initialScale = 0.88f
                ),
            exit =
                fadeOut(spring(dampingRatio = 0.86f, stiffness = 380f)) +
                slideOutVertically(
                    spring(dampingRatio = 0.86f, stiffness = 380f)
                ) { it } +
                scaleOut(
                    spring(dampingRatio = 0.86f, stiffness = 380f),
                    targetScale = 0.88f
                ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                        toolbarContainerColor = Color.Transparent,
                        toolbarContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    expandedShadowElevation = 0.dp,
                    modifier = Modifier
                        .height(DOCK_HEIGHT)
                        .dockFrostedGlass(),
                ) {
                    val allSelected =
                        selectedAlarms.size == alarms.size &&
                        selectedGroups.size == groups.size

                    ToolbarIconButton(
                        onClick = {
                            if (allSelected) {
                                selectedAlarms = emptySet()
                                selectedGroups = emptySet()
                            } else {
                                selectedAlarms = alarms.map { it.id }.toSet()
                                selectedGroups = groups.map { it.id }.toSet()
                            }
                        },
                        icon = if (allSelected) {
                            Icons.Rounded.Deselect
                        } else {
                            Icons.Rounded.SelectAll
                        },
                        contentDescription = "Select All"
                    )

                    ToolbarIconButton(
                        onClick = {
                            onSetEnabledSelected(
                                selectedAlarms,
                                selectedGroups,
                                true
                            )
                            selectedAlarms = emptySet()
                            selectedGroups = emptySet()
                        },
                        icon = Icons.Rounded.AlarmOn,
                        contentDescription = "Turn On"
                    )

                    ToolbarIconButton(
                        onClick = {
                            onSetEnabledSelected(
                                selectedAlarms,
                                selectedGroups,
                                false
                            )
                            selectedAlarms = emptySet()
                            selectedGroups = emptySet()
                        },
                        icon = Icons.Rounded.AlarmOff,
                        contentDescription = "Turn Off"
                    )

                    ToolbarIconButton(
                        onClick = {
                            onUngroupSelected(
                                selectedAlarms,
                                selectedGroups
                            )
                            selectedAlarms = emptySet()
                            selectedGroups = emptySet()
                        },
                        icon = Icons.Rounded.GroupRemove,
                        contentDescription = "Ungroup"
                    )

                    ToolbarIconButton(
                        onClick = {
                            showBatchDeleteConfirm = true
                        },
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = AlarmDeleteColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolbarIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    tint: Color = LocalContentColor.current
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 480f
        ),
        label = "toolbarIconScale"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

private val GROUP_SECTION_PADDING_H = 2.dp
private val GROUP_CARD_GAP = 12.dp
private val GROUP_CARD_PADDING_H = 14.dp
private val GROUP_CARD_PADDING_V = 12.dp

@Composable
private fun GroupCardsSection(
    groups: List<AlarmGroup>,
    byGroup: Map<Long?, List<Alarm>>,
    selectedGroups: Set<Long>,
    inSelectionMode: Boolean,
    onToggleGroup: (Long, Boolean) -> Unit,
    onToggleGroupSelect: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GROUP_SECTION_PADDING_H),
        verticalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
    ) {
        groups.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GROUP_CARD_GAP),
            ) {
                pair.forEach { group ->
                    val groupAlarms = byGroup[group.id].orEmpty()
                    val isSelected = group.id in selectedGroups
                    val armedCount = groupAlarms.count { it.enabled }
                    val unarmedCount = groupAlarms.size - armedCount

                    GroupCard(
                        group = group,
                        armedCount = armedCount,
                        unarmedCount = unarmedCount,
                        checked = groupAlarms.any { it.enabled },
                        isSelected = isSelected,
                        inSelectionMode = inSelectionMode,
                        onToggle = {
                            onToggleGroup(group.id, it)
                        },
                        onToggleSelect = {
                            onToggleGroupSelect(group.id)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupCard(
    group: AlarmGroup,
    armedCount: Int,
    unarmedCount: Int,
    checked: Boolean,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggle: (Boolean) -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 440f
        ),
        label = "groupScale"
    )

    val targetContainer =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (checked) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    val targetInk =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else if (checked) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "groupContainer"
    )

    val ink by animateColorAsState(
        targetValue = targetInk,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "groupInk"
    )

    val titleFontWeight =
        if (checked) FontWeight.SemiBold else FontWeight.Medium

    val targetIconBackground =
        if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }

    val targetIconTint =
        if (checked) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    val iconBackground by animateColorAsState(
        targetValue = targetIconBackground,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "iconBg"
    )

    val iconTint by animateColorAsState(
        targetValue = targetIconTint,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "iconTint"
    )

    val iconVector =
        if (checked) Icons.Rounded.Alarm else Icons.Rounded.AlarmOff

    val pillBorderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        }

    Surface(
        color = container,
        shape = CircleShape,
        border = BorderStroke(1.dp, pillBorderColor),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (inSelectionMode) {
                        onToggleSelect()
                    } else {
                        onToggle(!checked)
                    }
                },
                onLongClick = {
                    onToggleSelect()
                }
            ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = GROUP_CARD_PADDING_H,
                vertical = GROUP_CARD_PADDING_V
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconBackground,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedContent(
                        targetState = iconVector,
                        transitionSpec = {
                            (
                                fadeIn(tween(200)) +
                                scaleIn(
                                    spring(
                                        dampingRatio = 0.74f,
                                        stiffness = 400f
                                    )
                                )
                            ) togetherWith (
                                fadeOut(tween(140)) +
                                scaleOut(tween(140))
                            )
                        },
                        label = "groupIconAnim"
                    ) { targetIcon ->
                        Icon(
                            imageVector = targetIcon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = titleFontWeight
                    ),
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(3.dp))

                GroupCount(
                    armed = armedCount,
                    unarmed = unarmedCount,
                    ink = ink
                )
            }
        }
    }
}

@Composable
private fun GroupCount(
    armed: Int,
    unarmed: Int,
    ink: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Alarm,
            contentDescription = "On Alarms",
            tint = ink.copy(alpha = 0.75f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(4.dp))

        Text(
            armed.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = ink
        )

        Spacer(Modifier.width(10.dp))

        Icon(
            Icons.Rounded.AlarmOff,
            contentDescription = "Off Alarms",
            tint = ink.copy(alpha = 0.75f),
            modifier = Modifier.size(14.dp)
        )

        Spacer(Modifier.width(4.dp))

        Text(
            unarmed.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = ink
        )
    }
}

private val ROW_HORIZONTAL_PADDING = 20.dp
private val ROW_VERTICAL_PADDING = 16.dp
private val ROW_CORNER_RADIUS = 28.dp
private val ROW_LABEL_TO_TIME = 6.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmRow(
    alarm: Alarm,
    order: List<DayOfWeek>,
    is24Hour: Boolean,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = alarm.enabled

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 440f
        ),
        label = "rowScale"
    )

    val timeBreathScale by animateFloatAsState(
        targetValue = if (enabled) 1.02f else 1.0f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 340f
        ),
        label = "timeBreathScale"
    )

    val targetContainer =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    val targetInk =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else if (enabled) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    val container by animateColorAsState(
        targetValue = targetContainer,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "rowContainer"
    )

    val ink by animateColorAsState(
        targetValue = targetInk,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "rowInk"
    )

    val outlineColor = MaterialTheme.colorScheme.outline

    val cardBorderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
        }

    val hour12 =
        (alarm.time.hour % 12).takeIf { it != 0 } ?: 12

    val meridiem =
        if (alarm.time.hour < 12) "AM" else "PM"

    val displayLabel =
        if (alarm.label.isNotBlank()) alarm.label else " "

    val labelColor =
        if (alarm.label.isNotBlank()) ink else Color.Transparent

    val timeFontWeight =
        if (enabled) FontWeight.Medium else FontWeight.Normal

    val titleFontWeight =
        if (enabled) FontWeight.SemiBold else FontWeight.Normal

    Surface(
        color = container,
        shape = RoundedCornerShape(ROW_CORNER_RADIUS),
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (inSelectionMode) {
                        onToggleSelect()
                    } else {
                        onEdit()
                    }
                },
                onLongClick = {
                    onToggleSelect()
                }
            ),
    ) {
        BoxWithConstraints {
            val fontScale = LocalDensity.current.fontScale
            val rowInner = maxWidth - ROW_HORIZONTAL_PADDING * 2
            val rowVPad = if (LocalConfiguration.current.screenHeightDp < 480) 10.dp else ROW_VERTICAL_PADDING
            val timeNeed = (if (is24Hour) 95f else 130f) * fontScale
            val daysAvail = rowInner.value - timeNeed - 16f
            val daysScale = (daysAvail / (152f * fontScale)).coerceIn(0.55f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ROW_HORIZONTAL_PADDING,
                        vertical = rowVPad
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = timeBreathScale
                            scaleY = timeBreathScale
                        },
                    verticalArrangement = Arrangement.Top,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = titleFontWeight
                            ),
                            color = labelColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (alarm.pinnedAt != null) {
                            Spacer(Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = "Pinned",
                                tint = ink,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ROW_LABEL_TO_TIME))

                    val timeStyle =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = timeFontWeight,
                            fontFeatureSettings = "tnum"
                        )

                    val amPmStyle =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = timeFontWeight
                        )

                    if (is24Hour) {
                        Text(
                            text = "%02d:%02d".format(
                                alarm.time.hour,
                                alarm.time.minute
                            ),
                            style = timeStyle,
                            color = ink,
                            maxLines = 1,
                            softWrap = false
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%02d:%02d".format(
                                    hour12,
                                    alarm.time.minute
                                ),
                                style = timeStyle,
                                color = ink,
                                maxLines = 1,
                                softWrap = false
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = meridiem,
                                style = amPmStyle,
                                color = ink,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top,
                ) {
                    DayLetters(
                        alarm = alarm,
                        order = order,
                        ink = ink,
                        enabled = enabled,
                        scale = daysScale
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            if (!inSelectionMode) onToggle()
                        },
                        enabled = !inSelectionMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = container,
                            checkedTrackColor = ink,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = ink,
                            uncheckedTrackColor = Color.Transparent,
                            uncheckedBorderColor = outlineColor,
                            disabledCheckedThumbColor = container,
                            disabledCheckedTrackColor = ink,
                            disabledCheckedBorderColor = Color.Transparent,
                            disabledUncheckedThumbColor = ink,
                            disabledUncheckedTrackColor = Color.Transparent,
                            disabledUncheckedBorderColor = outlineColor,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayLetters(
    alarm: Alarm,
    order: List<DayOfWeek>,
    ink: Color,
    enabled: Boolean,
    scale: Float = 1f
) {
    val text = remember(
        alarm.days,
        alarm.isOneShot,
        order,
        ink,
        enabled
    ) {
        buildAnnotatedString {
            order.forEach { day ->
                val active = !alarm.isOneShot && day in alarm.days
                val isBold = enabled && active

                withStyle(
                    SpanStyle(
                        fontWeight = if (isBold) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        color = if (active) {
                            ink
                        } else {
                            ink.copy(alpha = 0.30f)
                        },
                    )
                ) {
                    append(day.name.take(1) + " ")
                }
            }
        }
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = (17f * scale).sp,
            letterSpacing = (2.5f * scale).sp
        ),
        maxLines = 1,
        softWrap = false
    )
}
