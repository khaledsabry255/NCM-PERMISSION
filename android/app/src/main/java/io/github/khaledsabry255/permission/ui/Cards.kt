package io.github.khaledsabry255.permission.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledsabry255.permission.data.*

@Composable
fun PersonCard(
    group: PersonGroup,
    strings: Strings,
    lang: Lang,
    aseColumns: List<String> = emptyList()
) {
    RecordCard(
        count = group.records.size,
        title = group.name.ifEmpty { strings.noName },
        subtitle = group.nationalId.ifEmpty { strings.noId },
        subtitleHighlighted = true,
        strings = strings
    ) { index ->
        val rec = group.records[index]
        OccurrenceBody(
            status = Permit.classify(rec.permitRaw),
            day = rec.day,
            strings = strings,
            lang = lang,
            fields = buildList {
                add(strings.fieldJob to rec.job)
                add(strings.fieldAddress to rec.address)
                add(strings.fieldSender to rec.sender)
                add(strings.fieldNotes to rec.note)
                if (rec.mailType.isNotEmpty()) add(strings.fieldMailType to rec.mailType)
            },
            // Label/value pairs for the ASE DATA block, dropping empty cells.
            extraGroup = aseColumns.mapIndexedNotNull { i, label ->
                val v = rec.ase.getOrNull(i).orEmpty()
                if (v.isEmpty()) null else aseLabel(label, lang) to v
            }
        )
    }
}

@Composable
fun VehicleCard(group: VehicleGroup, strings: Strings, lang: Lang) {
    RecordCard(
        count = group.records.size,
        title = group.plate,
        subtitle = null,
        subtitleHighlighted = false,
        strings = strings
    ) { index ->
        val rec = group.records[index]
        OccurrenceBody(
            status = Permit.classify(rec.permitRaw),
            day = rec.day,
            strings = strings,
            lang = lang,
            fields = buildList {
                add(strings.fieldType to rec.type)
                add(strings.fieldOwner to rec.owner)
                if (rec.mailType.isNotEmpty()) add(strings.fieldMailType to rec.mailType)
            }
        )
    }
}

@Composable
private fun RecordCard(
    count: Int,
    title: String,
    subtitle: String?,
    subtitleHighlighted: Boolean,
    strings: Strings,
    body: @Composable (Int) -> Unit
) {
    // Latest occurrence is the one shown first, as agreed for the web version.
    var selected by rememberSaveable(title, count) { mutableIntStateOf(count - 1) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Glass)
            .border(BorderStroke(1.dp, Palette.GlassBorder), RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f, fill = false)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 0 until count) {
                        OccurrenceChip(
                            label = (i + 1).toString(),
                            active = i == selected,
                            onClick = { selected = i }
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                CountBadge(strings.permitCount(count))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                title,
                color = Palette.TextMain,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(Palette.Card)
                    .border(BorderStroke(1.dp, Palette.Edge), RoundedCornerShape(11.dp))
                    .padding(vertical = 13.dp, horizontal = 10.dp)
            )

            if (subtitle != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    subtitle,
                    color = if (subtitleHighlighted) Palette.GoldSoft else Palette.TextDim,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (subtitleHighlighted) Palette.BgMid else Palette.Card)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (subtitleHighlighted) Palette.Gold else Palette.Edge
                            ),
                            RoundedCornerShape(11.dp)
                        )
                        .padding(vertical = 11.dp, horizontal = 10.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
        }

        Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 18.dp)) {
            body(selected.coerceIn(0, count - 1))
        }
    }
}

@Composable
private fun OccurrenceBody(
    status: PermitStatus,
    day: String,
    strings: Strings,
    lang: Lang,
    fields: List<Pair<String, String>>,
    extraGroup: List<Pair<String, String>> = emptyList()
) {
    PermitBanner(status, strings, lang)
    Spacer(Modifier.height(12.dp))
    GroupHeading("NCM DATA")
    Spacer(Modifier.height(8.dp))
    val sent = Permit.splitDateAndNote(day)
    val sentDate = sent.date
    val sentText = when {
        sentDate != null && sent.note.isNotEmpty() -> sentDate + " " + sent.note
        sentDate != null -> sentDate
        sent.note.isNotEmpty() -> sent.note
        else -> strings.notSpecified
    }
    FieldRows(listOf(strings.sendDate to sentText) + fields)
    if (extraGroup.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        GroupHeading("ASE DATA")
        Spacer(Modifier.height(8.dp))
        FieldRows(extraGroup)
    }
}

