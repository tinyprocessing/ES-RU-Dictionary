package com.tinyprocessing.spanishrussian.data

import android.content.Context
import android.content.SharedPreferences

object AppSettings {

    private const val PREFS_NAME = "spanishrussian_settings"
    private const val KEY_MULTITRAN_FALLBACK = "multitran_fallback"
    private const val KEY_MULTITRAN_ALWAYS = "multitran_always"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Always show Multitran results alongside offline results */
    fun isMultitranAlwaysEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_MULTITRAN_ALWAYS, false)
    }

    fun setMultitranAlwaysEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MULTITRAN_ALWAYS, enabled).apply()
    }

    /** Show Multitran results only when no offline results found */
    fun isMultitranFallbackEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_MULTITRAN_FALLBACK, false)
    }

    fun setMultitranFallbackEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MULTITRAN_FALLBACK, enabled).apply()
    }

    /** Should we fetch Multitran for this search? */
    fun shouldFetchOnline(context: Context, hasLocalResults: Boolean): Boolean {
        if (isMultitranAlwaysEnabled(context)) return true
        if (!hasLocalResults && isMultitranFallbackEnabled(context)) return true
        return false
    }
}
