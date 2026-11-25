package app.materialclock.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

enum class AtlasContinent {
    ASIA,
    EUROPE,
    NORTH_AMERICA,
    SOUTH_AMERICA,
    AFRICA,
    OCEANIA
}

private data class GeoBounds(
    val minLon: Float,
    val maxLon: Float,
    val minLat: Float,
    val maxLat: Float
)

data class AtlasHomeLocation(
    val continent: AtlasContinent,
    val lat: Float,
    val lon: Float,
    val label: String
)

private fun boundsForContinent(continent: AtlasContinent): GeoBounds = when (continent) {
    AtlasContinent.ASIA -> GeoBounds(minLon = 25f, maxLon = 152f, minLat = -12f, maxLat = 66f)
    AtlasContinent.EUROPE -> GeoBounds(minLon = -14f, maxLon = 50f, minLat = 33f, maxLat = 72f)
    AtlasContinent.NORTH_AMERICA -> GeoBounds(minLon = -168f, maxLon = -50f, minLat = 7f, maxLat = 75f)
    AtlasContinent.SOUTH_AMERICA -> GeoBounds(minLon = -86f, maxLon = -32f, minLat = -58f, maxLat = 15f)
    AtlasContinent.AFRICA -> GeoBounds(minLon = -20f, maxLon = 54f, minLat = -37f, maxLat = 39f)
    AtlasContinent.OCEANIA -> GeoBounds(minLon = 108f, maxLon = 180f, minLat = -49f, maxLat = 2f)
}

