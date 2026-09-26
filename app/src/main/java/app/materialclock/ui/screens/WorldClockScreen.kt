package app.materialclock.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.materialclock.core.WorldCity
import app.materialclock.data.HourFormat
import app.materialclock.data.WorldClockSettings
import app.materialclock.data.WorldClockStyle
import app.materialclock.ui.DOCK_HEIGHT
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ROW_HEIGHT_DP = 104 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    cities: List<WorldCity>,
    home: ZoneId,
    nowUtcMillis: Long,
    settings: WorldClockSettings,
    onSelectionChange: (Boolean) -> Unit,
    onRemove: (WorldCity) -> Unit,
    onTogglePin: (WorldCity) -> Unit,
    onDeleteSelected: (Set<ZoneId>) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val use24h = when (settings.hourFormat) {
        HourFormat.SYSTEM -> android.text.format.DateFormat.is24HourFormat(LocalContext.current)
        HourFormat.H12 -> false
        HourFormat.H24 -> true
    }
    
    val measurer = rememberTextMeasurer()
    
    var selectedCities by remember { mutableStateOf(emptySet<ZoneId>()) }
    val inSelectionMode = selectedCities.isNotEmpty()
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(inSelectionMode) {
        onSelectionChange(inSelectionMode)
    }

    BackHandler(enabled = inSelectionMode) {
        selectedCities = emptySet()
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Delete Cities") },
            text = { Text("Are you sure you want to delete the selected ${selectedCities.size} cities?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelected(selectedCities)
                        selectedCities = emptySet()
                        showBatchDeleteConfirm = false
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp), 
        ) {
            item(key = "main-clock", contentType = "header") {
                Crossfade(
                    targetState = settings.style,
                    animationSpec = tween(durationMillis = 500),
                    label = "clockStyleCrossfade"
                ) { style ->
                    if (style == WorldClockStyle.DIGITAL) {
                        HomeDigitalClock(
                            home = home,
                            nowUtcMillis = nowUtcMillis,
                            use24h = use24h,
                            showSeconds = settings.showSeconds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    } else {
                        CityDial(
                            cities = cities,
                            nowUtcMillis = nowUtcMillis,
                            measurer = measurer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 54.dp, end = 54.dp, top = 4.dp, bottom = 18.dp)
                                .aspectRatio(1f),
                        )
                    }
                }
            }
            
            items(cities, key = { it.zone.id }, contentType = { "city" }) { city ->
                val isSelected = city.zone in selectedCities
                
                CityRow(
                    city = city,
                    home = home,
                    nowUtcMillis = nowUtcMillis,
                    use24h = use24h,
                    showSeconds = settings.showSeconds,
                    isSelected = isSelected,
                    inSelectionMode = inSelectionMode,
                    onToggleSelect = {
                        selectedCities = if (isSelected) selectedCities - city.zone else selectedCities + city.zone
                    },
                    modifier = Modifier.animateItem(),
                )
            }
        }

        AnimatedVisibility(
            visible = inSelectionMode,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp) 
        ) {
            val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    colors = if (dark) {
                        FloatingToolbarDefaults.standardFloatingToolbarColors(
                            toolbarContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            toolbarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FloatingToolbarDefaults.vibrantFloatingToolbarColors()
                    },
                    expandedShadowElevation = 6.dp,
                    modifier = Modifier.height(DOCK_HEIGHT),
                ) {
                    val allSelected = selectedCities.size == cities.size
                    IconButton(onClick = {
                        selectedCities = if (allSelected) emptySet() else cities.map { it.zone }.toSet()
                    }) {
                        Icon(if (allSelected) Icons.Rounded.Deselect else Icons.Rounded.SelectAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = {
                        selectedCities.forEach { z -> cities.find { it.zone == z }?.let { if (it.pinnedAt == null) onTogglePin(it) } }
                        selectedCities = emptySet()
                    }) {
                        Icon(Icons.Rounded.PushPin, contentDescription = "Pin")
                    }
                    IconButton(onClick = {
                        selectedCities.forEach { z -> cities.find { it.zone == z }?.let { if (it.pinnedAt != null) onTogglePin(it) } }
                        selectedCities = emptySet()
                    }) {
                        Icon(Icons.Outlined.PushPin, contentDescription = "Unpin")
                    }
                    IconButton(onClick = { showBatchDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDigitalClock(
    home: ZoneId,
    nowUtcMillis: Long,
    use24h: Boolean,
    showSeconds: Boolean,
    modifier: Modifier = Modifier,
) {
    val instant = Instant.ofEpochMilli(nowUtcMillis)
    val local = instant.atZone(home)
    
    val hour = if (use24h) local.hour else ((local.hour % 12).takeIf { it != 0 } ?: 12)
    val meridiem = if (local.hour < 12) "AM" else "PM"
    val ink = MaterialTheme.colorScheme.onSurface

    val timeText = buildString {
        append(String.format(Locale.getDefault(), "%02d:%02d", hour, local.minute))
        if (showSeconds) {
            append(String.format(Locale.getDefault(), ":%02d", local.second))
        }
    }

    val timeFontSize = if (showSeconds) 52.sp else 76.sp
    val amPmFontSize = if (showSeconds) 24.sp else 32.sp
    val amPmBottomPadding = if (showSeconds) 10.dp else 14.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = timeFontSize, 
                    fontWeight = FontWeight.Medium, 
                    letterSpacing = 1.sp, 
                    fontFeatureSettings = "tnum"
                ),
                color = ink,
                maxLines = 1,
                softWrap = false,
            )
            if (!use24h) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = meridiem,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = amPmFontSize, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.sp
                    ),
                    color = ink,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(bottom = amPmBottomPadding) 
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        val formatter = remember(home) { 
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy").withZone(home) 
        }
        
        Text(
            text = formatter.format(instant), 
            style = MaterialTheme.typography.titleMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold 
        )
    }
}

@Composable
private fun CityDial(
    cities: List<WorldCity>, 
    nowUtcMillis: Long, 
    measurer: TextMeasurer, 
    modifier: Modifier = Modifier
) {
    val face = MaterialTheme.colorScheme.surfaceContainerHighest
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ghost = MaterialTheme.colorScheme.onSurfaceVariant
    val hourStyle = MaterialTheme.typography.headlineMedium.copy(color = ghost.copy(alpha = 0.45f))
    val pillStyle = MaterialTheme.typography.labelSmall.copy(color = onAccent, fontWeight = FontWeight.SemiBold)

    val hourLabels = remember(measurer, hourStyle) {
        (0 until 12).associateWith { i ->
            if (i % 3 == 0) {
                val label = if (i == 0) "12" else i.toString()
                measurer.measure(label, hourStyle)
            } else null
        }
    }

    Canvas(modifier) {
        val radius = size.minDimension / 2f
        val centerPoint = Offset(size.width / 2f, size.height / 2f)
        
        drawCircle(face, radius = radius, center = centerPoint)
        
        for (i in 0 until 12) {
            val angle = (i / 12f) * 2f * PI.toFloat() - PI.toFloat() / 2f
            val position = Offset(
                x = centerPoint.x + cos(angle) * (radius - 44.dp.toPx()), 
                y = centerPoint.y + sin(angle) * (radius - 44.dp.toPx())
            )
            
            val measuredText = hourLabels[i]
            if (measuredText != null) {
                drawText(
                    textLayoutResult = measuredText, 
                    topLeft = Offset(
                        x = position.x - measuredText.size.width / 2f, 
                        y = position.y - measuredText.size.height / 2f
                    )
                )
            } else {
                drawCircle(accent.copy(alpha = 0.45f), radius = 3.5.dp.toPx(), center = position)
            }
        }
        
        cities.forEach { city ->
            val local = city.timeAt(nowUtcMillis)
            val hours12 = (local.hour % 12) + local.minute / 60f
            val angle = hours12 / 12f * 360f - 90f
            val rad = angle * PI.toFloat() / 180f
            val lineLength = radius - 62.dp.toPx()
            val tip = Offset(
                x = centerPoint.x + cos(rad) * lineLength, 
                y = centerPoint.y + sin(rad) * lineLength
            )
            
            drawLine(accent, start = centerPoint, end = tip, strokeWidth = 2.dp.toPx())
            
            val measuredCityText = measurer.measure(city.city, pillStyle)
            val padH = 7.dp.toPx()
            val w = measuredCityText.size.width + padH * 2
            val h = measuredCityText.size.height + 5.dp.toPx()
            val flip = if (angle.mod(360f) in 90f..270f) 180f else 0f
            
            rotate(degrees = angle + flip, pivot = tip) {
                drawRoundRect(
                    color = accent, 
                    topLeft = Offset(tip.x - w / 2f, tip.y - h / 2f), 
                    size = Size(w, h), 
                    cornerRadius = CornerRadius(h / 2f)
                )
                drawText(
                    textLayoutResult = measuredCityText, 
                    topLeft = Offset(tip.x - measuredCityText.size.width / 2f, tip.y - measuredCityText.size.height / 2f)
                )
            }
        }
        drawCircle(accent, radius = 7.dp.toPx(), center = centerPoint)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CityRow(
    city: WorldCity,
    home: ZoneId,
    nowUtcMillis: Long,
    use24h: Boolean,
    showSeconds: Boolean,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val local = city.timeAt(nowUtcMillis)
    val night = city.isNight(nowUtcMillis)
    
    val timeOnly = buildString {
        if (use24h) {
            append(String.format(Locale.getDefault(), "%02d:%02d", local.hour, local.minute))
        } else {
            val h = (local.hour % 12).takeIf { it != 0 } ?: 12
            append(String.format(Locale.getDefault(), "%02d:%02d", h, local.minute))
        }
        if (showSeconds) {
            append(String.format(Locale.getDefault(), ":%02d", local.second))
        }
    }
    
    val amPmString = if (!use24h) (if (local.hour < 12) "am" else "pm") else ""

    val inst = Instant.ofEpochMilli(nowUtcMillis)
    val diffSeconds = city.zone.rules.getOffset(inst).totalSeconds - home.rules.getOffset(inst).totalSeconds
    val diffString = if (diffSeconds == 0) {
        "Local Time Zone"
    } else {
        val totalMins = kotlin.math.abs(diffSeconds) / 60
        val h = totalMins / 60
        val m = totalMins % 60
        val dir = if (diffSeconds > 0) "Ahead" else "Behind"
        val hStr = if (h > 0) "$h hours " else ""
        val mStr = if (m > 0) "$m Minutes " else ""
        "${hStr}${mStr}$dir"
    }

    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface 
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(percent = 50), 
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(ROW_HEIGHT_DP.dp)
            .combinedClickable(
                onClick = { if (inSelectionMode) onToggleSelect() },
                onLongClick = { onToggleSelect() }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else if (night) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (night) Icons.Outlined.Bedtime else Icons.Outlined.LightMode,
                        contentDescription = if (night) "night" else "daytime",
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (night) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.city,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (city.pinnedAt != null) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pinned",
                            tint = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${city.country.ifBlank { city.region }} |",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$diffString |",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, 
                    modifier = Modifier.basicMarquee() 
                )
                Text(
                    text = "${city.utcCode(nowUtcMillis)} |",
                    style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeOnly,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold, 
                        fontFeatureSettings = "tnum"
                    ), 
                    color = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false,
                )
                if (amPmString.isNotEmpty()) {
                    Text(
                        text = amPmString,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) contentColor else MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
