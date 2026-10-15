package app.materialclock.data

enum class ScreensaverBackgroundType(val label: String) {
    OLED_BLACK("OLED Pure Black"),
    LCD_DARK_GRAY("LCD Dark Gray"),
    SOLID_COLOR("Solid Color"),
    GRADIENT("Gradient"),
    GALLERY_IMAGE("Gallery Image")
}

data class ScreensaverSettings(
    val enabled: Boolean = true,
    val clockFaceId: String = "default_face",
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val dateFormat: String = "dd/MM/yyyy",
    val showBattery: Boolean = true,
    val backgroundType: ScreensaverBackgroundType = ScreensaverBackgroundType.OLED_BLACK,
    val solidBackgroundColor: Long = 0xFF000000,
    val galleryImageUri: String? = null,
    val nightModeEnabled: Boolean = false,
    val clockDimmingFactor: Float = 0.4f,
    val pixelShiftEnabled: Boolean = true,
    val clockShiftIntervalMinutes: Int = 2,
    val dateShiftIntervalMinutes: Int = 3,
    val batteryShiftIntervalMinutes: Int = 4
)
