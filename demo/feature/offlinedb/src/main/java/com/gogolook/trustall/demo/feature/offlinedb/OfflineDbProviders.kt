package com.gogolook.trustall.demo.feature.offlinedb

import android.content.Context
import com.gogolook.trustall.core.offlinedb.OfflineDbProvider
import com.gogolook.trustall.core.offlinedb.TrustallOfflineDb
import com.gogolook.trustall.core.offlinedb.model.DownloadState
import com.gogolook.trustall.core.offlinedb.model.OfflineDbProfile
import com.gogolook.trustall.core.offlinedb.model.OfflineNumberInfo
import com.gogolook.trustall.demo.core.util.ProviderPrefs
import com.gogolook.trustall.demo.core.util.ProviderSelection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** One number the custom offline database knows about: every field of [OfflineNumberInfo]. */
data class CustomOfflineEntry(
    val number: String,
    val name: String = "",
    val spamCategory: String = "",
    val spamLevel: OfflineNumberInfo.SpamLevel = OfflineNumberInfo.SpamLevel.UNLIKELY,
)

/**
 * The custom offline database and the rows it answers from.
 *
 * This seam takes a single provider rather than a list: an offline database is a stateful unit,
 * a file with a version, so splitting lookups, the profile and the download across several of
 * them would leave the three disagreeing about which database is in place. The screen therefore
 * offers a choice between the two rather than a combination.
 */
object OfflineDbProviders {

    private fun prefs(context: Context) = ProviderPrefs(context, SEAM_KEY)

    fun selection(context: Context): ProviderSelection = prefs(context).selection()

    /** Persists the choice and registers it, taking effect on the next call the SDK makes. */
    fun setSelection(context: Context, selection: ProviderSelection) {
        prefs(context).setSelection(selection)
        apply(context)
    }

    fun entries(context: Context): List<CustomOfflineEntry> =
        prefs(context).rows().mapNotNull { row ->
            val parts = row.split(FIELD_SEPARATOR)
            if (parts.size < 4) return@mapNotNull null
            CustomOfflineEntry(
                number = parts[0],
                name = parts[1],
                spamCategory = parts[2],
                spamLevel = OfflineNumberInfo.SpamLevel.entries
                    .firstOrNull { it.name == parts[3] }
                    ?: OfflineNumberInfo.SpamLevel.UNLIKELY,
            )
        }

    fun add(context: Context, entry: CustomOfflineEntry) {
        val kept = entries(context).filterNot { it.number == entry.number }
        write(context, kept + entry)
    }

    fun remove(context: Context, number: String) {
        write(context, entries(context).filterNot { it.number == number })
    }

    /** Registers the stored choice; called from `Application.onCreate` and on every toggle. */
    fun apply(context: Context) {
        TrustallOfflineDb.setProvider(
            if (selection(context).custom) DemoOfflineDbProvider(context) else null
        )
    }

    /** Version of the pretend database on disk, 0 until the first pretend download finishes. */
    internal fun version(context: Context): Int =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_VERSION, 0)

    internal fun bumpVersion(context: Context): Int {
        val next = version(context) + 1
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_VERSION, next)
            .apply()
        return next
    }

    private fun write(context: Context, entries: List<CustomOfflineEntry>) {
        prefs(context).setRows(
            entries.map { entry ->
                listOf(entry.number, entry.name, entry.spamCategory, entry.spamLevel.name)
                    .joinToString(FIELD_SEPARATOR)
            }
        )
    }

    private const val SEAM_KEY = "offlinedb"
    private const val PREFS_NAME = "trustall_demo_prefs"
    private const val KEY_VERSION = "offlinedb_fake_version"
    private const val FIELD_SEPARATOR = "\u0001"
}

/**
 * A pretend offline database: the rows entered on the screen, plus a download and a profile that
 * behave like the real thing without a byte leaving the device.
 *
 * [clear] is deliberately left on the interface's default body, so a release build of the demo
 * goes through `OfflineDbProvider$DefaultImpls` and exercises the SDK's keep rule for it. It is
 * also the honest answer for a database that ships inside the app: there is nothing to delete, so
 * clearing leaves the version and rows alone, unlike Gogolook's.
 */
internal class DemoOfflineDbProvider(context: Context) : OfflineDbProvider {

    private val appContext = context.applicationContext

    override suspend fun getNumberInfo(number: String): OfflineNumberInfo? =
        OfflineDbProviders.entries(appContext)
            .firstOrNull { it.number == number }
            ?.let { entry ->
                OfflineNumberInfo(
                    number = entry.number,
                    name = entry.name,
                    spamCategory = entry.spamCategory,
                    spamLevel = entry.spamLevel,
                )
            }

    /**
     * Counts from 0 to 100 over five seconds and finishes.
     *
     * Paced rather than instant so the screen's progress bar, and any integrator UI built the
     * same way, has something to render. No `flowOn` here on purpose: the interface says a
     * provider applies its own dispatcher, and pure delays belong on the caller's.
     */
    override fun downloadIfNeeded(): Flow<DownloadState> = flow {
        repeat(STEPS + 1) { step ->
            emit(DownloadState.Downloading(step * 100 / STEPS))
            delay(DURATION_MILLIS / STEPS)
        }
        OfflineDbProviders.bumpVersion(appContext)
        emit(DownloadState.Finished)
    }

    /**
     * Describes the rows currently entered, or null before the first pretend download.
     *
     * Null rather than an empty profile: the SDK's own provider reports nothing until a database
     * is actually on the device, and the screen reads null as "not downloaded yet".
     */
    override suspend fun getDbProfile(): OfflineDbProfile? {
        val version = OfflineDbProviders.version(appContext)
        if (version == 0) return null

        val entries = OfflineDbProviders.entries(appContext)
        return OfflineDbProfile(
            version = version,
            topNumSize = entries.count {
                it.spamLevel == OfflineNumberInfo.SpamLevel.UNLIKELY
            },
            toptopSpamSize = entries.count {
                it.spamLevel == OfflineNumberInfo.SpamLevel.CONFIRMED
            },
            spamNumSize = entries.count {
                it.spamLevel != OfflineNumberInfo.SpamLevel.UNLIKELY
            },
        )
    }

    private companion object {
        private const val STEPS = 20
        private const val DURATION_MILLIS = 5_000L
    }
}
