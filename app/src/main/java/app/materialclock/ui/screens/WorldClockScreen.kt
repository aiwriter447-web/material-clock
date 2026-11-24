package app.materialclock.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import app.materialclock.core.WorldCity
import app.materialclock.data.HourFormat
import app.materialclock.data.WorldClockSettings
import app.materialclock.data.WorldClockStyle
import app.materialclock.ui.DOCK_HEIGHT
import app.materialclock.ui.dockFrostedGlass
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val ROW_HEIGHT_DP = 96
private val CITY_CARD_CORNER_RADIUS = 28.dp

private val WorldClockDeleteColor = Color(0xFFB3261E)

private enum class ContinentRegion {
    ASIA,
    EUROPE,
    NORTH_AMERICA,
    SOUTH_AMERICA,
    AFRICA,
    OCEANIA
}

private data class HomeGeoLocation(
    val continent: ContinentRegion,
    val x: Float,
    val y: Float,
    val label: String
)

private fun resolveHomeGeoLocation(zone: ZoneId): HomeGeoLocation {
    val rawId = zone.id
    val normalizedId = when (rawId.uppercase(Locale.US)) {
        "IST" -> "Asia/Kolkata"
        "JST" -> "Asia/Tokyo"
        "CST" -> "Asia/Shanghai"
        "GMT", "UTC", "UCT", "Z" -> "Europe/London"
        "CET", "ECT" -> "Europe/Paris"
        "EET" -> "Europe/Athens"
        "EST", "US/EASTERN" -> "America/New_York"
        "US/CENTRAL" -> "America/Chicago"
        "MST", "US/MOUNTAIN" -> "America/Denver"
        "PST", "US/PACIFIC" -> "America/Los_Angeles"
        "AET" -> "Australia/Sydney"
        "NST" -> "Pacific/Auckland"
        else -> rawId
    }

    val cityName = normalizedId.substringAfterLast('/').replace('_', ' ')
    val offsetHours = zone.rules.getOffset(Instant.now()).totalSeconds / 3600f

    val southAmericaCities = setOf(
        "Buenos_Aires", "Sao_Paulo", "Lima", "Bogota", "Santiago",
        "Caracas", "Montevideo", "Asuncion", "La_Paz", "Quito", "Guayaquil",
        "Manaus", "Recife", "Fortaleza", "Belem", "Cayenne", "Paramaribo", "Guyana"
    )

    val continent = when {
        normalizedId.startsWith("Asia/") -> ContinentRegion.ASIA
        normalizedId.startsWith("Europe/") -> ContinentRegion.EUROPE
        normalizedId.startsWith("Africa/") -> ContinentRegion.AFRICA
        normalizedId.startsWith("Australia/") || normalizedId.startsWith("Pacific/") -> ContinentRegion.OCEANIA
        normalizedId.startsWith("America/Argentina/") ||
            southAmericaCities.any { normalizedId.endsWith(it) } -> ContinentRegion.SOUTH_AMERICA
        normalizedId.startsWith("America/") ||
            normalizedId.startsWith("Canada/") ||
            normalizedId.startsWith("US/") ||
            normalizedId.startsWith("Mexico/") -> ContinentRegion.NORTH_AMERICA
        normalizedId.startsWith("Indian/") -> ContinentRegion.ASIA
        normalizedId.startsWith("Atlantic/") -> ContinentRegion.EUROPE
        else -> ContinentRegion.ASIA
    }

    val exactCoords: Pair<Float, Float>? = when {
        // Asia
        normalizedId.contains("Kolkata") || normalizedId.contains("Calcutta") -> 0.36f to 0.59f
        normalizedId.contains("Colombo") -> 0.37f to 0.72f
        normalizedId.contains("Kathmandu") -> 0.39f to 0.51f
        normalizedId.contains("Dhaka") -> 0.43f to 0.56f
        normalizedId.contains("Karachi") -> 0.28f to 0.52f
        normalizedId.contains("Kabul") -> 0.27f to 0.44f
        normalizedId.contains("Tashkent") || normalizedId.contains("Almaty") -> 0.31f to 0.36f
        normalizedId.contains("Dubai") || normalizedId.contains("Muscat") -> 0.18f to 0.53f
        normalizedId.contains("Riyadh") || normalizedId.contains("Kuwait") || normalizedId.contains("Qatar") -> 0.13f to 0.53f
        normalizedId.contains("Tehran") -> 0.18f to 0.44f
        normalizedId.contains("Jerusalem") || normalizedId.contains("Beirut") || normalizedId.contains("Amman") -> 0.07f to 0.46f
        normalizedId.contains("Bangkok") || normalizedId.contains("Ho_Chi_Minh") -> 0.54f to 0.65f
        normalizedId.contains("Singapore") || normalizedId.contains("Kuala_Lumpur") -> 0.54f to 0.76f
        normalizedId.contains("Jakarta") -> 0.58f to 0.86f
        normalizedId.contains("Manila") -> 0.73f to 0.65f
        normalizedId.contains("Hong_Kong") || normalizedId.contains("Macau") -> 0.67f to 0.56f
        normalizedId.contains("Taipei") -> 0.73f to 0.55f
        normalizedId.contains("Shanghai") || normalizedId.contains("Beijing") -> 0.70f to 0.45f
        normalizedId.contains("Seoul") -> 0.78f to 0.41f
        normalizedId.contains("Tokyo") || normalizedId.contains("Osaka") -> 0.85f to 0.41f

        // Europe
        normalizedId.contains("London") || normalizedId.contains("Dublin") -> 0.24f to 0.49f
        normalizedId.contains("Lisbon") || normalizedId.contains("Madrid") -> 0.18f to 0.72f
        normalizedId.contains("Paris") || normalizedId.contains("Brussels") || normalizedId.contains("Amsterdam") -> 0.31f to 0.55f
        normalizedId.contains("Berlin") || normalizedId.contains("Zurich") || normalizedId.contains("Vienna") -> 0.43f to 0.53f
        normalizedId.contains("Rome") -> 0.45f to 0.68f
        normalizedId.contains("Stockholm") || normalizedId.contains("Oslo") || normalizedId.contains("Copenhagen") -> 0.46f to 0.34f
        normalizedId.contains("Helsinki") || normalizedId.contains("Warsaw") -> 0.56f to 0.43f
        normalizedId.contains("Athens") || normalizedId.contains("Bucharest") -> 0.58f to 0.71f
        normalizedId.contains("Istanbul") -> 0.66f to 0.69f
        normalizedId.contains("Moscow") || normalizedId.contains("Kiev") || normalizedId.contains("Kyiv") -> 0.73f to 0.42f

        // North America
        normalizedId.contains("New_York") || normalizedId.contains("Toronto") || normalizedId.contains("Montreal") -> 0.65f to 0.47f
        normalizedId.contains("Chicago") || normalizedId.contains("Winnipeg") -> 0.52f to 0.47f
        normalizedId.contains("Denver") || normalizedId.contains("Phoenix") || normalizedId.contains("Edmonton") -> 0.37f to 0.48f
        normalizedId.contains("Los_Angeles") || normalizedId.contains("San_Francisco") || normalizedId.contains("Vancouver") -> 0.25f to 0.48f
        normalizedId.contains("Anchorage") -> 0.14f to 0.23f
        normalizedId.contains("Mexico_City") -> 0.42f to 0.70f

        // South America
        normalizedId.contains("Bogota") || normalizedId.contains("Lima") || normalizedId.contains("Quito") -> 0.33f to 0.32f
        normalizedId.contains("Sao_Paulo") || normalizedId.contains("Rio_de_Janeiro") -> 0.64f to 0.53f
        normalizedId.contains("Buenos_Aires") || normalizedId.contains("Montevideo") -> 0.53f to 0.68f
        normalizedId.contains("Santiago") -> 0.39f to 0.68f

        // Africa
        normalizedId.contains("Cairo") -> 0.61f to 0.22f
        normalizedId.contains("Casablanca") || normalizedId.contains("Algiers") || normalizedId.contains("Tunis") -> 0.30f to 0.17f
        normalizedId.contains("Lagos") || normalizedId.contains("Accra") -> 0.36f to 0.47f
        normalizedId.contains("Nairobi") || normalizedId.contains("Addis_Ababa") -> 0.68f to 0.50f
        normalizedId.contains("Johannesburg") || normalizedId.contains("Cape_Town") -> 0.54f to 0.83f

        // Oceania
        normalizedId.contains("Perth") -> 0.23f to 0.56f
        normalizedId.contains("Darwin") -> 0.42f to 0.31f
        normalizedId.contains("Brisbane") -> 0.62f to 0.47f
        normalizedId.contains("Sydney") || normalizedId.contains("Melbourne") -> 0.59f to 0.60f
        normalizedId.contains("Auckland") || normalizedId.contains("Wellington") -> 0.80f to 0.70f

        else -> null
    }

    val (fx, fy) = exactCoords ?: when (continent) {
        ContinentRegion.ASIA -> {
            val t = ((offsetHours - 2f) / 8f).coerceIn(0.08f, 0.90f)
            t to 0.50f
        }
        ContinentRegion.EUROPE -> {
            val t = ((offsetHours + 1f) / 4.5f).coerceIn(0.18f, 0.82f)
            t to 0.52f
        }
        ContinentRegion.NORTH_AMERICA -> {
            val t = ((offsetHours + 9f) / 5.5f).coerceIn(0.16f, 0.72f)
            t to 0.48f
        }
        ContinentRegion.SOUTH_AMERICA -> {
            val t = ((offsetHours + 5.5f) / 3f).coerceIn(0.30f, 0.68f)
            t to 0.50f
        }
        ContinentRegion.AFRICA -> {
            val t = ((offsetHours + 1f) / 4.5f).coerceIn(0.22f, 0.76f)
            t to 0.48f
        }
        ContinentRegion.OCEANIA -> {
            val t = ((offsetHours - 8f) / 4.5f).coerceIn(0.22f, 0.82f)
            t to 0.54f
        }
    }

    return HomeGeoLocation(
        continent = continent,
        x = fx,
        y = fy,
        label = cityName
    )
}

