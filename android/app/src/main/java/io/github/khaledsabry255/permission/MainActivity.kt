package io.github.khaledsabry255.permission

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.github.khaledsabry255.permission.data.Prefs
import io.github.khaledsabry255.permission.ui.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = Prefs(this)

        setContent {
            var lang by rememberSaveable { mutableStateOf(prefs.lang) }
            var unlocked by rememberSaveable { mutableStateOf(prefs.isUnlocked()) }

            val strings = stringsFor(lang)
            val direction = if (lang == Lang.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

            val onLangChange: (Lang) -> Unit = { newLang ->
                lang = newLang
                prefs.lang = newLang
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
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
                                lang = lang,
                                onLangChange = onLangChange,
                                onUnlocked = {
                                    prefs.markUnlocked()
                                    unlocked = true
                                }
                            )
                        } else {
                            SearchScreen(
                                strings = strings,
                                lang = lang,
                                onLangChange = onLangChange
                            )
                        }
                    }
                }
            }
        }
    }
}
