package io.github.khaledsabry255.permission.data

const val MAX_RESULTS = 50

data class PersonResults(val groups: List<PersonGroup>, val truncated: Boolean)
data class VehicleResults(val groups: List<VehicleGroup>, val truncated: Boolean)

object Search {

    private val ARABIC_DIGITS = '٠'..'٩'

    fun people(all: List<PersonGroup>, query: String): PersonResults {
        val q = Permit.normalize(query)
        if (q.isEmpty()) return PersonResults(emptyList(), false)

        val digits = q.filter { it.isDigit() }
        if (digits.length >= 8) {
            val byId = all.firstOrNull { it.nationalId.filter(Char::isDigit) == digits }
            if (byId != null) return PersonResults(listOf(byId), false)
        }

        val exact = all.filter { Permit.normalize(it.name) == q }
        if (exact.isNotEmpty()) return PersonResults(exact, false)

        val found = all.filter { it.name.isNotEmpty() && Permit.normalize(it.name).contains(q) }
        return if (found.size > MAX_RESULTS) {
            PersonResults(found.take(MAX_RESULTS), true)
        } else {
            PersonResults(found, false)
        }
    }

    fun vehicles(all: List<VehicleGroup>, query: String): VehicleResults {
        val q = Permit.normalize(query).replace(" ", "")
        if (q.isEmpty()) return VehicleResults(emptyList(), false)

        val digitsOnly = q.all { it.isDigit() || it in ARABIC_DIGITS }

        if (!digitsOnly) {
            // Has letters -> exact plate match, as written in the sheet
            val g = all.firstOrNull { it.plate.replace(" ", "") == q }
            return VehicleResults(listOfNotNull(g), false)
        }

        // Digits only -> every plate containing this number anywhere
        val found = all.filter { it.plate.replace(" ", "").contains(q) }
        return if (found.size > MAX_RESULTS) {
            VehicleResults(found.take(MAX_RESULTS), true)
        } else {
            VehicleResults(found, false)
        }
    }
}
