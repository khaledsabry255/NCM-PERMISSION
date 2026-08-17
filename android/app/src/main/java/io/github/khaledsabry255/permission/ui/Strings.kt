package io.github.khaledsabry255.permission.ui

import io.github.khaledsabry255.permission.data.PermitKind
import io.github.khaledsabry255.permission.data.PermitStatus

enum class Lang { AR, EN }

/**
 * In-app translation table. Deliberately not Android string resources: the AR/EN
 * toggle has to switch instantly without recreating the activity or touching the
 * device locale.
 */
data class Strings(
    val appTitle: String,
    val secureAccess: String,
    val pinLabel: String,
    val pinHint: String,
    val pinError: String,
    val brandSub: String,
    val statusUpdating: String,
    val statusOnline: String,
    val statusError: String,
    val tabIndividuals: String,
    val tabVehicles: String,
    val searchPlaceholderInd: String,
    val searchPlaceholderVeh: String,
    val searchBtn: String,
    val loadingData: String,
    val retry: String,
    val hintInd: String,
    val hintVeh: String,
    val noName: String,
    val noId: String,
    val permitTag: String,
    val sendDate: String,
    val notSpecified: String,
    val fieldJob: String,
    val fieldAddress: String,
    val fieldSender: String,
    val fieldNotes: String,
    val fieldType: String,
    val fieldOwner: String,
    val permitPending: String,
    val permitUndetermined: String,
    val truncatedName: String,
    val truncatedPlate: String,
    private val noResultIndFn: (String) -> String,
    private val noResultVehFn: (String) -> String,
    private val noPlateContainsFn: (String) -> String,
    private val permitCountFn: (Int) -> String,
    private val loadErrorFn: (String) -> String
) {
    fun noResultInd(q: String) = noResultIndFn(q)
    fun noResultVeh(q: String) = noResultVehFn(q)
    fun noPlateContains(q: String) = noPlateContainsFn(q)
    fun permitCount(n: Int) = permitCountFn(n)
    fun loadError(detail: String) = loadErrorFn(detail)
}

/**
 * Permit values arrive from the sheet in Arabic. In English mode the known
 * phrases are mapped; anything unrecognised (dates, free text) is shown exactly
 * as written. Longest/most specific phrases come first.
 */
private val PERMIT_PHRASE_EN = listOf(
    "الرخصه منتهيه" to "License expired",
    "الرخصة منتهية" to "License expired",
    "غيرت لوحات عايزة جديد" to "Plates changed - new permit required",
    "غيرت لوحات" to "Plates changed",
    "عايز تجديد" to "Renewal required",
    "تجديد" to "Renewal",
    "مؤقت" to "Temporary",
    "منع" to "Banned"
)

fun permitText(status: PermitStatus, lang: Lang, s: Strings): String = when (status.kind) {
    PermitKind.PENDING -> s.permitPending
    PermitKind.UNDETERMINED -> s.permitUndetermined
    PermitKind.RAW -> {
        if (lang == Lang.EN) {
            PERMIT_PHRASE_EN.firstOrNull { status.raw.contains(it.first) }?.second ?: status.raw
        } else {
            status.raw
        }
    }
}

