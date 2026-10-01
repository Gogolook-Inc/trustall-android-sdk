package com.gogolook.trustall.demo.feature.search

import android.content.Context
import com.gogolook.trustall.core.numbersearch.NumberSearchProvider
import com.gogolook.trustall.core.numbersearch.TrustallNumberSearch
import com.gogolook.trustall.core.numbersearch.model.OnlineNumberInfo
import com.gogolook.trustall.demo.core.util.ProviderPrefs
import com.gogolook.trustall.demo.core.util.ProviderSelection

/** One number the custom provider knows about: every field of [OnlineNumberInfo]. */
data class CustomNumberEntry(
    val number: String,
    val name: String = "",
    val bizCategory: String = "",
    val spamCategory: String = "",
    val spamLevel: OnlineNumberInfo.SpamLevel = OnlineNumberInfo.SpamLevel.UNLIKELY,
)

/**
 * The custom number-search provider and the rows it answers from.
 *
 * Both halves are live. [apply] only swaps a field the SDK reads on every lookup, so the
 * checkboxes take effect at once; the rows are read on every lookup too, the same way a real
 * integrator's provider would query a live backend.
 */
object NumberSearchProviders {

    private fun prefs(context: Context) = ProviderPrefs(context, SEAM_KEY)

    fun selection(context: Context): ProviderSelection = prefs(context).selection()

    /** Persists the choice and registers it, taking effect on the next call the SDK makes. */
    fun setSelection(context: Context, selection: ProviderSelection) {
        prefs(context).setSelection(selection)
        apply(context)
    }

    fun entries(context: Context): List<CustomNumberEntry> =
        prefs(context).rows().mapNotNull { row ->
            val parts = row.split(FIELD_SEPARATOR)
            if (parts.size < 5) return@mapNotNull null
            CustomNumberEntry(
                number = parts[0],
                name = parts[1],
                bizCategory = parts[2],
                spamCategory = parts[3],
                spamLevel = OnlineNumberInfo.SpamLevel.entries
                    .firstOrNull { it.name == parts[4] }
                    ?: OnlineNumberInfo.SpamLevel.UNLIKELY,
            )
        }

    fun add(context: Context, entry: CustomNumberEntry) {
        val kept = entries(context).filterNot { it.number == entry.number }
        write(context, kept + entry)
    }

    fun remove(context: Context, number: String) {
        write(context, entries(context).filterNot { it.number == number })
    }

    /** Registers the stored choice; called from `Application.onCreate` and on every toggle. */
    fun apply(context: Context) {
        val selection = selection(context)
        val custom = if (selection.custom) DemoNumberSearchProvider(context) else null
        TrustallNumberSearch.setProviders(
            when {
                custom != null && selection.default ->
                    listOf(custom, TrustallNumberSearch.defaultProvider)
                custom != null -> listOf(custom)
                // Null restores the SDK's own; an empty list leaves it with no source at all.
                selection.default -> null
                else -> emptyList()
            }
        )
    }

    private fun write(context: Context, entries: List<CustomNumberEntry>) {
        prefs(context).setRows(
            entries.map { entry ->
                listOf(
                    entry.number,
                    entry.name,
                    entry.bizCategory,
                    entry.spamCategory,
                    entry.spamLevel.name,
                ).joinToString(FIELD_SEPARATOR)
            }
        )
    }

    private const val SEAM_KEY = "numbersearch"
    private const val FIELD_SEPARATOR = "\u0001"
}

/**
 * Answers from the rows on the Number Search screen, and returns null for anything else so the
 * lookup falls through to whatever comes next in the chain.
 *
 * Implements only [getNumberInfo]; the three cache methods come from the interface's default
 * bodies, which is the honest shape for a source with no cache of its own.
 */
internal class DemoNumberSearchProvider(context: Context) : NumberSearchProvider {

    private val appContext = context.applicationContext

    override suspend fun getNumberInfo(e164: String, isForceUpdate: Boolean): OnlineNumberInfo? =
        NumberSearchProviders.entries(appContext)
            .firstOrNull { it.number == e164 }
            ?.let { entry ->
                OnlineNumberInfo(
                    number = entry.number,
                    name = entry.name,
                    bizCategory = entry.bizCategory,
                    spamCategory = entry.spamCategory,
                    spamLevel = entry.spamLevel,
                )
            }
}
