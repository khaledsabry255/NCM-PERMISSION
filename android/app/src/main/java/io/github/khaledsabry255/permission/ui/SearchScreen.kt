package io.github.khaledsabry255.permission.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledsabry255.permission.data.*

private sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val data: SheetData) : LoadState
    data class Failed(val detail: String) : LoadState
}

@Composable
fun SearchScreen(strings: Strings, lang: Lang) {

    var state by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    var attempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(attempt) {
        state = LoadState.Loading
        state = try {
            LoadState.Ready(SheetRepository.load())
        } catch (e: Exception) {
            LoadState.Failed(e.message ?: e.javaClass.simpleName)
        }
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var indQuery by rememberSaveable { mutableStateOf("") }
    var vehQuery by rememberSaveable { mutableStateOf("") }
    var indSubmitted by rememberSaveable { mutableStateOf("") }
    var vehSubmitted by rememberSaveable { mutableStateOf("") }

    // Once a search is live, everything that isn't a result gets out of the way.
    val searchMode = if (tab == 0) indSubmitted.isNotBlank() else vehSubmitted.isNotBlank()

    Column(Modifier.fillMaxSize()) {

        Header(strings, state, searchMode)

        Column(Modifier.padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(if (searchMode) 10.dp else 18.dp))

            Tabs(
                strings = strings,
                selected = tab,
                onSelect = { tab = it }
            )

            Spacer(Modifier.height(if (searchMode) 10.dp else 16.dp))

            if (tab == 0) {
                SearchCard(
                    value = indQuery,
                    placeholder = strings.searchPlaceholderInd,
                    buttonLabel = strings.searchBtn,
                    compact = searchMode,
                    onValueChange = { indQuery = it },
                    onSubmit = { indSubmitted = indQuery },
                    onClear = { indQuery = ""; indSubmitted = "" }
                )
            } else {
                SearchCard(
                    value = vehQuery,
                    placeholder = strings.searchPlaceholderVeh,
                    buttonLabel = strings.searchBtn,
                    compact = searchMode,
                    onValueChange = { vehQuery = it },
                    onSubmit = { vehSubmitted = vehQuery },
                    onClear = { vehQuery = ""; vehSubmitted = "" }
                )
            }

            when (val s = state) {
                is LoadState.Loading -> StatusLine(strings.loadingData, showSpinner = true)
                is LoadState.Failed -> ErrorState(strings, s.detail) { attempt++ }
                is LoadState.Ready -> {
                    if (tab == 0) {
                        IndividualResults(s.data.people, indSubmitted, strings, lang, s.data.aseColumns)
                    } else {
                        VehicleResults(s.data.vehicles, vehSubmitted, strings, lang)
                    }
                }
            }
        }
    }
}

@Composable
private fun IndividualResults(
    all: List<PersonGroup>,
    query: String,
    strings: Strings,
    lang: Lang,
    aseColumns: List<String> = emptyList()
) {
    if (query.isBlank()) {
        HintState(strings.hintInd) { drawPersonIcon(it) }
        return
    }
    val results = remember(all, query) { Search.people(all, query) }

    if (results.groups.isEmpty()) {
        EmptyState(strings.noResultInd(Permit.normalize(query)))
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        if (results.truncated) {
            item { StatusLine(strings.truncatedName, showSpinner = false) }
        }
        items(results.groups, key = { it.nationalId + "|" + it.name }) { group ->
            PersonCard(group, strings, lang, aseColumns)
        }
    }
}

@Composable
private fun VehicleResults(
    all: List<VehicleGroup>,
    query: String,
    strings: Strings,
    lang: Lang
) {
    if (query.isBlank()) {
        HintState(strings.hintVeh) { drawCarIcon(it) }
        return
    }
    val results = remember(all, query) { Search.vehicles(all, query) }
    val normalized = Permit.normalize(query)

    if (results.groups.isEmpty()) {
        val digitsOnly = normalized.replace(" ", "").all { it.isDigit() || it in '٠'..'٩' }
        EmptyState(
            if (digitsOnly) strings.noPlateContains(normalized)
            else strings.noResultVeh(normalized)
        )
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        if (results.truncated) {
            item { StatusLine(strings.truncatedPlate, showSpinner = false) }
        }
        items(results.groups, key = { it.plate }) { group ->
            VehicleCard(group, strings, lang)
        }
    }
}

@Composable
private fun Header(
    strings: Strings,
    state: LoadState,
    searchMode: Boolean
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Palette.BgDeep)
            .statusBarsPadding()
            .padding(
                top = if (searchMode) 8.dp else 20.dp,
                bottom = if (searchMode) 8.dp else 14.dp
            )
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (searchMode) {
                NcmLogo(markSize = 21.sp, subSize = 5.8.sp, subSpacing = 2.1.sp)
            } else {
                NcmLogo(markSize = 40.sp, subSize = 9.4.sp, subSpacing = 3.8.sp)

                Spacer(Modifier.height(11.dp))

                val (dotColor, label) = when (state) {
                    is LoadState.Loading -> Palette.Soon to strings.statusUpdating
                    is LoadState.Failed -> Palette.Bad to strings.statusError
                    is LoadState.Ready -> Palette.Ok to strings.statusOnline
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(label, color = Palette.TextDim, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.07f))
    )
}