val AR_STRINGS = Strings(
    appTitle = "بحث التصاريح",
    secureAccess = "SECURE ACCESS",
    pinLabel = "أدخل رمز الدخول",
    pinHint = "اضغط هنا لفتح لوحة المفاتيح",
    pinError = "رمز الدخول غير صحيح",
    brandSub = "PERMISSION SEARCH",
    statusUpdating = "جاري التحديث",
    statusOnline = "متصل",
    statusError = "خطأ في الاتصال",
    tabIndividuals = "الأفراد",
    tabVehicles = "المركبات",
    searchPlaceholderInd = "الرقم القومى أو الاسم",
    searchPlaceholderVeh = "رقم السيارة",
    searchBtn = "بحث",
    loadingData = "جاري تحميل البيانات",
    retry = "إعادة المحاولة",
    hintInd = "ابحث بالرقم القومى أو الاسم لعرض سجل التصريح",
    hintVeh = "ابحث برقم اللوحة لعرض سجل التصريح",
    noName = "بدون اسم",
    noId = "غير مسجل",
    permitTag = "تصريح",
    sendDate = "تاريخ الإرسال",
    notSpecified = "غير محدد",
    fieldJob = "الوظيفة",
    fieldAddress = "العنوان",
    fieldSender = "المرسل",
    fieldNotes = "ملاحظات",
    fieldType = "النوع",
    fieldOwner = "تابعه",
    permitPending = "في انتظار الرد",
    permitUndetermined = "غير محدد موقفه حتى الآن",
    truncatedName = "فيه نتايج كتير مطابقة، ظاهر أول 50 بس. اكتب جزء أطول من الاسم عشان تضيّق البحث.",
    truncatedPlate = "فيه نتايج كتير مطابقة، ظاهر أول 50 بس. اكتب رقم أطول عشان تضيّق البحث.",
    noResultIndFn = { q -> "مفيش نتيجة مطابقة لـ \"$q\". تأكد إن الرقم القومى أو الاسم مكتوب زي ما هو بالظبط في الشيت." },
    noResultVehFn = { q -> "مفيش نتيجة مطابقة لرقم السيارة \"$q\". تأكد إنه مكتوب زي ما هو بالظبط في الشيت." },
    noPlateContainsFn = { q -> "مفيش أي سيارة رقمها فيه \"$q\"." },
    permitCountFn = { n -> "$n " + if (n == 1) "تصريح" else "تصاريح" },
    loadErrorFn = { d -> "حصل خطأ في تحميل البيانات ($d). اتأكد إن فيه اتصال بالإنترنت وجرّب تاني." }
)

val EN_STRINGS = Strings(
    appTitle = "Permit Search",
    secureAccess = "SECURE ACCESS",
    pinLabel = "Enter access code",
    pinHint = "Tap here to open the keyboard",
    pinError = "Incorrect access code",
    brandSub = "PERMISSION SEARCH",
    statusUpdating = "Updating",
    statusOnline = "Connected",
    statusError = "Connection error",
    tabIndividuals = "Individuals",
    tabVehicles = "Vehicles",
    searchPlaceholderInd = "National ID or name",
    searchPlaceholderVeh = "Plate number",
    searchBtn = "Search",
    loadingData = "Loading data",
    retry = "Retry",
    hintInd = "Search by national ID or name to view the permit record",
    hintVeh = "Search by plate number to view the permit record",
    noName = "No name",
    noId = "Not registered",
    permitTag = "Permit",
    sendDate = "Date sent",
    notSpecified = "Not specified",
    fieldJob = "Job title",
    fieldAddress = "Address",
    fieldSender = "Sender",
    fieldNotes = "Notes",
    fieldType = "Type",
    fieldOwner = "Belongs to",
    permitPending = "Awaiting response",
    permitUndetermined = "Status not yet determined",
    truncatedName = "Too many matches - showing the first 50 only. Type more of the name to narrow it down.",
    truncatedPlate = "Too many matches - showing the first 50 only. Type a longer number to narrow it down.",
    noResultIndFn = { q -> "No match found for \"$q\". Make sure the national ID or name is written exactly as it appears in the sheet." },
    noResultVehFn = { q -> "No match found for plate \"$q\". Make sure it is written exactly as it appears in the sheet." },
    noPlateContainsFn = { q -> "No vehicle has a plate containing \"$q\"." },
    permitCountFn = { n -> "$n " + if (n == 1) "permit" else "permits" },
    loadErrorFn = { d -> "Failed to load the data ($d). Check your internet connection and try again." }
)

fun stringsFor(lang: Lang): Strings = if (lang == Lang.AR) AR_STRINGS else EN_STRINGS
