package app.materialclock.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.Xml
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.PathParser
import app.materialclock.core.WorldCity
import app.materialclock.data.HourFormat
import app.materialclock.data.WorldClockSettings
import app.materialclock.data.WorldClockStyle
import app.materialclock.ui.DOCK_HEIGHT
import app.materialclock.ui.dockFrostedGlass
import org.xmlpull.v1.XmlPullParser
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Matched directly to Alarm Card shape & dimensions
private val CITY_CARD_CORNER_RADIUS = 28.dp
private val CITY_CARD_MIN_HEIGHT = 112.dp
private val CITY_ROW_HORIZONTAL_PADDING = 18.dp
private val CITY_ROW_VERTICAL_PADDING = 16.dp
private val CITY_LIST_HORIZONTAL_MARGIN = 16.dp

private val WorldClockDeleteColor = Color(0xFFB3261E)

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
    val context = LocalContext.current
    val use24h = when (settings.hourFormat) {
        HourFormat.SYSTEM ->
            android.text.format.DateFormat.is24HourFormat(context)

        HourFormat.H12 -> false
        HourFormat.H24 -> true
    }

    val measurer = rememberTextMeasurer()

    var selectedCities by remember {
        mutableStateOf(emptySet<ZoneId>())
    }

    val inSelectionMode = selectedCities.isNotEmpty()

    var showBatchDeleteConfirm by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(inSelectionMode) {
        onSelectionChange(inSelectionMode)
    }

    BackHandler(enabled = inSelectionMode) {
        selectedCities = emptySet()
    }

    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showBatchDeleteConfirm = false
            },
            title = {
                Text(
                    text = "Delete Cities?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete the selected ${selectedCities.size} cities?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Surface(
                    onClick = {
                        onDeleteSelected(selectedCities)
                        selectedCities = emptySet()
                        showBatchDeleteConfirm = false
                    },
                    shape = CircleShape,
                    color = WorldClockDeleteColor,
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

    val mapFillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight || maxWidth >= 600.dp

        ThemedWorldMap(
            mapColor = mapFillColor,
            modifier = Modifier.fillMaxSize()
        )

        val headerBlock: @Composable (Dp?) -> Unit = { dialSize ->
            Crossfade(
                targetState = settings.style,
                animationSpec = tween(durationMillis = 420),
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
                            .padding(
                                top = 12.dp,
                                bottom = 12.dp
                            ),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CityDial(
                            cities = cities,
                            nowUtcMillis = nowUtcMillis,
                            measurer = measurer,
                            modifier = if (dialSize != null) {
                                Modifier.size(dialSize * 0.88f)
                            } else {
                                Modifier
                                    .fillMaxWidth(0.88f)
                                    .padding(
                                        start = 24.dp,
                                        end = 24.dp,
                                        top = 4.dp,
                                        bottom = 12.dp
                                    )
                                    .aspectRatio(1f)
                            },
                        )
                    }
                }
            }
        }

        val cityItems: LazyListScope.() -> Unit = {
            items(
                items = cities,
                key = { it.zone.id },
                contentType = { "city" }
            ) { city ->
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
                        selectedCities =
                            if (isSelected) {
                                selectedCities - city.zone
                            } else {
                                selectedCities + city.zone
                            }
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
                        placementSpec = spring(dampingRatio = 0.82f, stiffness = 360f),
                        fadeOutSpec = spring(dampingRatio = 0.86f, stiffness = 380f)
                    ),
                )
            }
        }

        if (wide) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(
                            top = contentPadding.calculateTopPadding(),
                            bottom = 16.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val dial = minOf(maxWidth - 24.dp, maxHeight - 16.dp).coerceAtLeast(120.dp)
                    headerBlock(dial)
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding() + 10.dp,
                        bottom = contentPadding.calculateBottomPadding(),
                        start = 8.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    cityItems()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                    start = CITY_LIST_HORIZONTAL_MARGIN,
                    end = CITY_LIST_HORIZONTAL_MARGIN
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(
                    key = "main-clock",
                    contentType = "header"
                ) {
                    headerBlock(null)
                }
                cityItems()
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
                        selectedCities.size == cities.size

                    ToolbarIconButton(
                        onClick = {
                            selectedCities =
                                if (allSelected) {
                                    emptySet()
                                } else {
                                    cities.map { it.zone }.toSet()
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
                            selectedCities.forEach { zone ->
                                cities.find { it.zone == zone }?.let {
                                    if (it.pinnedAt == null) {
                                        onTogglePin(it)
                                    }
                                }
                            }
                            selectedCities = emptySet()
                        },
                        icon = Icons.Rounded.PushPin,
                        contentDescription = "Pin"
                    )

                    ToolbarIconButton(
                        onClick = {
                            selectedCities.forEach { zone ->
                                cities.find { it.zone == zone }?.let {
                                    if (it.pinnedAt != null) {
                                        onTogglePin(it)
                                    }
                                }
                            }
                            selectedCities = emptySet()
                        },
                        icon = Icons.Outlined.PushPin,
                        contentDescription = "Unpin"
                    )

                    ToolbarIconButton(
                        onClick = {
                            showBatchDeleteConfirm = true
                        },
                        icon = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = WorldClockDeleteColor
                    )
                }
            }
        }
    }
}