@Composable
private fun Tabs(strings: Strings, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabButton(strings.tabIndividuals, selected == 0, Modifier.weight(1f), { onSelect(0) }) { drawPersonIcon(it) }
        TabButton(strings.tabVehicles, selected == 1, Modifier.weight(1f), { onSelect(1) }) { drawCarIcon(it) }
    }
}

@Composable
private fun TabButton(
    label: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    icon: DrawScope.(Color) -> Unit
) {
    val content = if (active) Palette.OnGold else Palette.TextDim
    Row(
        modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) Palette.Gold else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(16.dp)) { icon(content) }
        Spacer(Modifier.width(7.dp))
        Text(label, color = content, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SearchCard(
    value: String,
    placeholder: String,
    buttonLabel: String,
    compact: Boolean,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxWidth()
            .then(
                if (compact) Modifier
                else Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.22f))
                .border(BorderStroke(1.dp, Palette.GlassBorder), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(Modifier.size(18.dp)) { drawSearchIcon(Palette.TextDim) }
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Palette.TextDim, fontSize = 15.5.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Palette.TextMain, fontSize = 15.5.sp),
                    cursorBrush = SolidColor(Palette.Gold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboard?.hide()
                        onSubmit()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp)
                )
            }
            if (value.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.09f))
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(12.dp)) { drawCloseIcon(Palette.TextDim) }
                }
            }
        }

        // Once results are on screen the button is dead weight — the field's own
        // Search key does the same job.
        if (!compact) {
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Palette.Gold)
                    .clickable {
                        keyboard?.hide()
                        onSubmit()
                    }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    buttonLabel,
                    color = Palette.OnGold,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun StatusLine(text: String, showSpinner: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                color = Palette.Gold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            color = Palette.TextDim,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HintState(text: String, icon: DrawScope.(Color) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(34.dp)) { icon(Palette.TextDim.copy(alpha = 0.5f)) }
        Spacer(Modifier.height(12.dp))
        Text(text, color = Palette.TextDim, fontSize = 13.5.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(Modifier.size(30.dp)) { drawSearchIcon(Palette.TextDim.copy(alpha = 0.5f)) }
        Spacer(Modifier.height(12.dp))
        Text(text, color = Palette.TextDim, fontSize = 13.5.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorState(strings: Strings, detail: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            strings.loadError(detail),
            color = Palette.Bad,
            fontSize = 13.5.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Palette.Gold)
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 11.dp)
        ) {
            Text(
                strings.retry,
                color = Palette.OnGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/* ---------- Hand-drawn icons (keeps the extended icon dependency out) ---------- */

private fun DrawScope.unit() = size.minDimension / 24f

private fun DrawScope.drawSearchIcon(tint: Color) {
    val u = unit()
    val s = Stroke(width = 2f * u, cap = StrokeCap.Round)
    drawCircle(tint, radius = 7 * u, center = Offset(11 * u, 11 * u), style = s)
    drawLine(tint, Offset(16.7f * u, 16.7f * u), Offset(21 * u, 21 * u), s.width, StrokeCap.Round)
}

private fun DrawScope.drawCloseIcon(tint: Color) {
    val u = unit()
    val w = 2.6f * u
    drawLine(tint, Offset(6 * u, 6 * u), Offset(18 * u, 18 * u), w, StrokeCap.Round)
    drawLine(tint, Offset(18 * u, 6 * u), Offset(6 * u, 18 * u), w, StrokeCap.Round)
}

private fun DrawScope.drawPersonIcon(tint: Color) {
    val u = unit()
    val s = Stroke(width = 2f * u, cap = StrokeCap.Round)
    drawCircle(tint, radius = 4 * u, center = Offset(12 * u, 8 * u), style = s)
    drawArc(
        color = tint,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(4 * u, 14 * u),
        size = Size(16 * u, 12 * u),
        style = s
    )
}

private fun DrawScope.drawCarIcon(tint: Color) {
    val u = unit()
    val s = Stroke(width = 2f * u, cap = StrokeCap.Round)
    drawLine(tint, Offset(3 * u, 13 * u), Offset(4.5f * u, 8 * u), s.width, StrokeCap.Round)
    drawLine(tint, Offset(4.5f * u, 8 * u), Offset(19.5f * u, 8 * u), s.width, StrokeCap.Round)
    drawLine(tint, Offset(19.5f * u, 8 * u), Offset(21 * u, 13 * u), s.width, StrokeCap.Round)
    drawRoundRect(
        color = tint,
        topLeft = Offset(3 * u, 13 * u),
        size = Size(18 * u, 5 * u),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * u),
        style = s
    )
    drawCircle(tint, radius = 1.4f * u, center = Offset(7 * u, 18.5f * u))
    drawCircle(tint, radius = 1.4f * u, center = Offset(17 * u, 18.5f * u))
}
