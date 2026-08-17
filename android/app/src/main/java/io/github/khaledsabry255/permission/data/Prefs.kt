package io.github.khaledsabry255.permission.data

import android.content.Context
import io.github.khaledsabry255.permission.ui.Lang

/**
 * Device-local settings. Mirrors the web app's localStorage keys and semantics
 * so both versions behave the same way.
 */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("ncm_permits", Context.MODE_PRIVATE)

    companion object {
        /**
         * Bump ACCESS_VERSION to revoke every device that was already unlocked:
         * they all get locked out and must enter the PIN again from scratch.
         */
        const val ACCESS_VERSION = "2"
        const val CORRECT_PIN = "1804"

        private const val KEY_UNLOCKED = "ncm_device_unlocked"
        private const val KEY_LANG = "ncm_lang"
    }

    /** True only when this device was unlocked under the CURRENT access version. */
    fun isUnlocked(): Boolean {
        val stored = sp.getString(KEY_UNLOCKED, null)
        if (stored == ACCESS_VERSION) return true
        // Anything else (an old version) means access was revoked.
        if (stored != null) sp.edit().remove(KEY_UNLOCKED).apply()
        return false
    }

    fun markUnlocked() {
        sp.edit().putString(KEY_UNLOCKED, ACCESS_VERSION).apply()
    }

    var lang: Lang
        get() = if (sp.getString(KEY_LANG, "ar") == "en") Lang.EN else Lang.AR
        set(value) {
            sp.edit().putString(KEY_LANG, if (value == Lang.EN) "en" else "ar").apply()
        }
}
