package com.gogolook.trustall.demo.feature.urlscan

import android.content.Context
import com.gogolook.trustall.core.urlscan.TrustallUrlScan
import com.gogolook.trustall.core.urlscan.UrlScanProvider
import com.gogolook.trustall.core.urlscan.model.CachePolicy
import com.gogolook.trustall.core.urlscan.model.Level
import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.demo.core.util.ProviderPrefs
import com.gogolook.trustall.demo.core.util.ProviderSelection

/**
 * One URL the custom provider has an answer for.
 *
 * [failWith] turns the answer into [UrlScanResult.Error] carrying that message, which is the one
 * outcome a plain verdict cannot express: the source covers this URL but could not reach its
 * backend. Blank means answer normally with [level].
 */
data class CustomUrlEntry(val url: String, val level: Level, val failWith: String = "")

/**
 * The custom URL-scan provider and the verdicts it serves.
 *
 * This seam has three outcomes rather than two, and the rows exercise all of them: a URL in the
 * list gets its [Level] as a Success, one given a failure message comes back as an Error, and a
 * URL that is not in the list at all returns null, which means "this source does not cover it"
 * and hands the scan to the next provider.
 */
object UrlScanProviders {

    private fun prefs(context: Context) = ProviderPrefs(context, SEAM_KEY)

    fun selection(context: Context): ProviderSelection = prefs(context).selection()

    /** Persists the choice and registers it, taking effect on the next call the SDK makes. */
    fun setSelection(context: Context, selection: ProviderSelection) {
        prefs(context).setSelection(selection)
        apply(context)
    }

    fun entries(context: Context): List<CustomUrlEntry> =
        prefs(context).rows().mapNotNull { row ->
            val parts = row.split(FIELD_SEPARATOR)
            if (parts.size < 3) return@mapNotNull null
            val level = Level.entries.firstOrNull { it.name == parts[1] } ?: return@mapNotNull null
            CustomUrlEntry(url = parts[0], level = level, failWith = parts[2])
        }

    fun add(context: Context, entry: CustomUrlEntry) {
        val kept = entries(context).filterNot { it.url == entry.url }
        write(context, kept + entry)
    }

    fun remove(context: Context, url: String) {
        write(context, entries(context).filterNot { it.url == url })
    }

    /** Registers the stored choice; called from `Application.onCreate` and on every toggle. */
    fun apply(context: Context) {
        val selection = selection(context)
        val custom = if (selection.custom) DemoUrlScanProvider(context) else null
        TrustallUrlScan.setProviders(
            when {
                custom != null && selection.default ->
                    listOf(custom, TrustallUrlScan.defaultProvider)
                custom != null -> listOf(custom)
                selection.default -> null
                else -> emptyList()
            }
        )
    }

    private fun write(context: Context, entries: List<CustomUrlEntry>) {
        prefs(context).setRows(
            entries.map { entry ->
                listOf(entry.url, entry.level.name, entry.failWith).joinToString(FIELD_SEPARATOR)
            }
        )
    }

    private const val SEAM_KEY = "urlscan"
    private const val FIELD_SEPARATOR = "\u0001"
}

internal class DemoUrlScanProvider(context: Context) : UrlScanProvider {

    private val appContext = context.applicationContext

    /** Null, not an Error: a URL absent from the list is one this source does not cover. */
    override suspend fun scan(url: String, cachePolicy: CachePolicy): UrlScanResult? =
        UrlScanProviders.entries(appContext)
            .firstOrNull { it.url == url }
            ?.let { entry ->
                if (entry.failWith.isNotBlank()) {
                    UrlScanResult.Error(url = entry.url, error = Exception(entry.failWith))
                } else {
                    UrlScanResult.Success(url = entry.url, level = entry.level)
                }
            }
}
