package io.github.khaledsabry255.permission.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads both tabs of the NCM permits Google Sheet.
 *
 * Uses the gviz CSV endpoint, NOT "Publish to web" links — the published links
 * are served from a host that rejects the request, which previously broke the
 * app entirely. Do not swap these back.
 */
object SheetRepository {

    private const val SHEET_ID = "18EH8aogUF0qZeLFjku7uiF3e9a_TjCTojRd53qMx0a0"
    private const val SHEET_INDIVIDUALS = "تصاريح أفراد "  // trailing space is part of the tab name
    private const val SHEET_VEHICLES = "تصاريح مركبات"

    // Read tabs by NAME, not gid: a re-import changes a tab's gid and the old
    // gid then falls back to the first sheet, which silently broke vehicles.
    private fun csvUrl(sheetName: String): String {
        val enc = java.net.URLEncoder.encode(sheetName, "UTF-8").replace("+", "%20")
        return "https://docs.google.com/spreadsheets/d/$SHEET_ID/gviz/tq?tqx=out:csv&sheet=$enc"
    }

    // Fixed column order in "تصاريح أفراد" — read by index, not by header name.
    private const val IND_NAME = 2
    private const val IND_JOB = 3
    private const val IND_ID = 4
    private const val IND_ADDR = 5
    private const val IND_NOTE = 6
    private const val IND_SENDER = 7
    private const val IND_PERMIT = 8
    private const val IND_DAY = 9

    // Fixed column order in "تصاريح مركبات".
    private const val VEH_PLATE = 2
    private const val VEH_TYPE = 3
    private const val VEH_OWNER = 4
    private const val VEH_PERMIT = 5
    private const val VEH_DAY = 6

    suspend fun load(): SheetData = withContext(Dispatchers.IO) {
        val people = parsePeople(fetch(csvUrl(SHEET_INDIVIDUALS)))
        val vehicles = parseVehicles(fetch(csvUrl(SHEET_VEHICLES)))
        SheetData(people, vehicles)
    }

    private fun fetch(urlStr: String): String {
        var current = urlStr
        // gviz bounces through a couple of Google hosts before serving the CSV.
        repeat(6) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 20_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                setRequestProperty("Accept", "text/csv,text/plain,*/*")
            }
            try {
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                        ?: throw IllegalStateException("HTTP $code without Location")
                    current = URL(URL(current), location).toString()
                    return@repeat
                }
                if (code != 200) throw IllegalStateException("HTTP $code")
                return conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } finally {
                conn.disconnect()
            }
        }
        throw IllegalStateException("Too many redirects")
    }

    private fun cell(cols: List<String>, i: Int): String =
        if (i < cols.size) Permit.normalize(cols[i]) else ""

    private fun rawCell(cols: List<String>, i: Int): String =
        if (i < cols.size) cols[i] else ""

    private fun parsePeople(csv: String): List<PersonGroup> {
        val rows = Csv.parse(csv).drop(1) // skip header row
        val grouped = LinkedHashMap<String, MutableList<PersonRecord>>()
        val identity = LinkedHashMap<String, Pair<String, String>>() // key -> (name, id)

        for (cols in rows) {
            if (cols.size < 5) continue
            val id = cell(cols, IND_ID)
            val name = cell(cols, IND_NAME)
            if (id.isEmpty() && name.isEmpty()) continue // separator / blank row

            val key = if (id.isNotEmpty()) "ID:" + id.filter { it.isDigit() } else "NAME:$name"
            identity.getOrPut(key) { name to id }
            grouped.getOrPut(key) { mutableListOf() }.add(
                PersonRecord(
                    job = cell(cols, IND_JOB),
                    address = cell(cols, IND_ADDR),
                    note = cell(cols, IND_NOTE),
                    sender = cell(cols, IND_SENDER),
                    permitRaw = rawCell(cols, IND_PERMIT),
                    day = cell(cols, IND_DAY),
                    dayMillis = Permit.parseDate(rawCell(cols, IND_DAY))
                )
            )
        }

        return grouped.map { (key, records) ->
            val (name, id) = identity[key]!!
            PersonGroup(name, id, records.sortedWith(byDay { it.dayMillis }))
        }
    }

    private fun parseVehicles(csv: String): List<VehicleGroup> {
        val rows = Csv.parse(csv).drop(1)
        val grouped = LinkedHashMap<String, MutableList<VehicleRecord>>()
        val plates = LinkedHashMap<String, String>()

        for (cols in rows) {
            if (cols.size < 3) continue
            val plate = cell(cols, VEH_PLATE)
            if (plate.isEmpty()) continue

            val key = plate.replace(" ", "")
            plates.getOrPut(key) { plate }
            grouped.getOrPut(key) { mutableListOf() }.add(
                VehicleRecord(
                    type = cell(cols, VEH_TYPE),
                    owner = cell(cols, VEH_OWNER),
                    permitRaw = rawCell(cols, VEH_PERMIT),
                    day = cell(cols, VEH_DAY),
                    dayMillis = Permit.parseDate(rawCell(cols, VEH_DAY))
                )
            )
        }

        return grouped.map { (key, records) ->
            VehicleGroup(plates[key]!!, records.sortedWith(byDay { it.dayMillis }))
        }
    }

    /** Oldest first; rows without a usable date keep to the front, as in the web app. */
    private fun <T> byDay(selector: (T) -> Long?): Comparator<T> = Comparator { a, b ->
        val da = selector(a)
        val db = selector(b)
        when {
            da != null && db != null -> da.compareTo(db)
            da != null -> -1
            db != null -> 1
            else -> 0
        }
    }
}