/** The pale blue plate the site heads each block of rows with. */
@Composable
private fun GroupHeading(text: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Palette.Sky)
            .border(BorderStroke(1.dp, Palette.SkyLine), RoundedCornerShape(9.dp))
            .padding(vertical = 9.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Palette.TextMain,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun PermitBanner(status: PermitStatus, strings: Strings, lang: Lang) {
    val c = colorsFor(status.level)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(c.background)
            .border(BorderStroke(1.5.dp, c.border), RoundedCornerShape(11.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(c.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                PermitIcon(status.level, Palette.OnGold)
            }
            Spacer(Modifier.width(10.dp))
            Text(strings.permitTag, color = c.content, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        val shown = permitText(status, lang, strings)
        Column(
            Modifier.weight(1f, fill = false),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                shown.date.orEmpty(),
                color = c.content,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.End
            )
            if (shown.note.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    shown.note,
                    color = c.content.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/** Small status glyphs drawn by hand so the app doesn't pull in the extended icon set. */
@Composable
private fun PermitIcon(level: PermitLevel, tint: Color) {
    Canvas(Modifier.size(18.dp)) {
        val w = size.minDimension
        val u = w / 24f
        val stroke = Stroke(width = 2.3f * u, cap = StrokeCap.Round)
        val center = Offset(12 * u, 12 * u)

        when (level) {
            PermitLevel.OK -> {
                drawLine(tint, Offset(4 * u, 12 * u), Offset(9 * u, 17 * u), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(9 * u, 17 * u), Offset(20 * u, 6 * u), stroke.width, StrokeCap.Round)
            }
            PermitLevel.SOON -> {
                drawCircle(tint, radius = 9 * u, center = center, style = stroke)
                drawLine(tint, center, Offset(12 * u, 7 * u), stroke.width, StrokeCap.Round)
                drawLine(tint, center, Offset(15 * u, 15 * u), stroke.width, StrokeCap.Round)
            }
            PermitLevel.BAD -> {
                drawCircle(tint, radius = 9 * u, center = center, style = stroke)
                drawLine(tint, Offset(12 * u, 7 * u), Offset(12 * u, 13 * u), stroke.width, StrokeCap.Round)
                drawCircle(tint, radius = 1.2f * u, center = Offset(12 * u, 16.5f * u))
            }
            PermitLevel.BAN -> {
                drawCircle(tint, radius = 9 * u, center = center, style = stroke)
                drawLine(tint, Offset(5.5f * u, 5.5f * u), Offset(18.5f * u, 18.5f * u), stroke.width, StrokeCap.Round)
            }
            PermitLevel.UNCLEAR -> {
                drawCircle(tint, radius = 9 * u, center = center, style = stroke)
                drawArc(
                    color = tint,
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = Offset(9 * u, 7 * u),
                    size = androidx.compose.ui.geometry.Size(6 * u, 6 * u),
                    style = stroke
                )
                drawLine(tint, Offset(12 * u, 12 * u), Offset(12 * u, 14 * u), stroke.width, StrokeCap.Round)
                drawCircle(tint, radius = 1.1f * u, center = Offset(12 * u, 16.5f * u))
            }
        }
    }
}

@Composable
private fun FieldRows(fields: List<Pair<String, String>>) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        fields.forEach { (key, value) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Palette.RowBg)
                    .border(BorderStroke(1.dp, Palette.RowLine), RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(key, color = Palette.TextDim, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(14.dp))
                Text(
                    value.ifEmpty { "—" },
                    color = Palette.TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HorizontalDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Palette.LineSoft)
    )
}

@Composable
private fun OccurrenceChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .heightIn(min = 30.dp)
            .widthIn(min = 30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Palette.Gold else Palette.Card)
            .border(
                BorderStroke(1.dp, if (active) Palette.Gold else Palette.Line),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (active) Palette.OnGold else Palette.TextDim,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CountBadge(text: String) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(Palette.CountBg)
            .border(BorderStroke(1.dp, Palette.CountBr), CircleShape)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(text, color = Palette.CountInk, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}