fun resolveAtlasHomeLocation(zone: ZoneId): AtlasHomeLocation {
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
        normalizedId.startsWith("Asia/") -> AtlasContinent.ASIA
        normalizedId.startsWith("Europe/") -> AtlasContinent.EUROPE
        normalizedId.startsWith("Africa/") -> AtlasContinent.AFRICA
        normalizedId.startsWith("Australia/") || normalizedId.startsWith("Pacific/") -> AtlasContinent.OCEANIA
        normalizedId.startsWith("America/Argentina/") ||
            southAmericaCities.any { normalizedId.endsWith(it) } -> AtlasContinent.SOUTH_AMERICA
        normalizedId.startsWith("America/") ||
            normalizedId.startsWith("Canada/") ||
            normalizedId.startsWith("US/") ||
            normalizedId.startsWith("Mexico/") -> AtlasContinent.NORTH_AMERICA
        normalizedId.startsWith("Indian/") -> AtlasContinent.ASIA
        normalizedId.startsWith("Atlantic/") -> AtlasContinent.EUROPE
        else -> AtlasContinent.ASIA
    }

    // Real geographic coordinates (Latitude, Longitude)
    val coords: Pair<Float, Float>? = when {
        // Asia
        normalizedId.contains("Kolkata") || normalizedId.contains("Calcutta") -> 22.57f to 88.36f
        normalizedId.contains("Delhi") || normalizedId.contains("Mumbai") -> 24.50f to 77.20f
        normalizedId.contains("Colombo") -> 6.93f to 79.86f
        normalizedId.contains("Kathmandu") -> 27.71f to 85.32f
        normalizedId.contains("Dhaka") -> 23.81f to 90.41f
        normalizedId.contains("Karachi") -> 24.86f to 67.00f
        normalizedId.contains("Kabul") -> 34.55f to 69.20f
        normalizedId.contains("Tashkent") -> 41.30f to 69.24f
        normalizedId.contains("Almaty") -> 43.22f to 76.85f
        normalizedId.contains("Dubai") || normalizedId.contains("Muscat") -> 25.20f to 55.27f
        normalizedId.contains("Riyadh") -> 24.71f to 46.67f
        normalizedId.contains("Kuwait") || normalizedId.contains("Qatar") -> 25.28f to 51.53f
        normalizedId.contains("Tehran") -> 35.68f to 51.38f
        normalizedId.contains("Baghdad") -> 33.31f to 44.36f
        normalizedId.contains("Jerusalem") || normalizedId.contains("Beirut") || normalizedId.contains("Amman") -> 31.76f to 35.21f
        normalizedId.contains("Bangkok") -> 13.75f to 100.50f
        normalizedId.contains("Ho_Chi_Minh") -> 10.82f to 106.63f
        normalizedId.contains("Singapore") -> 1.35f to 103.82f
        normalizedId.contains("Kuala_Lumpur") -> 3.13f to 101.68f
        normalizedId.contains("Jakarta") -> -6.20f to 106.84f
        normalizedId.contains("Manila") -> 14.59f to 120.98f
        normalizedId.contains("Hong_Kong") || normalizedId.contains("Macau") -> 22.31f to 114.16f
        normalizedId.contains("Taipei") -> 25.03f to 121.56f
        normalizedId.contains("Shanghai") -> 31.23f to 121.47f
        normalizedId.contains("Beijing") -> 39.90f to 116.40f
        normalizedId.contains("Seoul") -> 37.56f to 126.97f
        normalizedId.contains("Tokyo") || normalizedId.contains("Osaka") -> 35.67f to 139.65f

        // Europe
        normalizedId.contains("London") -> 51.50f to -0.12f
        normalizedId.contains("Dublin") -> 53.34f to -6.26f
        normalizedId.contains("Lisbon") -> 38.72f to -9.13f
        normalizedId.contains("Madrid") -> 40.41f to -3.70f
        normalizedId.contains("Paris") -> 48.85f to 2.35f
        normalizedId.contains("Brussels") || normalizedId.contains("Amsterdam") -> 52.36f to 4.90f
        normalizedId.contains("Berlin") -> 52.52f to 13.40f
        normalizedId.contains("Zurich") -> 47.37f to 8.54f
        normalizedId.contains("Vienna") -> 48.20f to 16.37f
        normalizedId.contains("Rome") -> 41.90f to 12.49f
        normalizedId.contains("Stockholm") -> 59.32f to 18.06f
        normalizedId.contains("Oslo") -> 59.91f to 10.75f
        normalizedId.contains("Copenhagen") -> 55.67f to 12.56f
        normalizedId.contains("Helsinki") -> 60.16f to 24.93f
        normalizedId.contains("Warsaw") -> 52.22f to 21.01f
        normalizedId.contains("Athens") -> 37.98f to 23.72f
        normalizedId.contains("Bucharest") -> 44.42f to 26.10f
        normalizedId.contains("Istanbul") -> 41.00f to 28.97f
        normalizedId.contains("Kiev") || normalizedId.contains("Kyiv") -> 50.45f to 30.52f
        normalizedId.contains("Moscow") -> 55.75f to 37.61f

        // North America
        normalizedId.contains("New_York") -> 40.71f to -74.00f
        normalizedId.contains("Toronto") -> 43.65f to -79.38f
        normalizedId.contains("Montreal") -> 45.50f to -73.56f
        normalizedId.contains("Chicago") -> 41.87f to -87.62f
        normalizedId.contains("Denver") -> 39.73f to -104.99f
        normalizedId.contains("Phoenix") -> 33.44f to -112.07f
        normalizedId.contains("Los_Angeles") -> 34.05f to -118.24f
        normalizedId.contains("San_Francisco") -> 37.77f to -122.41f
        normalizedId.contains("Vancouver") -> 49.28f to -123.12f
        normalizedId.contains("Anchorage") -> 61.21f to -149.90f
        normalizedId.contains("Mexico_City") -> 19.43f to -99.13f

        // South America
        normalizedId.contains("Bogota") -> 4.71f to -74.07f
        normalizedId.contains("Lima") -> -12.04f to -77.04f
        normalizedId.contains("Quito") -> -0.18f to -78.46f
        normalizedId.contains("Sao_Paulo") -> -23.55f to -46.63f
        normalizedId.contains("Rio_de_Janeiro") -> -22.90f to -43.17f
        normalizedId.contains("Buenos_Aires") -> -34.60f to -58.38f
        normalizedId.contains("Santiago") -> -33.44f to -70.66f

        // Africa
        normalizedId.contains("Cairo") -> 30.04f to 31.23f
        normalizedId.contains("Casablanca") -> 33.57f to -7.58f
        normalizedId.contains("Algiers") -> 36.75f to 3.05f
        normalizedId.contains("Lagos") -> 6.52f to 3.37f
        normalizedId.contains("Nairobi") -> -1.29f to 36.82f
        normalizedId.contains("Addis_Ababa") -> 9.03f to 38.74f
        normalizedId.contains("Johannesburg") -> -26.20f to 28.04f
        normalizedId.contains("Cape_Town") -> -33.92f to 18.42f

        // Oceania
        normalizedId.contains("Perth") -> -31.95f to 115.86f
        normalizedId.contains("Darwin") -> -12.46f to 130.84f
        normalizedId.contains("Brisbane") -> -27.47f to 153.02f
        normalizedId.contains("Sydney") -> -33.86f to 151.20f
        normalizedId.contains("Melbourne") -> -37.81f to 144.96f
        normalizedId.contains("Auckland") -> -36.84f to 174.76f
        normalizedId.contains("Wellington") -> -41.28f to 174.77f

        else -> null
    }

    val (lat, lon) = coords ?: run {
        val approxLon = (offsetHours * 15f)
        when (continent) {
            AtlasContinent.ASIA -> 26f to approxLon.coerceIn(35f, 140f)
            AtlasContinent.EUROPE -> 49f to approxLon.coerceIn(-8f, 38f)
            AtlasContinent.NORTH_AMERICA -> 39f to approxLon.coerceIn(-125f, -70f)
            AtlasContinent.SOUTH_AMERICA -> -18f to approxLon.coerceIn(-76f, -38f)
            AtlasContinent.AFRICA -> 4f to approxLon.coerceIn(-12f, 42f)
            AtlasContinent.OCEANIA -> -28f to approxLon.coerceIn(115f, 175f)
        }
    }

    return AtlasHomeLocation(
        continent = continent,
        lat = lat,
        lon = lon,
        label = cityName
    )
}

