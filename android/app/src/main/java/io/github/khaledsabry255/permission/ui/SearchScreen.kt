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
fun SearchScreen(strings: Strings, lang: Lang, onLangChange: (Lang) -> Unit) {

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

    Column(Modifier.fillMaxSize().statusBarsPadding()) {

        if (!searchMode) Header(strings, state)

        Column(Modifier.padding(horizontal = 14.dp)) {

            Spacer(Modifier.height(if (searchMode) 10.dp else 12.dp))

            Tabs(
                strings = strings,
                selected = tab,
                lang = lang,
                onSelect = { tab = it },
                onLangChange = onLangChange,
                onRefresh = { attempt++ }
            )

            Spacer(Modifier.height(if (searchMode) 10.dp else 12.dp))

            if (tab == 0) {
                SearchCard(
                    value = indQuery,
                    placeholder = strings.searchPlaceholderInd,
                    buttonLabel = strings.searchBtn,
                    onValueChange = { indQuery = it },
                    onSubmit = { indSubmitted = indQuery },
                    onClear = { indQuery = ""; indSubmitted = "" }
                )
            } else {
                SearchCard(
                    value = vehQuery,
                    placeholder = strings.searchPlaceholderVeh,
                    buttonLabel = strings.searchBtn,
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
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
private fun Header(strings: Strings, state: LoadState) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Palette.BgDeep)
            .padding(vertical = 16.dp)
    ) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NcmLogo(markSize = 40.sp, subSize = 8.6.sp, subSpacing = 2.05.sp)

            Spacer(Modifier.height(10.dp))

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

@Composable
private fun Tabs(
    strings: Strings,
    selected: Int,
    lang: Lang,
    onSelect: (Int) -> Unit,
    onLangChange: (Lang) -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RefreshButton(onRefresh)
        Row(
            Modifier.weight(1f).fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(strings.tabIndividuals, selected == 0, Modifier.weight(1f), { onSelect(0) }) { drawPersonIcon(it) }
            TabButton(strings.tabVehicles, selected == 1, Modifier.weight(1f), { onSelect(1) }) { drawCarIcon(it) }
        }
        LangButton(lang, onLangChange)
    }
}

/** The site's reload control: a plain bordered square carrying a round arrow. */
@Composable
private fun RefreshButton(onRefresh: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Palette.TabOffBg)
            .border(BorderStroke(1.dp, Palette.TabOffBr), RoundedCornerShape(11.dp))
            .clickable(onClick = onRefresh),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) { drawRefreshIcon(Palette.TabOffInk) }
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
    val content = if (active) Palette.TabOnInk else Palette.TabOffInk
    Row(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) Palette.TabOnBg else Palette.TabOffBg)
            .border(
                BorderStroke(1.dp, if (active) Palette.TabOnBr else Palette.TabOffBr),
                RoundedCornerShape(11.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(16.dp)) { icon(content) }
        Spacer(Modifier.width(7.dp))
        Text(label, color = content, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun SearchCard(
    value: String,
    placeholder: String,
    buttonLabel: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClear: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    // The site sets the field and its button side by side on one 45px line.
    Row(
        Modifier.fillMaxWidth().height(45.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(Palette.Field)
                .border(BorderStroke(1.dp, Palette.Line), RoundedCornerShape(9.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(Modifier.size(16.dp)) { drawSearchIcon(Palette.TextDim) }
            Spacer(Modifier.width(9.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = Palette.TextDim,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Palette.TextMain,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Fonts.Sans
                    ),
                    cursorBrush = SolidColor(Palette.Gold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboard?.hide()
                        onSubmit()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Palette.Line)
                        .clickable(onClick = onClear),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(10.dp)) { drawCloseIcon(Palette.TextDim) }
                }
            }
        }

        Box(
            Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(9.dp))
                .background(Palette.Gold)
                .clickable {
                    keyboard?.hide()
                    onSubmit()
                }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                buttonLabel,
                color = Palette.OnGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
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

private fun DrawScope.drawRefreshIcon(tint: Color) {
    val u = unit()
    val s = Stroke(width = 2.1f * u, cap = StrokeCap.Round)
    drawArc(
        color = tint,
        startAngle = 40f,
        sweepAngle = 290f,
        useCenter = false,
        topLeft = Offset(3 * u, 3 * u),
        size = Size(18 * u, 18 * u),
        style = s
    )
    drawLine(tint, Offset(21 * u, 3 * u), Offset(21 * u, 9 * u), s.width, StrokeCap.Round)
    drawLine(tint, Offset(21 * u, 9 * u), Offset(15 * u, 9 * u), s.width, StrokeCap.Round)
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
