package io.github.khaledsabry255.permission.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.khaledsabry255.permission.R

/**
 * The two faces the web app is drawn in, bundled in the APK so the phone renders
 * exactly what the site does. Without them Android falls back to Roboto and the
 * app reads as a different product even when every colour matches.
 */
object Fonts {

    /** Tajawal — the body face, used for everything that is not the wordmark. */
    val Sans = FontFamily(
        Font(R.font.tajawal_regular, FontWeight.Normal),
        Font(R.font.tajawal_medium, FontWeight.Medium),
        Font(R.font.tajawal_bold, FontWeight.Bold),
        Font(R.font.tajawal_extrabold, FontWeight.ExtraBold),
        Font(R.font.tajawal_black, FontWeight.Black)
    )

    /** Montserrat — the wordmark and the line under it, nothing else. */
    val Display = FontFamily(
        Font(R.font.montserrat_semibold, FontWeight.SemiBold),
        Font(R.font.montserrat_extrabold, FontWeight.ExtraBold)
    )
}
