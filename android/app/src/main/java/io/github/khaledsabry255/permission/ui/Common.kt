package io.github.khaledsabry255.permission.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The NCM wordmark, drawn as text rather than a bitmap so it stays razor sharp
 * at any density. Colours and face are the site's: Montserrat, a blue N, a green
 * C and a grey M.
 */
@Composable
fun NcmLogo(
    markSize: TextUnit,
    subSize: TextUnit,
    subSpacing: TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Palette.Gold)) { append("N") }
                withStyle(SpanStyle(color = Palette.BrandGreen)) { append("C") }
                withStyle(SpanStyle(color = Palette.BrandGrey)) { append("M") }
            },
            fontFamily = Fonts.Display,
            fontWeight = FontWeight.ExtraBold,
            fontSize = markSize,
            // The site sets -0.005em, which reads as a hair of negative tracking.
            letterSpacing = (markSize.value * -0.005f).sp
        )
        Spacer(Modifier.height((markSize.value * 0.2f).dp))
        Text(
            text = "NUCLEAR CONCRETE MIXES",
            color = Palette.BrandGrey,
            fontFamily = Fonts.Display,
            fontWeight = FontWeight.SemiBold,
            fontSize = subSize,
            letterSpacing = subSpacing,
            maxLines = 1
        )
    }
}

/**
 * The language control: one solid blue button carrying the language it switches
 * to, exactly as the site draws it.
 */
@Composable
fun LangButton(
    lang: Lang,
    onLangChange: (Lang) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Palette.Gold)
            .clickable { onLangChange(if (lang == Lang.AR) Lang.EN else Lang.AR) }
            .padding(horizontal = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(14.dp)) { drawGlobeIcon(Palette.OnGold) }
        Text(
            text = if (lang == Lang.AR) "EN" else "ع",
            color = Palette.OnGold,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** The globe the site puts on the language button. */
fun DrawScope.drawGlobeIcon(tint: Color) {
    val u = size.minDimension / 24f
    val s = Stroke(width = 2f * u, cap = StrokeCap.Round)
    drawCircle(tint, radius = 9 * u, center = Offset(12 * u, 12 * u), style = s)
    drawLine(tint, Offset(3 * u, 12 * u), Offset(21 * u, 12 * u), s.width, StrokeCap.Round)
    drawArc(
        color = tint,
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(7.5f * u, 3 * u),
        size = Size(9 * u, 18 * u),
        style = s
    )
    drawArc(
        color = tint,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(7.5f * u, 3 * u),
        size = Size(9 * u, 18 * u),
        style = s
    )
}