/**
 * Catmull-Rom spline converted to smooth cubic Bezier curves so coastlines look like a true 2D vector atlas.
 */
private fun buildSmoothAtlasPath(
    latLonPoints: List<Pair<Float, Float>>,
    project: (lat: Float, lon: Float) -> Offset
): Path {
    val pts = latLonPoints.map { (lat, lon) -> project(lat, lon) }
    val path = Path()
    val n = pts.size
    if (n < 3) return path

    path.moveTo(pts[0].x, pts[0].y)
    val tension = 0.22f

    for (i in 0 until n) {
        val p0 = pts[(i - 1 + n) % n]
        val p1 = pts[i]
        val p2 = pts[(i + 1) % n]
        val p3 = pts[(i + 2) % n]

        val cp1x = p1.x + (p2.x - p0.x) * tension
        val cp1y = p1.y + (p2.y - p0.y) * tension
        val cp2x = p2.x - (p3.x - p1.x) * tension
        val cp2y = p2.y - (p3.y - p1.y) * tension

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
    path.close()
    return path
}

private fun getContinentPolygons(continent: AtlasContinent): List<List<Pair<Float, Float>>> = when (continent) {
    AtlasContinent.ASIA -> listOf(
        // Mainland Eurasia-Asia: Anatolia -> Levant -> Arabian Peninsula -> Persian Gulf -> Makran ->
        // Indian Subcontinent -> Bay of Bengal -> Malay Peninsula -> Indochina -> South & East China ->
        // Korean Peninsula -> Russian Far East -> Siberia -> Central Asia
        listOf(
            41.2f to 29.0f, // Istanbul / Bosporus
            41.8f to 35.0f, // Black Sea coast
            41.6f to 41.6f, // Batumi
            43.5f to 40.0f,
            45.0f to 38.0f,
            47.0f to 40.0f,
            46.0f to 48.0f, // North Caspian
            42.0f to 52.0f,
            37.0f to 54.0f, // South Caspian
            38.5f to 49.0f,
            41.5f to 49.5f,
            45.0f to 51.0f,
            55.0f to 60.0f, // Urals
            65.0f to 68.0f, // Ob Gulf
            66.0f to 85.0f,
            66.0f to 110.0f,
            65.0f to 135.0f,
            62.0f to 150.0f, // Sea of Okhotsk north
            59.0f to 143.0f,
            54.0f to 138.0f,
            48.0f to 139.0f,
            43.0f to 132.0f, // Vladivostok
            40.0f to 129.5f, // Korea east
            35.1f to 129.0f, // Busan
            34.7f to 126.3f, // Korea southwest
            37.5f to 126.0f, // Incheon
            39.8f to 124.3f,
            39.0f to 121.6f, // Liaodong
            40.5f to 120.5f, // Bohai Bay
            38.0f to 118.5f,
            37.5f to 122.5f, // Shandong Peninsula
            35.5f to 119.5f,
            31.2f to 121.8f, // Yangtze Delta / Shanghai
            28.0f to 121.2f,
            24.5f to 118.5f, // Fujian
            22.3f to 114.2f, // Hong Kong / Pearl River
            20.3f to 110.4f, // Leizhou
            21.5f to 108.0f, // Gulf of Tonkin
            18.5f to 105.8f,
            16.0f to 108.3f, // Da Nang
            12.0f to 109.2f,
            9.2f to 105.0f,  // Mekong Delta
            10.5f to 103.5f,
            13.4f to 100.6f, // Bangkok / Gulf of Thailand
            9.5f to 99.2f,
            6.5f to 101.8f,
            1.3f to 103.8f,  // Singapore / Tip of Malay Peninsula
            3.1f to 101.3f,  // Strait of Malacca
            6.5f to 99.8f,
            10.0f to 98.5f,
            14.5f to 97.8f,
            16.5f to 97.5f,  // Gulf of Martaban
            16.0f to 94.3f,  // Irrawaddy Delta
            20.5f to 92.8f,  // Arakan coast
            22.3f to 91.8f,  // Chittagong
            21.8f to 89.0f,  // Sundarbans / Ganges Delta
            20.8f to 86.9f,  // Odisha
            17.7f to 83.3f,  // Visakhapatnam
            15.8f to 80.8f,
            13.1f to 80.3f,  // Chennai
            10.3f to 79.8f,  // Palk Strait
            8.08f to 77.55f, // Kanyakumari (Southern tip of India)
            9.9f to 76.2f,   // Kochi
            12.9f to 74.8f,  // Mangalore
            15.4f to 73.8f,  // Goa
            19.0f to 72.8f,  // Mumbai
            21.2f to 72.6f,  // Gulf of Khambhat
            20.7f to 70.5f,  // Kathiawar / Diu
            22.4f to 69.0f,  // Dwarka
            23.2f to 68.6f,  // Kutch
            24.8f to 67.0f,  // Karachi / Indus Delta
            25.2f to 62.0f,  // Makran coast
            25.6f to 57.5f,  // Strait of Hormuz (Iran side)
            27.2f to 56.2f,  // Bandar Abbas
            28.9f to 50.8f,  // Bushehr
            30.2f to 48.5f,  // Shatt al-Arab / Kuwait
            29.3f to 48.0f,
            26.2f to 50.6f,  // Bahrain / Qatar
            24.5f to 51.5f,
            24.4f to 54.4f,  // Abu Dhabi
            25.3f to 55.3f,  // Dubai
            26.2f to 56.3f,  // Musandam
            23.6f to 58.6f,  // Muscat
            22.5f to 59.8f,  // Ras al Hadd (Oman)
            17.0f to 54.1f,  // Salalah
            12.8f to 45.0f,  // Aden
            12.6f to 43.4f,  // Bab-el-Mandeb
            16.5f to 42.5f,  // Red Sea coast
            21.5f to 39.1f,  // Jeddah
            27.5f to 35.5f,
            29.5f to 34.9f,  // Gulf of Aqaba
            27.8f to 34.3f,  // Sinai tip
            31.2f to 32.3f,  // Suez / Port Said
            31.8f to 34.6f,  // Levant coast
            33.9f to 35.5f,  // Beirut
            36.8f to 36.0f,  // Iskenderun
            36.5f to 32.0f,  // Antalya
            36.8f to 28.2f,  // Marmaris
            38.4f to 27.1f,  // Izmir
            40.1f to 26.4f   // Dardanelles
        ),
        // Sri Lanka
        listOf(
            9.8f to 80.2f,
            8.6f to 81.2f,
            6.9f to 81.8f,
            6.0f to 80.6f,
            6.9f to 79.8f,
            8.8f to 79.9f
        ),
        // Japanese Archipelago (Honshu + Kyushu + Shikoku)
        listOf(
            41.4f to 140.9f,
            39.6f to 142.0f,
            38.2f to 141.0f,
            35.7f to 140.8f,
            35.0f to 139.0f,
            34.4f to 136.8f,
            33.5f to 135.7f,
            33.0f to 132.8f,
            31.2f to 130.6f,
            32.8f to 129.8f,
            34.0f to 131.0f,
            35.5f to 133.5f,
            35.7f to 136.0f,
            37.4f to 137.2f,
            37.9f to 139.0f,
            40.5f to 139.9f
        ),
        // Hokkaido
        listOf(
            45.4f to 141.7f,
            44.0f to 145.1f,
            42.9f to 144.4f,
            42.0f to 143.2f,
            42.4f to 140.3f,
            43.8f to 141.3f
        ),
        // Taiwan
        listOf(
            25.3f to 121.6f,
            24.0f to 121.6f,
            22.0f to 120.8f,
            23.0f to 120.1f,
            24.8f to 120.9f
        ),
        // Hainan
        listOf(
            20.0f to 110.3f,
            19.2f to 110.6f,
            18.2f to 109.5f,
            19.1f to 108.7f
        ),
        // Sumatra
        listOf(
            5.5f to 95.3f,
            3.6f to 98.7f,
            1.0f to 102.5f,
            -2.5f to 105.8f,
            -5.9f to 105.7f,
            -4.2f to 102.2f,
            -0.8f to 100.3f,
            2.8f to 97.5f
        ),
        // Java
        listOf(
            -6.0f to 106.0f,
            -6.8f to 110.5f,
            -7.7f to 114.4f,
            -8.6f to 114.2f,
            -7.8f to 110.0f,
            -7.2f to 106.4f
        ),
        // Borneo
        listOf(
            6.9f to 117.0f,
            5.2f to 119.1f,
            1.8f to 118.9f,
            -1.2f to 116.8f,
            -3.6f to 115.8f,
            -3.2f to 111.5f,
            0.0f to 109.2f,
            2.1f to 111.2f,
            4.5f to 114.0f
        ),
        // Sulawesi
        listOf(
            1.5f to 125.0f,
            0.6f to 123.0f,
            -1.5f to 123.5f,
            -4.5f to 122.5f,
            -5.5f to 120.0f,
            -3.5f to 119.0f,
            -0.5f to 119.8f,
            1.2f to 120.8f
        ),
        // Philippines (Luzon + Mindanao)
        listOf(
            18.5f to 121.5f,
            16.5f to 122.3f,
            14.0f to 122.8f,
            13.0f to 123.9f,
            13.7f to 121.0f,
            15.5f to 119.9f,
            18.1f to 120.6f
        ),
        listOf(
            9.6f to 125.6f,
            7.2f to 126.5f,
            6.1f to 125.2f,
            7.3f to 122.2f,
            8.6f to 123.4f
        )
    )

    AtlasContinent.EUROPE -> listOf(
        // Continental Europe + Scandinavia
        listOf(
            36.1f to -5.4f,  // Gibraltar
            38.7f to -9.4f,  // Lisbon
            43.3f to -8.4f,  // Galicia
            43.4f to -1.8f,  // Bay of Biscay
            47.5f to -3.0f,  // Brittany
            48.6f to -4.6f,
            49.5f to 0.1f,   // Normandy
            51.0f to 2.2f,   // Calais
            53.4f to 6.0f,   // Netherlands
            54.0f to 8.8f,   // German Bight
            57.7f to 10.6f,  // Jutland / Skagen
            54.5f to 10.2f,
            54.5f to 14.0f,  // Baltic coast
            54.5f to 18.6f,  // Gdansk
            56.5f to 21.0f,  // Latvia
            59.4f to 24.7f,  // Tallinn
            60.0f to 30.2f,  // St Petersburg
            60.5f to 22.2f,  // Finland SW
            65.0f to 25.4f,  // Gulf of Bothnia
            63.8f to 20.3f,
            59.3f to 18.1f,  // Stockholm
            56.0f to 14.8f,
            55.6f to 12.9f,  // Malmo
            59.9f to 10.7f,  // Oslo
            58.1f to 8.0f,
            60.4f to 5.3f,   // Bergen
            63.4f to 10.4f,  // Trondheim
            68.5f to 16.0f,  // Lofoten
            70.5f to 25.0f,  // North Cape
            69.0f to 33.0f,  // Murmansk
            66.0f to 40.0f,  // White Sea
            67.0f to 48.0f,
            55.0f to 49.0f,
            47.0f to 39.0f,  // Sea of Azov
            45.3f to 36.5f,  // Crimea
            46.4f to 30.7f,  // Odessa
            44.2f to 28.6f,  // Constanta
            41.2f to 29.0f,  // Istanbul
            40.6f to 23.0f,  // Thessaloniki
            38.0f to 23.7f,  // Athens
            36.8f to 22.5f,  // Peloponnese
            39.6f to 20.0f,  // Corfu
            42.6f to 18.1f,  // Dubrovnik
            45.4f to 13.5f,  // Istria / Venice
            43.6f to 13.5f,  // Ancona
            40.6f to 18.0f,  // Brindisi / Heel of Italy
            38.0f to 15.7f,  // Calabria / Toe
            40.8f to 14.2f,  // Naples
            41.9f to 12.4f,  // Rome
            44.4f to 8.9f,   // Genoa
            43.3f to 5.4f,   // Marseille
            41.4f to 2.2f,   // Barcelona
            39.5f to -0.3f,  // Valencia
            36.8f to -2.5f   // Almeria
        ),
        // Great Britain
        listOf(
            58.6f to -3.1f,
            57.5f to -1.8f,
            55.9f to -3.2f,
            54.5f to -0.8f,
            52.7f to 1.7f,
            51.3f to 1.4f,
            50.7f to -1.8f,
            50.1f to -5.5f,
            51.7f to -5.1f,
            53.4f to -3.1f,
            55.0f to -4.8f,
            57.8f to -5.6f
        ),
        // Ireland
        listOf(
            55.2f to -7.3f,
            54.6f to -5.6f,
            52.2f to -6.4f,
            51.6f to -9.8f,
            53.3f to -9.9f,
            54.4f to -8.5f
        ),
        // Sicily & Sardinia
        listOf(
            38.2f to 15.6f,
            37.0f to 15.1f,
            37.8f to 12.5f,
            38.1f to 13.4f
        ),
        listOf(
            41.2f to 9.3f,
            39.1f to 9.5f,
            39.0f to 8.4f,
            40.9f to 8.3f
        )
    )

    AtlasContinent.NORTH_AMERICA -> listOf(
        listOf(
            71.0f to -156.0f, // Point Barrow
            70.0f to -140.0f,
            69.0f to -125.0f,
            68.0f to -105.0f,
            66.0f to -86.0f,
            62.0f to -78.0f,  // Hudson Bay east
            55.0f to -79.0f,  // James Bay
            52.0f to -81.0f,
            56.0f to -88.0f,
            61.0f to -94.0f,
            64.0f to -88.0f,
            62.0f to -75.0f,
            58.0f to -63.0f,  // Labrador
            52.0f to -56.0f,
            47.5f to -53.0f,  // Newfoundland
            46.5f to -64.0f,
            44.6f to -63.6f,  // Nova Scotia
            42.3f to -71.0f,  // Boston
            40.7f to -74.0f,  // New York
            37.0f to -76.0f,  // Chesapeake
            35.2f to -75.5f,  // Cape Hatteras
            32.0f to -81.0f,  // Savannah
            25.8f to -80.2f,  // Miami
            25.2f to -81.1f,
            29.8f to -84.0f,  // Florida Panhandle
            29.3f to -89.5f,  // Mississippi Delta
            29.3f to -94.8f,  // Galveston
            26.0f to -97.2f,
            22.2f to -97.8f,  // Tampico
            19.2f to -96.1f,  // Veracruz
            18.5f to -92.5f,
            21.5f to -88.0f,  // Yucatan
            20.5f to -87.0f,
            16.0f to -86.0f,  // Honduras
            14.5f to -83.3f,
            9.0f to -79.5f,   // Panama
            8.5f to -83.0f,
            13.5f to -89.5f,
            16.8f to -99.9f,  // Acapulco
            20.6f to -105.2f, // Puerto Vallarta
            24.0f to -107.0f,
            31.0f to -114.5f, // Gulf of California
            23.0f to -109.9f, // Cabo San Lucas
            28.0f to -114.5f,
            32.7f to -117.2f, // San Diego
            34.0f to -118.5f, // Los Angeles
            37.8f to -122.5f, // San Francisco
            42.0f to -124.3f,
            48.4f to -124.6f, // Seattle / Olympic
            50.5f to -127.5f, // Vancouver Island
            54.5f to -130.5f,
            59.0f to -138.0f,
            60.5f to -148.0f, // Anchorage
            58.0f to -153.0f,
            55.0f to -163.0f, // Alaska Peninsula
            60.0f to -166.0f,
            65.5f to -168.0f  // Bering Strait
        ),
        // Greenland
        listOf(
            82.0f to -35.0f,
            76.0f to -20.0f,
            68.0f to -30.0f,
            60.0f to -44.0f,
            65.0f to -52.0f,
            72.0f to -55.0f,
            78.0f to -70.0f
        ),
        // Cuba
        listOf(
            23.1f to -82.4f,
            22.0f to -78.0f,
            20.1f to -74.3f,
            20.0f to -77.5f,
            21.8f to -84.5f
        )
    )

    AtlasContinent.SOUTH_AMERICA -> listOf(
        listOf(
            11.0f to -74.8f,  // Barranquilla
            12.2f to -71.5f,  // Guajira
            10.5f to -67.0f,  // Caracas
            10.2f to -62.0f,
            6.8f to -58.0f,
            4.5f to -51.5f,
            0.0f to -49.5f,   // Amazon Mouth
            -2.5f to -44.2f,
            -5.2f to -35.4f,  // Natal / Eastern tip
            -8.0f to -34.9f,  // Recife
            -13.0f to -38.5f, // Salvador
            -20.3f to -40.3f,
            -22.9f to -43.2f, // Rio de Janeiro
            -25.5f to -48.5f,
            -30.0f to -50.1f,
            -34.9f to -56.2f, // Montevideo / Rio de la Plata
            -36.3f to -56.8f,
            -38.0f to -57.5f, // Mar del Plata
            -41.0f to -62.8f,
            -45.8f to -67.5f,
            -51.6f to -69.2f,
            -55.0f to -67.0f, // Tierra del Fuego / Cape Horn
            -53.5f to -71.5f,
            -47.0f to -74.8f,
            -41.5f to -73.8f,
            -33.0f to -71.6f, // Valparaiso
            -23.6f to -70.4f, // Antofagasta
            -18.4f to -70.3f, // Arica
            -15.5f to -75.0f,
            -12.0f to -77.1f, // Lima
            -6.0f to -81.0f,  // Piura
            -2.2f to -80.9f,  // Guayaquil
            1.0f to -79.5f,
            5.5f to -77.4f,
            8.5f to -77.3f
        )
    )

    AtlasContinent.AFRICA -> listOf(
        listOf(
            35.8f to -5.8f,   // Tangier
            36.8f to 3.0f,    // Algiers
            37.2f to 9.9f,    // Tunis
            33.8f to 10.9f,
            32.9f to 13.2f,   // Tripoli
            31.2f to 16.6f,   // Gulf of Sidra
            32.8f to 22.0f,   // Benghazi
            31.2f to 29.9f,   // Alexandria
            31.3f to 32.3f,   // Port Said / Suez
            27.8f to 33.7f,
            22.0f to 36.8f,
            15.6f to 39.4f,   // Massawa
            11.8f to 43.2f,   // Djibouti
            11.8f to 51.2f,   // Horn of Africa (Cape Guardafui)
            5.0f to 48.2f,
            2.0f to 45.3f,    // Mogadishu
            -1.3f to 41.5f,
            -4.0f to 39.7f,   // Mombasa
            -6.8f to 39.3f,   // Dar es Salaam
            -11.0f to 40.5f,
            -15.0f to 40.7f,
            -20.0f to 34.9f,  // Beira
            -25.9f to 32.6f,  // Maputo
            -29.8f to 31.0f,  // Durban
            -33.9f to 25.6f,  // Port Elizabeth
            -34.8f to 20.0f,  // Cape Agulhas
            -33.9f to 18.4f,  // Cape Town
            -28.6f to 16.4f,
            -22.9f to 14.5f,  // Walvis Bay
            -17.2f to 11.8f,
            -8.8f to 13.2f,   // Luanda
            -4.8f to 11.8f,
            0.4f to 9.4f,     // Libreville
            4.0f to 9.7f,     // Douala / Cameroon
            4.5f to 7.0f,     // Niger Delta
            6.4f to 3.4f,     // Lagos
            5.5f to -0.2f,    // Accra
            4.8f to -7.5f,
            6.3f to -10.8f,   // Monrovia
            9.5f to -13.7f,   // Conakry
            14.7f to -17.5f,  // Dakar (Western tip)
            19.5f to -16.5f,
            24.0f to -15.5f,
            28.5f to -11.5f,
            33.5f to -7.6f    // Casablanca
        ),
        // Madagascar
        listOf(
            -12.0f to 49.3f,
            -15.5f to 50.4f,
            -20.0f to 48.8f,
            -25.2f to 47.0f,
            -25.0f to 44.0f,
            -20.0f to 44.3f,
            -15.7f to 46.3f
        )
    )

    AtlasContinent.OCEANIA -> listOf(
        // Australia
        listOf(
            -11.0f to 142.5f, // Cape York
            -15.5f to 145.3f, // Cairns
            -20.0f to 148.5f,
            -24.5f to 152.3f,
            -27.5f to 153.4f, // Brisbane
            -33.8f to 151.3f, // Sydney
            -37.5f to 149.9f, // Cape Howe
            -38.3f to 144.9f, // Melbourne
            -38.0f to 140.8f,
            -35.0f to 138.5f, // Adelaide
            -34.8f to 135.8f,
            -32.0f to 133.5f, // Great Australian Bight
            -32.2f to 126.0f,
            -34.0f to 122.0f,
            -35.0f to 117.9f, // Albany
            -34.3f to 115.0f, // Cape Leeuwin
            -31.9f to 115.7f, // Perth
            -27.5f to 114.1f,
            -21.8f to 114.1f, // North West Cape
            -20.3f to 118.6f,
            -17.9f to 122.2f, // Broome
            -14.5f to 125.5f,
            -12.4f to 130.8f, // Darwin
            -11.5f to 136.5f, // Arnhem Land
            -15.0f to 135.8f,
            -17.5f to 140.5f  // Gulf of Carpentaria
        ),
        // Tasmania
        listOf(
            -40.8f to 144.8f,
            -41.1f to 148.2f,
            -43.3f to 147.2f,
            -43.1f to 145.6f
        ),
        // New Zealand North Island
        listOf(
            -34.5f to 172.8f,
            -36.8f to 174.8f,
            -37.7f to 178.4f,
            -41.3f to 175.2f,
            -39.3f to 173.8f,
            -37.5f to 174.7f
        ),
        // New Zealand South Island
        listOf(
            -40.8f to 172.7f,
            -41.5f to 174.1f,
            -43.8f to 173.0f,
            -46.5f to 168.4f,
            -45.5f to 166.7f,
            -42.5f to 171.2f
        )
    )
}

fun DrawScope.drawAtlasContinentBackground(
    homeLocation: AtlasHomeLocation,
    landFillColor: Color,
    coastlineStrokeColor: Color,
    gridLineColor: Color,
    pinColor: Color,
    measurer: TextMeasurer,
    labelColor: Color
) {
    val bounds = boundsForContinent(homeLocation.continent)
    val lonSpan = (bounds.maxLon - bounds.minLon).coerceAtLeast(1f)
    val latSpan = (bounds.maxLat - bounds.minLat).coerceAtLeast(1f)

    // Preserve aspect ratio while centering nicely across the full screen
    val scaleX = size.width / lonSpan
    val scaleY = size.height / latSpan
    val scale = maxOf(scaleX, scaleY) * 0.94f

    val projectedWidth = lonSpan * scale
    val projectedHeight = latSpan * scale
    val offsetX = (size.width - projectedWidth) / 2f
    val offsetY = (size.height - projectedHeight) / 2f

    fun project(lat: Float, lon: Float): Offset {
        val x = offsetX + (lon - bounds.minLon) * scale
        val y = offsetY + (bounds.maxLat - lat) * scale
        return Offset(x, y)
    }

    // Subtle 15-degree atlas latitude/longitude graticule lines
    val gridStroke = 0.75.dp.toPx()
    var lonTick = ((bounds.minLon / 15f).toInt() * 15).toFloat()
    while (lonTick <= bounds.maxLon) {
        val x = project(bounds.minLat, lonTick).x
        if (x in 0f..size.width) {
            drawLine(
                color = gridLineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = gridStroke
            )
        }
        lonTick += 15f
    }

    var latTick = ((bounds.minLat / 15f).toInt() * 15).toFloat()
    while (latTick <= bounds.maxLat) {
        val y = project(latTick, bounds.minLon).y
        if (y in 0f..size.height) {
            drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = gridStroke
            )
        }
        latTick += 15f
    }

    // Draw smooth Bezier landmasses
    val polygons = getContinentPolygons(homeLocation.continent)
    polygons.forEach { latLonList ->
        val path = buildSmoothAtlasPath(latLonList, ::project)
        drawPath(path = path, color = landFillColor)
        drawPath(
            path = path,
            color = coastlineStrokeColor,
            style = Stroke(
                width = 1.25.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // Draw Home City beacon pin at exact Lat/Lon
    val pinPos = project(homeLocation.lat, homeLocation.lon)
    if (pinPos.x in 0f..size.width && pinPos.y in 0f..size.height) {
        drawCircle(
            color = pinColor.copy(alpha = pinColor.alpha * 0.22f),
            radius = 22.dp.toPx(),
            center = pinPos
        )
        drawCircle(
            color = pinColor.copy(alpha = pinColor.alpha * 0.50f),
            radius = 11.dp.toPx(),
            center = pinPos,
            style = Stroke(width = 1.25.dp.toPx())
        )
        drawCircle(
            color = pinColor,
            radius = 4.5.dp.toPx(),
            center = pinPos
        )

        if (homeLocation.label.isNotBlank()) {
            val textLayout = measurer.measure(
                text = homeLocation.label,
                style = TextStyle(
                    color = labelColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            )
            val labelX = (pinPos.x + 12.dp.toPx())
                .coerceIn(12.dp.toPx(), (size.width - textLayout.size.width - 12.dp.toPx()).coerceAtLeast(12.dp.toPx()))
            val labelY = (pinPos.y - textLayout.size.height / 2f)
                .coerceIn(12.dp.toPx(), (size.height - textLayout.size.height - 12.dp.toPx()).coerceAtLeast(12.dp.toPx()))
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(labelX, labelY)
            )
        }
    }
}

@Composable
fun WorldContinentMapBackground(
    homeZone: ZoneId,
    landFillColor: Color,
    coastlineStrokeColor: Color,
    gridLineColor: Color,
    pinColor: Color,
    labelColor: Color,
    measurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val homeLocation = remember(homeZone) { resolveAtlasHomeLocation(homeZone) }

    Canvas(modifier = modifier) {
        drawAtlasContinentBackground(
            homeLocation = homeLocation,
            landFillColor = landFillColor,
            coastlineStrokeColor = coastlineStrokeColor,
            gridLineColor = gridLineColor,
            pinColor = pinColor,
            measurer = measurer,
            labelColor = labelColor
        )
    }
}