private data class ParsedSvgMap(
    val width: Float,
    val height: Float,
    val paths: List<android.graphics.Path>
)

private fun parseSvgPathsFromAssets(context: android.content.Context, assetName: String): ParsedSvgMap {
    return runCatching {
        context.assets.open(assetName).use { inputStream ->
            val parser = Xml.newPullParser().apply {
                setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                setInput(inputStream, null)
            }
            var width = 5365f
            var height = 2796f
            val paths = mutableListOf<android.graphics.Path>()

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "svg" -> {
                            parser.getAttributeValue(null, "width")?.let { w ->
                                w.filter { it.isDigit() || it == '.' }.toFloatOrNull()?.let { width = it }
                            }
                            parser.getAttributeValue(null, "height")?.let { h ->
                                h.filter { it.isDigit() || it == '.' }.toFloatOrNull()?.let { height = it }
                            }
                            if (parser.getAttributeValue(null, "width") == null) {
                                parser.getAttributeValue(null, "viewBox")?.split(Regex("[,\\s]+"))?.let { parts ->
                                    if (parts.size == 4) {
                                        parts[2].toFloatOrNull()?.let { width = it }
                                        parts[3].toFloatOrNull()?.let { height = it }
                                    }
                                }
                            }
                        }
                        "path" -> {
                            val d = parser.getAttributeValue(null, "d")
                            if (!d.isNullOrBlank()) {
                                runCatching { PathParser.createPathFromPathData(d) }.getOrNull()?.let {
                                    paths.add(it)
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            ParsedSvgMap(
                width = width.coerceAtLeast(1f),
                height = height.coerceAtLeast(1f),
                paths = paths
            )
        }
    }.getOrElse {
        ParsedSvgMap(width = 5365f, height = 2796f, paths = emptyList())
    }
}

@Composable
private fun ThemedWorldMap(
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    mapColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
) {
    val context = LocalContext.current
    val parsedSvg = remember {
        parseSvgPathsFromAssets(context, "World_map_blank_gmt.svg")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val widthPx = size.width.toInt().coerceAtLeast(1)
            val heightPx = size.height.toInt().coerceAtLeast(1)

            val picture = Picture()
            val picCanvas = picture.beginRecording(widthPx, heightPx)

            val scaleX = widthPx.toFloat() / parsedSvg.width
            val scaleY = heightPx.toFloat() / parsedSvg.height
            picCanvas.scale(scaleX, scaleY)

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = mapColor.toArgb()
            }
            parsedSvg.paths.forEach { path ->
                picCanvas.drawPath(path, fillPaint)
            }
            picture.endRecording()

            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val bitmapCanvas = Canvas(bitmap)
            bitmapCanvas.drawPicture(picture)

            val tintedBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val tintedCanvas = Canvas(tintedBitmap)
            val tintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = PorterDuffColorFilter(mapColor.toArgb(), PorterDuff.Mode.SRC_IN)
            }
            tintedCanvas.drawBitmap(bitmap, 0f, 0f, tintPaint)

            drawImage(image = tintedBitmap.asImageBitmap())
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
    val interactionSource = remember {
        MutableInteractionSource()
    }

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

    val hour = if (use24h) {
        local.hour
    } else {
        (local.hour % 12).takeIf { it != 0 } ?: 12
    }

    val meridiem = if (local.hour < 12) "AM" else "PM"
    val ink = MaterialTheme.colorScheme.onSurface

    val timeText = buildString {
        append(
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hour,
                local.minute
            )
        )

        if (showSeconds) {
            append(
                String.format(
                    Locale.getDefault(),
                    ":%02d",
                    local.second
                )
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val baseTime = if (showSeconds) 57f else 84f
        val baseAmPm = if (showSeconds) 26f else 35f
        val fontScale = LocalDensity.current.fontScale
        val units = timeText.length * 0.6f + if (use24h) 0f else 0.5f
        val fitSp = (maxWidth.value - 16f) / (fontScale * units)
        val shrink = (minOf(baseTime, fitSp) / baseTime).coerceIn(0.4f, 1f)
        val timeFontSize = (baseTime * shrink).sp
        val amPmFontSize = (baseAmPm * shrink).sp
        val amPmBottomPadding = if (showSeconds) 11.dp else 15.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
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
                        fontWeight = FontWeight(450),
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
                            fontWeight = FontWeight(450),
                            letterSpacing = 1.sp
                        ),
                        color = ink,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(
                            bottom = amPmBottomPadding
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val formatter = remember(home) {
                DateTimeFormatter
                    .ofPattern("EEEE, d MMMM yyyy")
                    .withZone(home)
            }

            Text(
                text = formatter.format(instant),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight(450)
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CityDial(
    cities: List<WorldCity>,
    nowUtcMillis: Long,
    measurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val face = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f)
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ghost = MaterialTheme.colorScheme.onSurfaceVariant

    val hourStyle = MaterialTheme.typography.titleLarge.copy(
        color = ghost.copy(alpha = 0.45f),
        fontWeight = FontWeight(450)
    )

    val pillStyle = MaterialTheme.typography.labelSmall.copy(
        color = onAccent,
        fontWeight = FontWeight(450)
    )

    val hourLabels = remember(measurer, hourStyle) {
        (0 until 12).associateWith { i ->
            if (i % 3 == 0) {
                val label = if (i == 0) "12" else i.toString()
                measurer.measure(label, hourStyle)
            } else {
                null
            }
        }
    }

    Canvas(modifier) {
        val radius = size.minDimension / 2f
        val centerPoint = Offset(
            size.width / 2f,
            size.height / 2f
        )

        drawCircle(
            face,
            radius = radius,
            center = centerPoint
        )

        for (i in 0 until 12) {
            val angle =
                (i / 12f) * 2f * PI.toFloat() -
                    PI.toFloat() / 2f

            val position = Offset(
                x = centerPoint.x +
                    cos(angle) * (radius - 35.dp.toPx()),
                y = centerPoint.y +
                    sin(angle) * (radius - 35.dp.toPx())
            )

            val measuredText = hourLabels[i]

            if (measuredText != null) {
                drawText(
                    textLayoutResult = measuredText,
                    topLeft = Offset(
                        x = position.x -
                            measuredText.size.width / 2f,
                        y = position.y -
                            measuredText.size.height / 2f
                    )
                )
            } else {
                drawCircle(
                    accent.copy(alpha = 0.45f),
                    radius = 3.dp.toPx(),
                    center = position
                )
            }
        }

        cities.forEach { city ->
            val local = city.timeAt(nowUtcMillis)
            val hours12 =
                (local.hour % 12) + local.minute / 60f

            val angle = hours12 / 12f * 360f - 90f
            val rad = angle * PI.toFloat() / 180f
            val lineLength = radius - 48.dp.toPx()

            val tip = Offset(
                x = centerPoint.x + cos(rad) * lineLength,
                y = centerPoint.y + sin(rad) * lineLength
            )

            drawLine(
                accent,
                start = centerPoint,
                end = tip,
                strokeWidth = 2.dp.toPx()
            )

            val measuredCityText =
                measurer.measure(city.city, pillStyle)

            val padH = 6.dp.toPx()
            val w = measuredCityText.size.width + padH * 2
            val h = measuredCityText.size.height + 4.dp.toPx()

            val flip =
                if (angle.mod(360f) in 90f..270f) 180f else 0f

            rotate(
                degrees = angle + flip,
                pivot = tip
            ) {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(
                        tip.x - w / 2f,
                        tip.y - h / 2f
                    ),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(h / 2f)
                )

                drawText(
                    textLayoutResult = measuredCityText,
                    topLeft = Offset(
                        tip.x - measuredCityText.size.width / 2f,
                        tip.y - measuredCityText.size.height / 2f
                    )
                )
            }
        }

        drawCircle(
            accent,
            radius = 6.dp.toPx(),
            center = centerPoint
        )
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

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 440f
        ),
        label = "cityRowScale"
    )

    val targetContainer =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.74f)
        }

    val targetContent =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    val targetTimeColor =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.primary
        }

    val containerColor by animateColorAsState(
        targetContainer,
        spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetContent,
        spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "contentColor"
    )

    val timeTextColor by animateColorAsState(
        targetTimeColor,
        spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "timeTextColor"
    )

    val targetBadgeBg =
        if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else if (night) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

    val targetBadgeTint =
        if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else if (night) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

    val badgeBg by animateColorAsState(
        targetBadgeBg,
        spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "badgeBg"
    )

    val badgeTint by animateColorAsState(
        targetBadgeTint,
        spring(dampingRatio = 0.85f, stiffness = 360f),
        label = "badgeTint"
    )

    val cardBorderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
        }

    val hourValue = if (use24h) {
        local.hour
    } else {
        (local.hour % 12).takeIf { it != 0 } ?: 12
    }

    val displayTimeText = buildString {
        append(
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                hourValue,
                local.minute
            )
        )
        if (showSeconds) {
            append(
                String.format(
                    Locale.getDefault(),
                    ":%02d",
                    local.second
                )
            )
        }
    }

    val amPmString =
        if (!use24h) {
            if (local.hour < 12) "AM" else "PM"
        } else {
            ""
        }

    val inst = Instant.ofEpochMilli(nowUtcMillis)

    val diffSeconds =
        city.zone.rules.getOffset(inst).totalSeconds -
            home.rules.getOffset(inst).totalSeconds

    val diffString =
        if (diffSeconds == 0) {
            "Local Time Zone"
        } else {
            val totalMins = kotlin.math.abs(diffSeconds) / 60
            val h = totalMins / 60
            val m = totalMins % 60
            val dir = if (diffSeconds > 0) "Ahead" else "Behind"

            val hStr = if (h > 0) "$h hours " else ""
            val mStr = if (m > 0) "$m min " else ""

            "${hStr}${mStr}$dir".trim()
        }

    val countryAndUtc = "${city.country.ifBlank { city.region }} | ${city.utcCode(nowUtcMillis)}"

    val compactRows = LocalConfiguration.current.screenHeightDp < 480
    val rowVPad = if (compactRows) 12.dp else CITY_ROW_VERTICAL_PADDING
    val minCardHeight = if (compactRows) 92.dp else CITY_CARD_MIN_HEIGHT

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(CITY_CARD_CORNER_RADIUS),
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minCardHeight)
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
                    }
                },
                onLongClick = {
                    onToggleSelect()
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CITY_ROW_HORIZONTAL_PADDING,
                    vertical = rowVPad
                ),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                color = badgeBg,
                shape = CircleShape,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = night,
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
                        label = "sunMoonAnim"
                    ) { isNightTime ->
                        Icon(
                            imageVector = if (isNightTime) {
                                Icons.Outlined.Bedtime
                            } else {
                                Icons.Outlined.LightMode
                            },
                            contentDescription = if (isNightTime) {
                                "night"
                            } else {
                                "daytime"
                            },
                            tint = badgeTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 14.dp,
                        end = 10.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.city,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
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
                            tint = if (isSelected) {
                                contentColor
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = countryAndUtc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isSelected) {
                        contentColor.copy(alpha = 0.90f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = diffString,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        contentColor.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = displayTimeText,
                    style = (
                        if (showSeconds) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.headlineMedium
                        }
                    ).copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = timeTextColor,
                    maxLines = 1,
                    softWrap = false,
                )

                if (amPmString.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = amPmString,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = timeTextColor.copy(alpha = 0.85f),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
