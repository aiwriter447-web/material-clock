package app.materialclock.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.materialclock.data.HourFormat
import app.materialclock.data.WorldClockSettings
import app.materialclock.data.WorldClockStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.materialclock.core.WorldCity
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals
import app.materialclock.ui.theme.CapText
import java.time.ZoneId
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ROW_HEIGHT_DP = 128

@Composable
fun WorldClockScreen(
    cities: List<WorldCity>,
    home: ZoneId,
    nowUtcMillis: Long,
    settings: WorldClockSettings,
    onRemove: (WorldCity) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val use24h = when (settings.hourFormat) {
        HourFormat.SYSTEM -> android.text.format.DateFormat.is24HourFormat(LocalContext.current)
        HourFormat.H12 -> false
        HourFormat.H24 -> true
    }
    val measurer = rememberTextMeasurer()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        item {
            if (settings.style == WorldClockStyle.DIGITAL) {
                HomeDigitalClock(
                    home = home,
                    nowUtcMillis = nowUtcMillis,
                    use24h = use24h,
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
        items(cities, key = { it.zone.id }) { city ->
            CityRow(
                city = city,
                home = home,
                nowUtcMillis = nowUtcMillis,
                use24h = use24h,
                showSeconds = settings.showSeconds,
                onRemove = { onRemove(city) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

/** 
 * A large central digital clock for the Home time, matching the Google Clock aesthetic 
 * instead of displaying a grid of world cities. 
 */
@Composable
private fun HomeDigitalClock(
    home: ZoneId,
    nowUtcMillis: Long,
    use24h: Boolean,
    modifier: Modifier = Modifier,
) {
    val instant = Instant.ofEpochMilli(nowUtcMillis)
    val local = instant.atZone(home)
    val hour = if (use24h) local.hour else ((local.hour % 12).takeIf { it != 0 } ?: 12)
    val meridiem = if (local.hour < 12) "AM" else "PM"
    val ink = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Numerals(
                text = if (use24h) "%02d:%02d".format(hour, local.minute) else "%d:%02d".format(hour, local.minute),
                capHeight = 72.dp,
                color = ink,
                width = ClockFace.CONDENSED,
                weight = ClockFace.WEIGHT_ON,
                tracking = ClockFace.CONDENSED_TRACKING,
            )
            if (!use24h) {
                Spacer(Modifier.width(8.dp))
                CapText(
                    text = meridiem,
                    capHeight = 24.dp,
                    color = ink,
                    tracking = ClockFace.CONDENSED_TRACKING,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        val formatter = remember(home) { DateTimeFormatter.ofPattern("EEE, d MMM").withZone(home) }
        Text(
            text = formatter.format(instant),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CityDial(
    cities: List<WorldCity>,
    nowUtcMillis: Long,
    measurer: TextMeasurer,
    modifier: Modifier = Modifier,
) {
    val face = MaterialTheme.colorScheme.surfaceContainerHighest
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ghost = MaterialTheme.colorScheme.onSurfaceVariant

    val hourStyle = MaterialTheme.typography.headlineMedium.copy(color = ghost.copy(alpha = .45f))
    val pillStyle = MaterialTheme.typography.labelSmall.copy(
        color = onAccent,
        fontWeight = FontWeight.SemiBold,
    )

    Canvas(modifier) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(face, radius = r, center = c)

        for (i in 0 until 12) {
            val a = (i / 12f) * 2f * PI.toFloat() - PI.toFloat() / 2f
            val p = Offset(c.x + cos(a) * (r - 44.dp.toPx()), c.y + sin(a) * (r - 44.dp.toPx()))
            if (i % 3 == 0) {
                val label = if (i == 0) "12" else i.toString()
                val m = measurer.measure(label, hourStyle)
                drawText(m, topLeft = Offset(p.x - m.size.width / 2f, p.y - m.size.height / 2f))
            } else {
                drawCircle(accent.copy(alpha = .45f), radius = 3.5.dp.toPx(), center = p)
            }
        }

        cities.forEach { city ->
            val local = city.timeAt(nowUtcMillis)
            val hours12 = (local.hour % 12) + local.minute / 60f
            val angle = hours12 / 12f * 360f - 90f
            val rad = angle * PI.toFloat() / 180f
            val len = r - 62.dp.toPx()
            val tip = Offset(c.x + cos(rad) * len, c.y + sin(rad) * len)

            drawLine(accent, start = c, end = tip, strokeWidth = 2.dp.toPx())

            val m = measurer.measure(city.city, pillStyle)
            val padH = 7.dp.toPx()
            val w = m.size.width + padH * 2
            val h = m.size.height + 5.dp.toPx()
            val flip = if (angle.mod(360f) in 90f..270f) 180f else 0f
            rotate(degrees = angle + flip, pivot = tip) {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(tip.x - w / 2f, tip.y - h / 2f),
                    size = Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2f),
                )
                drawText(
                    m,
                    topLeft = Offset(tip.x - m.size.width / 2f, tip.y - m.size.height / 2f),
                )
            }
        }
        drawCircle(accent, radius = 7.dp.toPx(), center = c)
    }
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CityRow(
    city: WorldCity,
    home: ZoneId,
    nowUtcMillis: Long,
    use24h: Boolean,
    showSeconds: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val local = city.timeAt(nowUtcMillis)
    val night = city.isNight(nowUtcMillis)

    val state = remember(city.zone.id) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = { distance: Float -> distance * 0.5f },
        )
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        onDismiss = { onRemove() },
        backgroundContent = {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(ROW_HEIGHT_DP.dp),
            ) {
                Box(Modifier.fillMaxSize().padding(end = 32.dp), Alignment.CenterEnd) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp),
                    )
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
                    customActions = listOf(
                        CustomAccessibilityAction("Remove ${city.city}") { onRemove(); true }
                    )
                },
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = if (night) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(51.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (night) Icons.Outlined.Bedtime else Icons.Outlined.LightMode,
                            contentDescription = if (night) "night" else "daytime",
                            tint = if (night) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).padding(start = 9.6.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        city.city,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${city.country.ifBlank { city.region }} | ${city.offsetLabel(home, nowUtcMillis)} | ${city.utcCode(nowUtcMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                
                // Digital clock look added to the side of the World Clock List 
                val hour = if (use24h) local.hour else ((local.hour % 12).takeIf { it != 0 } ?: 12)
                val timeText = buildString {
                    if (use24h) {
                        append("%02d:%02d".format(hour, local.minute))
                    } else {
                        append("%d:%02d".format(hour, local.minute))
                    }
                    if (showSeconds) append(":%02d".format(local.second))
                }
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Numerals(
                        text = timeText,
                        capHeight = if (showSeconds) 24.dp else 36.dp,
                        color = MaterialTheme.colorScheme.primary,
                        width = ClockFace.CONDENSED,
                        weight = ClockFace.WEIGHT_ON,
                        tracking = ClockFace.CONDENSED_TRACKING,
                    )
                    if (!use24h) {
                        Spacer(Modifier.width(4.dp))
                        CapText(
                            text = if (local.hour < 12) "AM" else "PM",
                            capHeight = if (showSeconds) 10.dp else 14.dp,
                            color = MaterialTheme.colorScheme.primary,
                            tracking = ClockFace.CONDENSED_TRACKING,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
