package io.github.khaledsabry255.permission.ui

import androidx.compose.ui.graphics.Color
import io.github.khaledsabry255.permission.data.PermitLevel

/** Palette lifted from the web app so both versions look identical. */
object Palette {
    // Light NCM palette, matching the web app.
    val BgDeep = Color(0xFFEDF2F8)      // page
    val BgMid = Color(0xFFE6EEF8)       // tint
    val Glass = Color(0x99ACBCCE)       // translucent record card
    val GlassBorder = Color(0xFFB3C2D4)

    // Brand, taken from the NCM wordmark
    val Gold = Color(0xFF1565C0)        // the accent is blue now; the name is
    val GoldSoft = Color(0xFF1E75D6)    // kept so every rule that reads it follows
    val GoldDim = Color(0xFF0E4C93)
    val OnGold = Color(0xFFFFFFFF)
    val BrandGreen = Color(0xFF4BA548)
    val BrandGrey = Color(0xFF474D54)

    val TextMain = Color(0xFF0B1114)
    val TextDim = Color(0xFF2A3238)

    val Card = Color(0xFFFFFFFF)
    val Field = Color(0xFFF3F7FB)
    val Line = Color(0xFFD2DEEC)
    val LineSoft = Color(0xFFE2E9F2)
    val Edge = Color(0xFFC0D3EC)
    val RowBg = Color(0xFFFFFFFF)
    val RowLine = Color(0xFFC7D3E0)
    val Sky = Color(0xFFA9CCEE)         // NCM DATA / ASE DATA heading
    val SkyLine = Color(0xFF7BAADD)

    val Ok = Color(0xFF12874F)
    val OkBg = Color(0xFFDFF3E7)
    val OkBr = Color(0xFF8FD3AE)
    val Soon = Color(0xFFB96E08)
    val SoonBg = Color(0xFFFCEBD5)
    val SoonBr = Color(0xFFEFC383)
    val Bad = Color(0xFFC22F2F)
    val BadBg = Color(0xFFFBE3E3)
    val BadBr = Color(0xFFEFA9A9)
    val Ban = Color(0xFFA31515)
    val BanBg = Color(0xFFF6D6D6)
    val BanBr = Color(0xFFE08A8A)
    val Unclear = Color(0xFF5A646C)
    val UnclearBg = Color(0xFFEDF0F3)
    val UnclearBr = Color(0xFFCBD3DB)

    // Tab row, straight off the site: the open tab is amber, the other is a
    // plain white box rather than an empty slot.
    val TabOnBg = Color(0xFFFFD24D)
    val TabOnBr = Color(0xFFE0A800)
    val TabOnInk = Color(0xFF0B1114)
    val TabOffBg = Color(0xFFFFFFFF)
    val TabOffBr = Color(0xFFD2DEEC)
    val TabOffInk = Color(0xFF2A3238)

    // Permit-count badge
    val CountBg = Color(0xFFF7C58A)
    val CountBr = Color(0xFFD98B2B)
    val CountInk = Color(0xFF8A4E04)
}

data class PermitColors(
    val background: Color,
    val border: Color,
    val content: Color,
    val iconBackground: Color
)

fun colorsFor(level: PermitLevel): PermitColors = when (level) {
    PermitLevel.OK -> PermitColors(Palette.OkBg, Palette.OkBr, Palette.Ok, Palette.Ok)
    PermitLevel.SOON -> PermitColors(Palette.SoonBg, Palette.SoonBr, Palette.Soon, Palette.Soon)
    PermitLevel.BAD -> PermitColors(Palette.BadBg, Palette.BadBr, Palette.Bad, Palette.Bad)
    PermitLevel.BAN -> PermitColors(Palette.BanBg, Palette.BanBr, Palette.Ban, Palette.Ban)
    PermitLevel.UNCLEAR -> PermitColors(Palette.UnclearBg, Palette.UnclearBr, Palette.Unclear, Palette.Unclear)
}
