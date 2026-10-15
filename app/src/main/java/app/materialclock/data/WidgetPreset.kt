package app.materialclock.data

/**
 * The five starting points the widget editor opens on, plus "Custom" as the sixth tile.
 *
 * A preset is a whole [WidgetConfig], not a hint; picking one replaces every field. That is what
 * makes the grid honest: the tile is rendered by the same code that will draw the widget, so what
 * is on the tile is exactly what lands on the home screen.
 *
 * They are chosen to span the axes rather than to be five variations of a circle: between them they
 * cover the plain classic dial and four finished designs: Ripple, Bold, Digit and Orbit. Anything a preset does not reach is one tap away in Custom.
 */
enum class WidgetPreset(val label: String, val config: WidgetConfig) {

    /** The default: a filled circle, four numerals, dots between them. */
    CLASSIC(
        "Classic",
        WidgetConfig(),
    ),

    /**
     * A scalloped dial with two thick short hands, and a small dot that circles the rim once a
     * minute in place of a seconds hand. No numerals, no marks: the shape is the design.
     */
    RIPPLE(
        "Ripple",
        WidgetConfig(
            look = AnalogLook.RIPPLE,
            shape = FaceShape.RIPPLE,
            hourHand = HandStyle.BATON,
            minuteHand = HandStyle.BATON,
            secondHand = HandStyle.ORBIT,
            centrePin = false,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.NONE,
        ),
    ),

    /** Four huge numerals in a tone only a little off the dial's own, with bold hands over them. */
    BOLD(
        "Bold",
        WidgetConfig(
            look = AnalogLook.BOLD,
            shape = FaceShape.CIRCLE,
            hourHand = HandStyle.BATON,
            minuteHand = HandStyle.BATON,
            secondHand = HandStyle.NEEDLE,
            numerals = NumeralSystem.ARABIC,
            indexSet = IndexSet.QUARTERS,
            minorIndices = MinorIndex.NONE,
            numeralSize = NumeralSize.HUGE,
            numeralWeight = NumeralWeight.BLACK,
        ),
    ),

    /** The time itself, hours over minutes, as big numerals behind the hands. */
    DIGIT(
        "Digit",
        WidgetConfig(
            look = AnalogLook.DIGIT,
            shape = FaceShape.RIPPLE,
            hourHand = HandStyle.BATON,
            minuteHand = HandStyle.BATON,
            secondHand = HandStyle.NEEDLE,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.DOTS,
        ),
    ),

    /**
     * A round dial with four complications: a small clock on top, the date below, the time on
     * the right and the battery on the left. The three hands here belong to the small clock.
     */
    ORBIT(
        "Orbit",
        WidgetConfig(
            look = AnalogLook.ORBIT,
            shape = FaceShape.CIRCLE,
            hourHand = HandStyle.BATON,
            minuteHand = HandStyle.NEEDLE,
            secondHand = HandStyle.NEEDLE,
            centrePin = true,
            numerals = NumeralSystem.NONE,
            indexSet = IndexSet.NONE,
            minorIndices = MinorIndex.NONE,
        ),
    ),
    ;

    companion object {
        /** The preset [config] is exactly, or null when the configuration has been hand-edited. */
        fun matching(config: WidgetConfig): WidgetPreset? = entries.firstOrNull { it.config == config }
    }
}