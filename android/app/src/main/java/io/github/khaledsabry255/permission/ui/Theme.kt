package io.github.khaledsabry255.permission.ui

import androidx.compose.ui.graphics.Color
import io.github.khaledsabry255.permission.data.PermitLevel

/** Palette lifted from the web app so both versions look identical. */
object Palette {
    val BgDeep = Color(0xFF0D1210)
    val BgMid = Color(0xFF121815)
    val Glass = Color(0x0FFFFFFF)
    val GlassBorder = Color(0x24FFFFFF)

    // Brand accent, taken from the NCM wordmark.
    val Gold = Color(0xFFC9A961)
    val GoldSoft = Color(0xFFE0C489)
    val GoldDim = Color(0xFF8A7340)
    val OnGold = Color(0xFF0D1210)
    val BrandGreen = Color(0xFF4CAF50)

    val TextMain = Color(0xFFF2EFE6)
    val TextDim = Color(0xFF9AA39B)

    val Ok = Color(0xFF3ECF8E)
    val OkBg = Color(0x243ECF8E)
    // Pushed from yellow to orange so it never reads as the gold accent.
    val Soon = Color(0xFFE89B3C)
    val SoonBg = Color(0x26E89B3C)
    val Bad = Color(0xFFEF6A6A)
    val BadBg = Color(0x26EF6A6A)
    val Ban = Color(0xFFFF3B3B)
    val Unclear = Color(0xFFA8B0A8)
    val UnclearBg = Color(0x24A8B0A8)
}

data class PermitColors(
    val background: Color,
    val border: Color,
    val content: Color,
    val iconBackground: Color
)

fun colorsFor(level: PermitLevel): PermitColors = when (level) {
    PermitLevel.OK -> PermitColors(Palette.OkBg, Palette.Ok.copy(alpha = 0.42f), Palette.Ok, Palette.Ok.copy(alpha = 0.2f))
    PermitLevel.SOON -> PermitColors(Palette.SoonBg, Palette.Soon.copy(alpha = 0.42f), Palette.Soon, Palette.Soon.copy(alpha = 0.2f))
    PermitLevel.BAD -> PermitColors(Palette.BadBg, Palette.Bad.copy(alpha = 0.42f), Palette.Bad, Palette.Bad.copy(alpha = 0.2f))
    PermitLevel.BAN -> PermitColors(Palette.Ban, Palette.Ban, Color.White, Color.White.copy(alpha = 0.22f))
    PermitLevel.UNCLEAR -> PermitColors(Palette.UnclearBg, Palette.Unclear.copy(alpha = 0.42f), Palette.Unclear, Palette.Unclear.copy(alpha = 0.2f))
}