private fun DrawScope.drawContinentWatermark(
    geo: HomeGeoLocation,
    mapLeft: Float,
    mapTop: Float,
    mapWidth: Float,
    mapHeight: Float,
    fillColor: Color,
    strokeColor: Color,
    pinColor: Color,
    measurer: TextMeasurer? = null,
    labelColor: Color = Color.Transparent,
    showLabel: Boolean = false
) {
    fun px(nx: Float) = mapLeft + nx * mapWidth
    fun py(ny: Float) = mapTop + ny * mapHeight

    fun polygonPath(points: List<Pair<Float, Float>>): Path {
        return Path().apply {
            points.forEachIndexed { idx, (nx, ny) ->
                if (idx == 0) moveTo(px(nx), py(ny))
                else lineTo(px(nx), py(ny))
            }
            close()
        }
    }

    val landmasses: List<Path> = when (geo.continent) {
        ContinentRegion.ASIA -> listOf(
            polygonPath(
                listOf(
                    0.04f to 0.42f,
                    0.12f to 0.35f,
                    0.22f to 0.16f,
                    0.46f to 0.09f,
                    0.74f to 0.11f,
                    0.90f to 0.14f,
                    0.93f to 0.24f,
                    0.83f to 0.27f,
                    0.77f to 0.37f,
                    0.80f to 0.44f,
                    0.75f to 0.42f,
                    0.74f to 0.52f,
                    0.66f to 0.58f,
                    0.59f to 0.67f,
                    0.54f to 0.77f,
                    0.50f to 0.63f,
                    0.44f to 0.56f,
                    0.43f to 0.61f,
                    0.36f to 0.72f,
                    0.30f to 0.56f,
                    0.25f to 0.51f,
                    0.19f to 0.50f,
                    0.22f to 0.58f,
                    0.13f to 0.63f,
                    0.07f to 0.51f
                )
            ),
            // Sri Lanka
            polygonPath(
                listOf(
                    0.38f to 0.72f,
                    0.40f to 0.72f,
                    0.40f to 0.76f,
                    0.38f to 0.76f
                )
            ),
            // Japan
            polygonPath(
                listOf(
                    0.85f to 0.33f,
                    0.88f to 0.36f,
                    0.86f to 0.44f,
                    0.81f to 0.45f,
                    0.83f to 0.39f
                )
            ),
            // Taiwan
            polygonPath(
                listOf(
                    0.72f to 0.54f,
                    0.74f to 0.54f,
                    0.74f to 0.58f,
                    0.72f to 0.58f
                )
            ),
            // Sumatra
            polygonPath(
                listOf(
                    0.48f to 0.76f,
                    0.51f to 0.76f,
                    0.56f to 0.84f,
                    0.53f to 0.85f
                )
            ),
            // Java
            polygonPath(
                listOf(
                    0.56f to 0.87f,
                    0.65f to 0.88f,
                    0.65f to 0.90f,
                    0.56f to 0.89f
                )
            ),
            // Borneo
            polygonPath(
                listOf(
                    0.61f to 0.76f,
                    0.68f to 0.77f,
                    0.67f to 0.84f,
                    0.60f to 0.83f
                )
            ),
            // Philippines
            polygonPath(
                listOf(
                    0.72f to 0.62f,
                    0.75f to 0.63f,
                    0.76f to 0.72f,
                    0.73f to 0.71f
                )
            )
        )

        ContinentRegion.EUROPE -> listOf(
            polygonPath(
                listOf(
                    0.13f to 0.75f,
                    0.17f to 0.64f,
                    0.27f to 0.60f,
                    0.37f to 0.51f,
                    0.42f to 0.44f,
                    0.55f to 0.47f,
                    0.64f to 0.28f,
                    0.74f to 0.20f,
                    0.88f to 0.25f,
                    0.85f to 0.58f,
                    0.74f to 0.66f,
                    0.61f to 0.78f,
                    0.52f to 0.68f,
                    0.49f to 0.78f,
                    0.44f to 0.76f,
                    0.43f to 0.66f,
                    0.33f to 0.69f,
                    0.22f to 0.78f
                )
            ),
            // Scandinavia
            polygonPath(
                listOf(
                    0.37f to 0.36f,
                    0.51f to 0.14f,
                    0.60f to 0.20f,
                    0.50f to 0.41f,
                    0.42f to 0.40f
                )
            ),
            // Great Britain
            polygonPath(
                listOf(
                    0.22f to 0.42f,
                    0.26f to 0.43f,
                    0.28f to 0.54f,
                    0.21f to 0.54f
                )
            ),
            // Ireland
            polygonPath(
                listOf(
                    0.16f to 0.46f,
                    0.19f to 0.46f,
                    0.19f to 0.52f,
                    0.16f to 0.52f
                )
            )
        )

        ContinentRegion.NORTH_AMERICA -> listOf(
            polygonPath(
                listOf(
                    0.09f to 0.22f,
                    0.34f to 0.14f,
                    0.56f to 0.16f,
                    0.58f to 0.28f,
                    0.78f to 0.33f,
                    0.67f to 0.52f,
                    0.62f to 0.66f,
                    0.56f to 0.61f,
                    0.48f to 0.64f,
                    0.46f to 0.76f,
                    0.55f to 0.86f,
                    0.51f to 0.88f,
                    0.35f to 0.70f,
                    0.28f to 0.58f,
                    0.23f to 0.41f,
                    0.16f to 0.28f
                )
            ),
            // Greenland
            polygonPath(
                listOf(
                    0.68f to 0.08f,
                    0.86f to 0.08f,
                    0.82f to 0.24f,
                    0.70f to 0.22f
                )
            )
        )

        ContinentRegion.SOUTH_AMERICA -> listOf(
            polygonPath(
                listOf(
                    0.34f to 0.12f,
                    0.54f to 0.17f,
                    0.75f to 0.34f,
                    0.65f to 0.55f,
                    0.54f to 0.69f,
                    0.45f to 0.89f,
                    0.39f to 0.92f,
                    0.36f to 0.66f,
                    0.27f to 0.36f
                )
            )
        )

        ContinentRegion.AFRICA -> listOf(
            polygonPath(
                listOf(
                    0.23f to 0.14f,
                    0.46f to 0.15f,
                    0.63f to 0.20f,
                    0.73f to 0.37f,
                    0.83f to 0.44f,
                    0.68f to 0.64f,
                    0.62f to 0.79f,
                    0.50f to 0.90f,
                    0.44f to 0.70f,
                    0.42f to 0.50f,
                    0.18f to 0.45f,
                    0.16f to 0.29f
                )
            ),
            // Madagascar
            polygonPath(
                listOf(
                    0.72f to 0.71f,
                    0.76f to 0.71f,
                    0.75f to 0.83f,
                    0.71f to 0.83f
                )
            )
        )

        ContinentRegion.OCEANIA -> listOf(
            polygonPath(
                listOf(
                    0.19f to 0.43f,
                    0.32f to 0.31f,
                    0.44f to 0.28f,
                    0.56f to 0.26f,
                    0.65f to 0.51f,
                    0.55f to 0.66f,
                    0.39f to 0.58f,
                    0.22f to 0.61f
                )
            ),
            // Tasmania
            polygonPath(
                listOf(
                    0.53f to 0.71f,
                    0.58f to 0.71f,
                    0.57f to 0.76f,
                    0.53f to 0.76f
                )
            ),
            // New Zealand North
            polygonPath(
                listOf(
                    0.79f to 0.65f,
                    0.83f to 0.67f,
                    0.81f to 0.73f,
                    0.78f to 0.71f
                )
            ),
            // New Zealand South
            polygonPath(
                listOf(
                    0.75f to 0.75f,
                    0.79f to 0.76f,
                    0.74f to 0.84f,
                    0.71f to 0.82f
                )
            )
        )
    }

    landmasses.forEach { path ->
        drawPath(path = path, color = fillColor)
        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = 1.25.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    val pinCenter = Offset(px(geo.x), py(geo.y))

    // Soft radar halo around selected home city
    drawCircle(
        color = pinColor.copy(alpha = pinColor.alpha * 0.22f),
        radius = 18.dp.toPx(),
        center = pinCenter
    )
    drawCircle(
        color = pinColor.copy(alpha = pinColor.alpha * 0.45f),
        radius = 9.dp.toPx(),
        center = pinCenter,
        style = Stroke(width = 1.2.dp.toPx())
    )
    drawCircle(
        color = pinColor,
        radius = 4.dp.toPx(),
        center = pinCenter
    )

    if (showLabel && measurer != null && geo.label.isNotBlank()) {
        val textLayout = measurer.measure(
            text = geo.label,
            style = androidx.compose.ui.text.TextStyle(
                color = labelColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        )
        val labelX = (pinCenter.x + 11.dp.toPx())
            .coerceAtMost(mapLeft + mapWidth - textLayout.size.width - 8.dp.toPx())
        val labelY = pinCenter.y - textLayout.size.height / 2f
        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(labelX, labelY)
        )
    }
}

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
        HourFormat.SYSTEM ->
            android.text.format.DateFormat.is24HourFormat(LocalContext.current)

        HourFormat.H12 -> false
        HourFormat.H24 -> true
    }

    val measurer = rememberTextMeasurer()
    val homeGeo = remember(home) { resolveHomeGeoLocation(home) }

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
                    shadowElevation = 1.dp
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

    val mapFillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.065f)
    val mapStrokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val mapPinColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val mapLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val wide = maxWidth > maxHeight || maxWidth >= 600.dp

        // Background Continent Watermark + Home City Pin
        Canvas(modifier = Modifier.fillMaxSize()) {
            val availW = size.width - 24.dp.toPx()
            val availH = if (wide) size.height * 0.78f else size.height * 0.44f
            val side = minOf(availW, availH)
            val left = (size.width - side) / 2f
            val top = if (wide) {
                (size.height - side) / 2f
            } else {
                contentPadding.calculateTopPadding().toPx() + 8.dp.toPx()
            }

            drawContinentWatermark(
                geo = homeGeo,
                mapLeft = left,
                mapTop = top,
                mapWidth = side,
                mapHeight = side,
                fillColor = mapFillColor,
                strokeColor = mapStrokeColor,
                pinColor = mapPinColor,
                measurer = measurer,
                labelColor = mapLabelColor,
                showLabel = true
            )
        }

        val headerBlock: @Composable (Dp?) -> Unit = { dialSize ->
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
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            ),
                    )
                } else {
                    CityDial(
                        cities = cities,
                        homeGeo = homeGeo,
                        nowUtcMillis = nowUtcMillis,
                        measurer = measurer,
                        modifier = if (dialSize != null) {
                            Modifier.size(dialSize)
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 54.dp,
                                    end = 54.dp,
                                    top = 4.dp,
                                    bottom = 12.dp
                                )
                                .aspectRatio(1f)
                        },
                    )
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
                    modifier = Modifier.animateItem(),
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
                    val dial = minOf(maxWidth - 32.dp, maxHeight - 16.dp).coerceAtLeast(120.dp)
                    headerBlock(dial)
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
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
                contentPadding = contentPadding,
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
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                ) +
                slideInVertically(
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) { it } +
                scaleIn(
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.88f
                ),
            exit =
                fadeOut(spring()) +
                slideOutVertically(
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy
                    )
                ) { it } +
                scaleOut(
                    spring(),
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
        targetValue = if (isPressed) 0.80f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
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
        val baseTime = if (showSeconds) 52f else 76f
        val baseAmPm = if (showSeconds) 24f else 32f
        val fontScale = LocalDensity.current.fontScale
        val units = timeText.length * 0.6f + if (use24h) 0f else 0.5f
        val fitSp = (maxWidth.value - 16f) / (fontScale * units)
        val shrink = (minOf(baseTime, fitSp) / baseTime).coerceIn(0.4f, 1f)
        val timeFontSize = (baseTime * shrink).sp
        val amPmFontSize = (baseAmPm * shrink).sp
        val amPmBottomPadding = if (showSeconds) 10.dp else 14.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
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

            Spacer(Modifier.height(16.dp))

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
    homeGeo: HomeGeoLocation,
    nowUtcMillis: Long,
    measurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val face = MaterialTheme.colorScheme.surfaceContainerHighest
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ghost = MaterialTheme.colorScheme.onSurfaceVariant

    val hourStyle = MaterialTheme.typography.headlineMedium.copy(
        color = ghost.copy(alpha = 0.45f),
        fontWeight = FontWeight(450)
    )

    val pillStyle = MaterialTheme.typography.labelMedium.copy(
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

        // Subtle continent watermark + home city pin inside the analog clock dial
        val dialClip = Path().apply {
            addOval(
                Rect(
                    left = centerPoint.x - radius,
                    top = centerPoint.y - radius,
                    right = centerPoint.x + radius,
                    bottom = centerPoint.y + radius
                )
            )
        }

        clipPath(dialClip) {
            val mapSpan = radius * 1.48f
            drawContinentWatermark(
                geo = homeGeo,
                mapLeft = centerPoint.x - mapSpan / 2f,
                mapTop = centerPoint.y - mapSpan / 2f,
                mapWidth = mapSpan,
                mapHeight = mapSpan,
                fillColor = accent.copy(alpha = 0.075f),
                strokeColor = accent.copy(alpha = 0.16f),
                pinColor = accent.copy(alpha = 0.55f),
                showLabel = false
            )
        }

        for (i in 0 until 12) {
            val angle =
                (i / 12f) * 2f * PI.toFloat() -
                    PI.toFloat() / 2f

            val position = Offset(
                x = centerPoint.x +
                    cos(angle) * (radius - 44.dp.toPx()),
                y = centerPoint.y +
                    sin(angle) * (radius - 44.dp.toPx())
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
                    radius = 3.5.dp.toPx(),
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
            val lineLength = radius - 62.dp.toPx()

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

            val padH = 7.dp.toPx()
            val w = measuredCityText.size.width + padH * 2
            val h = measuredCityText.size.height + 5.dp.toPx()

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
            radius = 7.dp.toPx(),
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
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cityRowScale"
    )

    val targetContainer =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
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
        spring(stiffness = Spring.StiffnessLow),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetContent,
        spring(stiffness = Spring.StiffnessLow),
        label = "contentColor"
    )

    val timeTextColor by animateColorAsState(
        targetTimeColor,
        spring(stiffness = Spring.StiffnessLow),
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
        spring(stiffness = Spring.StiffnessLow),
        label = "badgeBg"
    )

    val badgeTint by animateColorAsState(
        targetBadgeTint,
        spring(stiffness = Spring.StiffnessLow),
        label = "badgeTint"
    )

    val cardBorderColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
        }

    val timeOnly = buildString {
        if (use24h) {
            append(
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    local.hour,
                    local.minute
                )
            )
        } else {
            val h =
                (local.hour % 12).takeIf { it != 0 } ?: 12

            append(
                String.format(
                    Locale.getDefault(),
                    "%d:%02d",
                    h,
                    local.minute
                )
            )
        }

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
            if (local.hour < 12) "am" else "pm"
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
            val mStr = if (m > 0) "$m Minutes " else ""

            "${hStr}${mStr}$dir"
        }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(CITY_CARD_CORNER_RADIUS),
        border = BorderStroke(1.dp, cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(min = if (LocalConfiguration.current.screenHeightDp < 480) 78.dp else ROW_HEIGHT_DP.dp)
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
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = badgeBg,
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = night,
                        transitionSpec = {
                            (
                                fadeIn(tween(220)) +
                                scaleIn(
                                    spring(
                                        dampingRatio =
                                            Spring.DampingRatioMediumBouncy
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
                        start = 16.dp,
                        end = 8.dp
                    ),
                verticalArrangement = Arrangement.Center,
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

                val compactRows = LocalConfiguration.current.screenHeightDp < 480
                Text(
                    text = if (compactRows) {
                        "${city.country.ifBlank { city.region }} | ${city.utcCode(nowUtcMillis)}"
                    } else {
                        "${city.country.ifBlank { city.region }} | ${city.utcCode(nowUtcMillis)}"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    color = if (isSelected) {
                        contentColor
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
                        contentColor
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = timeOnly,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum"
                    ),
                    color = timeTextColor,
                    maxLines = 1,
                    softWrap = false,
                )

                if (amPmString.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = amPmString,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = timeTextColor,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}
