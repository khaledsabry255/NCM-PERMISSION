package io.github.khaledsabry255.permission.data

data class PersonRecord(
    val job: String,
    val address: String,
    val note: String,
    val sender: String,
    val mailType: String,
    /** Values for the ASE DATA block, aligned with [SheetData.aseColumns]. */
    val ase: List<String>,
    val permitRaw: String,
    val day: String,
    val dayMillis: Long?
)

data class PersonGroup(
    val name: String,
    val nationalId: String,
    val records: List<PersonRecord>
)

data class VehicleRecord(
    val type: String,
    val owner: String,
    val mailType: String,
    val permitRaw: String,
    val day: String,
    val dayMillis: Long?
)

data class VehicleGroup(
    val plate: String,
    val records: List<VehicleRecord>
)

data class SheetData(
    val people: List<PersonGroup>,
    val vehicles: List<VehicleGroup>,
    /** Header labels for the ASE DATA columns, in sheet order. */
    val aseColumns: List<String> = emptyList()
)
