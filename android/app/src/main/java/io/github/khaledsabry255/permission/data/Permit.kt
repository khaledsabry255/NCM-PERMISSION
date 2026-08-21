package io.github.khaledsabry255.permission.data

import java.util.Calendar

/** Colour band for a permit cell. Mirrors the web app's pb-* classes exactly. */
enum class PermitLevel { OK, SOON, BAD, BAN, UNCLEAR }

/**
 * Language-neutral shape of a permit cell. The displayed text is resolved later
 * from [kind] + [raw], so the same record can be re-rendered in either language
 * without re-reading the sheet.
 */
enum class PermitKind { PENDING, UNDETERMINED, RAW }

/** A cell that starts with a date: the date itself, and the text after it. */
data class DateNote(val date: String?, val note: String)

data class PermitStatus(
    val kind: PermitKind,
    val level: PermitLevel,
    val raw: String
)

object Permit {

    private val BAN_WORDS = listOf("منع")
    private val RENEW_WORDS = listOf("عايز تجديد", "الرخصه منتهيه", "غيرت لوحات", "تجديد")
    private val TEMP_WORDS = listOf("مؤقت")

    private val DATE_RE = Regex("""^(\d{1,2})([-/])(\d{1,2})[-/](\d{4})""")

    /** Arabic-Indic and Persian digits, rewritten to 0-9. Letters are untouched. */
    private fun toLatinDigits(s: String): String = buildString {
        for (c in s) append(
            when (c) {
                in '٠'..'٩' -> '0' + (c - '٠')
                in '۰'..'۹' -> '0' + (c - '۰')
                else -> c
            }
        )
    }

    fun normalize(s: String?): String =
        toLatinDigits(s ?: "").replace(Regex("""\s+"""), " ").trim()

    /**
     * The sheet hands us two different date shapes:
     *   "18-11-2025"  text cells typed by hand              -> day first
     *   "11/30/2025"  real date cells, exported in US order -> month first
     *
     * Reading a slash date as day-first turns 11/30/2025 into month 30, which
     * rolls forward into 2028 and paints an expired permit green. Pick the order
     * from the separator, and fall back to day-first when the numbers rule out
     * month-first.
     */
    private data class Parts(val day: Int, val month: Int, val year: Int, val matched: String, val millis: Long)

    private fun parts(s: String): Parts? {
        val m = DATE_RE.find(s) ?: return null
        val (aStr, _, bStr, yStr) = m.destructured

        val a = aStr.toInt()
        val b = bStr.toInt()
        val year = yStr.toInt()

        // The sheet is normalised to day-first (dd/mm/yyyy). Only read month-first
        // when day-first is impossible, which is what a legacy US-order export
        // looks like.
        val day: Int
        val month: Int
        if (b > 12 && a <= 12) {
            month = a; day = b
        } else {
            day = a; month = b
        }

        if (month !in 1..12 || day !in 1..31) return null

        val millis = try {
            val cal = Calendar.getInstance()
            cal.isLenient = false // reject 31-02 instead of rolling it over
            cal.clear()
            cal.set(year, month - 1, day)
            cal.timeInMillis
        } catch (e: Exception) {
            return null
        }
        return Parts(day, month, year, m.value, millis)
    }

    fun parseDate(v: String?): Long? {
        val s = normalize(v)
        if (s.isEmpty()) return null
        return parts(s)?.millis
    }

    /**
     * Splits a cell into a tidy dd/mm/yyyy date plus whatever text trailed it,
     * so "18-11-2025(ABNA SINA)" reads as a date with its note beside it — which
     * is what the site shows.
     */
    fun splitDateAndNote(v: String?): DateNote {
        val s = normalize(v)
        if (s.isEmpty()) return DateNote(null, "")
        val p = parts(s) ?: return DateNote(null, s)
        val rest = s.substring(p.matched.length)
            .trimStart(' ', '-', '/', '(', ')', ',', '.', ':', '\u061B', ';')
            .trimEnd(')', ']')
            .trim()
        return DateNote("%02d/%02d/%04d".format(p.day, p.month, p.year), rest)
    }

    fun classify(rawValue: String?): PermitStatus {
        val s = normalize(rawValue)

        // 1) Strictly empty -> awaiting response
        if (s.isEmpty()) return PermitStatus(PermitKind.PENDING, PermitLevel.SOON, s)

        // 2) Question marks only (spaces ignored) -> status undetermined
        val noSpaces = s.replace(Regex("""\s+"""), "")
        if (noSpaces.isNotEmpty() && noSpaces.all { it == '?' || it == '؟' }) {
            return PermitStatus(PermitKind.UNDETERMINED, PermitLevel.UNCLEAR, s)
        }

        // 3) Everything else keeps the existing classification, shown as written
        if (BAN_WORDS.any { s.contains(it) }) return PermitStatus(PermitKind.RAW, PermitLevel.BAN, s)
        if (RENEW_WORDS.any { s.contains(it) }) return PermitStatus(PermitKind.RAW, PermitLevel.BAD, s)
        if (TEMP_WORDS.any { s.contains(it) }) return PermitStatus(PermitKind.RAW, PermitLevel.UNCLEAR, s)

        val date = parseDate(s)
        if (date != null) {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val diffDays = Math.round((date - today) / 86_400_000.0)
            return when {
                diffDays < 0 -> PermitStatus(PermitKind.RAW, PermitLevel.BAD, s)
                diffDays <= 30 -> PermitStatus(PermitKind.RAW, PermitLevel.SOON, s)
                else -> PermitStatus(PermitKind.RAW, PermitLevel.OK, s)
            }
        }
        return PermitStatus(PermitKind.RAW, PermitLevel.UNCLEAR, s)
    }
}
