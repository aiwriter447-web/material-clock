package app.materialclock.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ROW_HEIGHT_DP = 100 

@Composable
fun WorldClockScreen(
    cities: List<WorldCity>,
    home: ZoneId,
    nowUtcMillis: Long,
    settings: WorldClockSettings,
    onRemove: (WorldCity) -> Unit,
    onTogglePin: (WorldCity) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val use24h = when (settings.hourFormat) {
        HourFormat.SYSTEM -> android.text.format.DateFormat.is24HourFormat(LocalContext.current)
        HourFormat.H12 -> false
        HourFormat.H24 -> true
    }
    
    val measurer = rememberTextMeasurer()
    var cityToDelete by remember { mutableStateOf<WorldCity?>(null) }

    cityToDelete?.let { city ->
        AlertDialog(
            onDismissRequest = { cityToDelete = null },
            title = { Text("Remove City") },
            text = { Text("Are you sure you want to remove ${city.city} from your world clock?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove(city)
                        cityToDelete = null
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cityToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
            CityRow(
                city = city,
                home = home,
                nowUtcMillis = nowUtcMillis,
                use24h = use24h,
                showSeconds = settings.showSeconds,
                onDeleteRequest = { cityToDelete = city },
                onTogglePin = { onTogglePin(city) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

private const val MIN_CLOCK_SCALE = 0.45f

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

    val idealTimeSize = 86.sp
    val idealMeridiemSize = 32.sp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val availableWidthPx = with(density) { maxWidth.toPx() }
            val gapPx = with(density) { 8.dp.toPx() }

            val timeStyle = TextStyle(
                fontSize = idealTimeSize, 
                fontWeight = FontWeight.Medium, 
                letterSpacing = 1.sp, 
                fontFeatureSettings = "tnum"
            )
            val meridiemStyle = TextStyle(fontSize = idealMeridiemSize, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)

            val timeWidthPx = measurer.measure(timeText, timeStyle, maxLines = 1, softWrap = false).size.width.toFloat()
            val meridiemWidthPx = if (!use24h) {
                measurer.measure(meridiem, meridiemStyle, maxLines = 1, softWrap = false).size.width.toFloat()
            } else 0f
            val totalWidthPx = timeWidthPx + (if (!use24h) gapPx + meridiemWidthPx else 0f)

            val rawScale = if (totalWidthPx > availableWidthPx) availableWidthPx / totalWidthPx else 1f
            val scale = rawScale.coerceIn(MIN_CLOCK_SCALE, 1f)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = timeText,
                    style = timeStyle.copy(fontSize = (idealTimeSize.value * scale).sp),
                    color = ink,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.alignByBaseline()
                )
                if (!use24h) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = meridiem,
                        style = meridiemStyle.copy(fontSize = (idealMeridiemSize.value * scale).sp),
                        color = ink,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.alignByBaseline()
                    )
                }
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CityRow(
    city: WorldCity,
    home: ZoneId,
    nowUtcMillis: Long,
    use24h: Boolean,
    showSeconds: Boolean,
    onDeleteRequest: () -> Unit,
    onTogglePin: () -> Unit,
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

    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { 
                    onDeleteRequest() 
                    false 
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onTogglePin()
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.25f } // Triggers fast with a short swipe
    )

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = true, 
        enableDismissFromEndToStart = true,  
        backgroundContent = {
            val direction = state.dismissDirection
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(ROW_HEIGHT_DP.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp), 
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete, 
                            contentDescription = "Delete", 
                            tint = MaterialTheme.colorScheme.onErrorContainer, 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            } else if (direction == SwipeToDismissBoxValue.StartToEnd) {
                val isPinned = city.pinnedAt != null
                Surface(
                    color = if (isPinned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(ROW_HEIGHT_DP.dp),
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp), 
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            imageVector = if (isPinned) Icons.Outlined.PushPin else Icons.Filled.PushPin, 
                            contentDescription = if (isPinned) "Unpin" else "Pin", 
                            tint = if (isPinned) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer, 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(percent = 50), 
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(ROW_HEIGHT_DP.dp)
                .semantics { 
                    customActions = listOf(CustomAccessibilityAction("Remove ${city.city}") { onDeleteRequest(); true }) 
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = if (night) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (night) Icons.Outlined.Bedtime else Icons.Outlined.LightMode,
                            contentDescription = if (night) "night" else "daytime",
                            tint = if (night) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = city.city,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (city.pinnedAt != null) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${city.country.ifBlank { city.region }} | ${city.offsetLabel(home, nowUtcMillis)} |\n${city.utcCode(nowUtcMillis)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3, // Allowed 3 lines so UTC code never gets cut off
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timeOnly,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"), 
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                    if (amPmString.isNotEmpty()) {
                        Text(
                            text = amPmString,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
