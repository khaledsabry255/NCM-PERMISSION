package io.github.khaledsabry255.permission.data

/**
 * Minimal RFC-4180 CSV reader.
 *
 * The sheet contains Arabic free text with commas and the occasional embedded
 * newline inside quoted cells, so a naive split(",") loses columns and silently
 * shifts every field after it. This walks the text character by character instead.
 */
object Csv {

    fun parse(text: String): List<List<String>> {
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var i = 0

        // Strip a UTF-8 BOM if the export includes one (written as an escape so the
        // source file itself stays plain ASCII here).
        val src = if (text.isNotEmpty() && text[0] == '\uFEFF') text.substring(1) else text

        while (i < src.length) {
            val c = src[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < src.length && src[i + 1] == '"' -> {
                        cell.append('"'); i++
                    }
                    c == '"' -> inQuotes = false
                    else -> cell.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> { row.add(cell.toString()); cell.setLength(0) }
                c == '\r' -> { /* handled by the \n branch */ }
                c == '\n' -> {
                    row.add(cell.toString()); cell.setLength(0)
                    rows.add(row); row = ArrayList()
                }
                else -> cell.append(c)
            }
            i++
        }

        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            rows.add(row)
        }
        return rows
    }
}
