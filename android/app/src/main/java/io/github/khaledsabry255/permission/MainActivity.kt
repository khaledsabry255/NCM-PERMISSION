package io.github.khaledsabry255.permission

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.khaledsabry255.permission.data.Prefs
import io.github.khaledsabry255.permission.ui.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = Prefs(this)

        setContent {
            var lang by rememberSaveable { mutableStateOf(prefs.lang) }
            // The stored flag is the source of truth: a saved-state snapshot can
            // outlive it and disagree after the process is recreated.
            var unlocked by remember { mutableStateOf(prefs.isUnlocked()) }

            val strings = stringsFor(lang)
            val direction = if (lang == Lang.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Palette.Gold,
                    background = Palette.BgDeep,
                    surface = Palette.BgDeep,
                    onBackground = Palette.TextMain,
                    onSurface = Palette.TextMain
                )
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Palette.BgDeep)
                    ) {
                        if (!unlocked) {
                            LockScreen(
                                strings = strings,
                                onUnlocked = {
                                    prefs.markUnlocked()
                                    unlocked = true
                                }
                            )
                        } else {
                            SearchScreen(strings = strings, lang = lang)
                        }

                        // Rendered once, at the root, above both screens, and pinned to a
                        // physical corner (AbsoluteAlignment) — so it cannot move when the
                        // header collapses or when the layout direction flips.
                        LangSwitch(
                            lang = lang,
                            onLangChange = { newLang ->
                                lang = newLang
                                prefs.lang = newLang
                            },
                            modifier = Modifier
                                .align(AbsoluteAlignment.TopRight)
                                .statusBarsPadding()
                                // absolutePadding, not padding: `end` would flip sides in RTL.
                                .absolutePadding(top = 14.dp, right = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
