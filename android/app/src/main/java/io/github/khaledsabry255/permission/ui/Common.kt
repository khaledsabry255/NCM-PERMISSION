package io.github.khaledsabry255.permission.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The NCM shield mark, redrawn from the same path the web app uses. */
@Composable
fun ShieldLogo(
    size: Dp,
    strokeWidth: Float = 6f,
    showInnerRing: Boolean = false
) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension / 100f
        val stroke = Stroke(
            width = strokeWidth * s,
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )

        val shield = Path().apply {
            moveTo(20 * s, 20 * s)
            lineTo(26 * s, 10 * s)
            lineTo(74 * s, 10 * s)
            lineTo(80 * s, 20 * s)
            lineTo(80 * s, 44 * s)
            quadraticBezierTo(80 * s, 68 * s, 50 * s, 90 * s)
            quadraticBezierTo(20 * s, 68 * s, 20 * s, 44 * s)
            close()
        }
        drawPath(shield, Palette.Gold, style = stroke)

        // Outer broken ring around the shield's centre.
        val r = 15.5f * s
        drawArc(
            color = Palette.Gold,
            startAngle = -71f,
            sweepAngle = 277f,
            useCenter = false,
            topLeft = Offset(50 * s - r, 44 * s - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = (strokeWidth - 0.5f).coerceAtLeast(2f) * s, cap = StrokeCap.Round)
        )

        if (showInnerRing) {
            val r2 = 12.4f * s
            drawArc(
                color = Palette.GoldDim,
                startAngle = 75f,
                sweepAngle = 74f,
                useCenter = false,
                topLeft = Offset(50 * s - r2, 44 * s - r2),
                size = Size(r2 * 2, r2 * 2),
                style = Stroke(width = 2.4f * s, cap = StrokeCap.Round)
            )
        }

        drawCircle(Palette.Gold, radius = 2.6f * s, center = Offset(50 * s, 44 * s))
    }
}

/** AR / EN pill. Same control appears on the lock screen and in the header. */
@Composable
fun LangSwitch(
    lang: Lang,
    onLangChange: (Lang) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Palette.GlassBorder), CircleShape)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LangOption("ع", lang == Lang.AR) { onLangChange(Lang.AR) }
        LangOption("EN", lang == Lang.EN) { onLangChange(Lang.EN) }
    }
}

@Composable
private fun LangOption(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (active) Palette.Gold else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) Color(0xFF06120F) else Palette.TextDim,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
