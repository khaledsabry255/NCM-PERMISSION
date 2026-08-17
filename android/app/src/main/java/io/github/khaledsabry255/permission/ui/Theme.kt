package io.github.khaledsabry255.permission.ui

import androidx.compose.ui.graphics.Color
import io.github.khaledsabry255.permission.data.PermitLevel

/** Palette lifted from the web app so both versions look identical. */
object Palette {
    val BgDeep = Color(0xFF0B1117)
    val BgMid = Color(0xFF0E1620)
    val Glass = Color(0x0FFFFFFF)
    val GlassBorder = Color(0x24FFFFFF)
    val Gold = Color(0xFF17E0C3)
    val GoldSoft = Color(0xFF5EF2DC)
    val GoldDim = Color(0xFF0C8A78)
    val TextMain = Color(0xFFF4F1E9)
    val TextDim = Color(0xFF9FB3AE)

    val Ok = Color(0xFF3ECF8E)
    val OkBg = Color(0x243ECF8E)
    val Soon = Color(0xFFF0C14B)
    val SoonBg = Color(0x29F0C14B)
    val Bad = Color(0xFFEF6A6A)
    val BadBg = Color(0x29EF6A6A)
    val Ban = Color(0xFFFF3B3B)
    val Unclear = Color(0xFFA8B5B2)
    val UnclearBg = Color(0x24A8B5B2)
}

data class PermitColors(
    val background: Color,
    val border: Color,
    val content: Color,
    val iconBackground: Color
)

fun colorsFor(level: PermitLevel): PermitColors = when (level) {
    PermitLevel.OK -> PermitColors(Palette.OkBg, Palette.Ok.copy(alpha = 0.4f), Palette.Ok, Palette.Ok.copy(alpha = 0.2f))
    PermitLevel.SOON -> PermitColors(Palette.SoonBg, Palette.Soon.copy(alpha = 0.4f), Palette.Soon, Palette.Soon.copy(alpha = 0.2f))
    PermitLevel.BAD -> PermitColors(Palette.BadBg, Palette.Bad.copy(alpha = 0.4f), Palette.Bad, Palette.Bad.copy(alpha = 0.2f))
    PermitLevel.BAN -> PermitColors(Palette.Ban, Palette.Ban, Color.White, Color.White.copy(alpha = 0.22f))
    PermitLevel.UNCLEAR -> PermitColors(Palette.UnclearBg, Palette.Unclear.copy(alpha = 0.4f), Palette.Unclear, Palette.Unclear.copy(alpha = 0.2f))
}
