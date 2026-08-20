package io.github.khaledsabry255.permission.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The NCM wordmark, drawn as text rather than a bitmap so it stays razor sharp
 * at any density and scales with the layout instead of blurring.
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
                withStyle(SpanStyle(color = Color.White)) { append("N") }
                withStyle(SpanStyle(color = Palette.BrandGreen)) { append("C") }
                withStyle(SpanStyle(color = Color.White)) { append("M") }
            },
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = markSize,
            letterSpacing = (markSize.value * 0.025f).sp
        )
        Spacer(Modifier.height((markSize.value * 0.22f).dp))
        Text(
            text = "NUCLEAR CONCRETE MIXES",
            color = Palette.Gold,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = subSize,
            letterSpacing = subSpacing,
            maxLines = 1
        )
    }
}

/** AR / EN pill. Rendered once at the root so it never shifts between screens. */
@Composable
fun LangSwitch(
    lang: Lang,
    onLangChange: (Lang) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
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
            color = if (active) Palette.OnGold else Palette.TextDim,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
