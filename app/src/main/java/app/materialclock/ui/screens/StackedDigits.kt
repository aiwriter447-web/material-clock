package app.materialclock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.materialclock.ui.theme.ClockFace
import app.materialclock.ui.theme.Numerals

@Composable
fun StackedDigits(
    parts: List<String>,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    label: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = ROW_INK_INSET)
            .then(
                if (label != null) {
                    Modifier.clearAndSetSemantics { contentDescription = label }
                } else {
                    Modifier
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        parts.forEachIndexed { i, part ->
            DigitRow(
                part = part,
                capHeight = capHeight,
                color = color,
                leading = leading.takeIf { i == 0 },
                separator = i > 0,
            )
        }
    }
}

@Composable
fun DigitRow(
    part: String,
    capHeight: Dp,
    color: Color,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    separator: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(SEPARATOR_CELL_DP.dp), contentAlignment = Alignment.Center) {
            when {
                leading != null -> androidx.compose.material3.Icon(
                    leading,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(capHeight * 0.96f),
                )
                separator -> ColonDots(color, capHeight)
            }
        }
        Numerals(
            text = part,
            capHeight = capHeight,
            color = color,
            width = ClockFace.TIMER_WIDTH,
            weight = ClockFace.TIMER_WEIGHT,
            slashedZero = true,
        )
    }
}

private const val SEPARATOR_CELL_DP = 67.2f
private val DOT_DIAMETER = 13.dp

val ROW_INK_INSET: Dp = (SEPARATOR_CELL_DP.dp - DOT_DIAMETER) / 2

@Composable
private fun ColonDots(color: Color, capHeight: Dp) {
    val spacing = (capHeight.value * 0.45f).dp
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(2) {
            Surface(color = color, shape = CircleShape, modifier = Modifier.size(DOT_DIAMETER)) {}
        }
    }
}

const val PILL_HEIGHT_DP = 136f

@Composable
fun WidePill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: ImageVector? = null,
    outlined: Boolean = false,
    height: Dp = PILL_HEIGHT_DP.dp,
    compact: Boolean = false,
    fillWidth: Boolean = true,
    contentPaddingH: Dp = if (compact) 10.dp else 24.dp,
    contentAlpha: Float = 1f,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier.then(if (fillWidth) Modifier.fillMaxWidth() else Modifier).height(height),
        shape = CircleShape,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = if (outlined) Color.Transparent else container,
            contentColor = content,
        ),
        border = if (outlined) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        },
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = contentPaddingH),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
        ) {
            if (icon != null) {
                androidx.compose.material3.Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 22.dp else 28.dp),
                )
                Spacer(Modifier.width(if (compact) 8.dp else 12.dp))
            }
            androidx.compose.material3.Text(
                text,
                style = if (compact) {
                    MaterialTheme.typography.titleLargeEmphasized
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
