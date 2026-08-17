package io.github.khaledsabry255.permission.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.khaledsabry255.permission.data.Prefs
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * PIN gate. Input comes from the device keyboard only — an in-app number pad
 * fights the system keyboard and produces doubled digits, so there isn't one.
 */
@Composable
fun LockScreen(
    strings: Strings,
    lang: Lang,
    onLangChange: (Lang) -> Unit,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    val shake by animateFloatAsState(
        targetValue = if (error) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0; -5f at 80; 5f at 160; -5f at 240; 3f at 320; 0f at 400
        },
        label = "shake"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    LaunchedEffect(error) {
        if (error) {
            delay(420)
            pin = ""
            error = false
        }
    }

    Box(Modifier.fillMaxSize()) {

        LangSwitch(
            lang = lang,
            onLangChange = onLangChange,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(20.dp)
        )

        Column(
            Modifier
                .align(Alignment.Center)
                .widthIn(max = 340.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShieldLogo(size = 72.dp, strokeWidth = 4.5f, showInnerRing = true)

            Spacer(Modifier.height(14.dp))

            Text(
                strings.appTitle,
                color = Palette.TextMain,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                strings.secureAccess,
                color = Palette.GoldSoft,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(36.dp))

            Text(strings.pinLabel, color = Palette.TextDim, fontSize = 13.sp)

            Spacer(Modifier.height(18.dp))

            BasicTextField(
                value = pin,
                onValueChange = { new ->
                    if (error) return@BasicTextField
                    val digits = new.filter(Char::isDigit).take(4)
                    pin = digits
                    if (digits.length == 4) {
                        if (digits == Prefs.CORRECT_PIN) {
                            keyboard?.hide()
                            onUnlocked()
                        } else {
                            error = true
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()
                    .height(56.dp)
                    .offset { IntOffset(shake.roundToInt(), 0) },
                decorationBox = { inner ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(
                                BorderStroke(1.dp, if (error) Palette.Bad else Palette.Gold.copy(alpha = 0.25f)),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            repeat(4) { i ->
                                val filled = i < pin.length
                                Box(
                                    Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) Palette.Gold else Color.Transparent)
                                        .border(
                                            BorderStroke(
                                                1.5.dp,
                                                when {
                                                    error -> Palette.Bad
                                                    filled -> Palette.Gold
                                                    else -> Palette.Gold.copy(alpha = 0.4f)
                                                }
                                            ),
                                            CircleShape
                                        )
                                )
                            }
                        }
                        Box(Modifier.alpha(0f)) { inner() }
                    }
                }
            )

            Spacer(Modifier.height(12.dp))
            Text(strings.pinHint, color = Color(0xFF5C6B68), fontSize = 11.sp)

            Spacer(Modifier.height(8.dp))
            Text(
                text = strings.pinError,
                color = Palette.Bad,
                fontSize = 12.5.sp,
                modifier = Modifier.alpha(if (error) 1f else 0f)
            )
        }
    }
}
