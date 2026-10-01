package com.gogolook.trustall.demo.core.util

import android.content.Context

/**
 * Which providers a seam has registered.
 *
 * The SDK takes these two in one of two shapes. Number search and URL scan take an ordered list,
 * so both can be on at once and the custom one is tried first. The offline database and call log
 * upload take a single provider, so exactly one of these is on.
 */
data class ProviderSelection(
    val custom: Boolean = false,
    val default: Boolean = true,
)

/**
 * Stores one seam's provider choice and the rows its custom provider serves.
 *
 * This only remembers the choice across launches — each feature's own `apply()` is what
 * registers it, at startup and again whenever the choice changes. Rows are opaque strings: each feature encodes and parses its own, which keeps this shared piece
 * free of any one seam's model.
 */
class ProviderPrefs(context: Context, private val seamKey: String) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun selection(): ProviderSelection = ProviderSelection(
        custom = prefs.getBoolean(key("custom"), false),
        default = prefs.getBoolean(key("default"), true),
    )

    fun setSelection(selection: ProviderSelection) {
        prefs.edit()
            .putBoolean(key("custom"), selection.custom)
            .putBoolean(key("default"), selection.default)
            .apply()
    }

    /** Rows in the order they were added; empty when the custom provider has nothing to serve. */
    fun rows(): List<String> =
        prefs.getString(key("rows"), null)
            ?.split(ROW_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun setRows(rows: List<String>) {
        prefs.edit().putString(key("rows"), rows.joinToString(ROW_SEPARATOR)).apply()
    }

    private fun key(suffix: String) = "provider_${seamKey}_$suffix"

    private companion object {
        const val PREFS_NAME = "trustall_demo_prefs"
        const val ROW_SEPARATOR = "\n"
    }
}
