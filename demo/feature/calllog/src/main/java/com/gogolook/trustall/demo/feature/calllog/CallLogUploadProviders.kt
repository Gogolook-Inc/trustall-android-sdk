package com.gogolook.trustall.demo.feature.calllog

import android.content.Context
import com.gogolook.trustall.calllog.CallLogUploadProvider
import com.gogolook.trustall.calllog.TrustallCallLog
import com.gogolook.trustall.calllog.model.CallLog
import com.gogolook.trustall.calllog.model.UploadResult
import com.gogolook.trustall.demo.core.util.ProviderPrefs
import com.gogolook.trustall.demo.core.util.ProviderSelection

/** One record the custom provider received instead of sending it to Gogolook. */
data class CapturedUpload(val number: String, val date: Long)

/**
 * The custom call log destination and what it has received.
 *
 * This seam is the one that hands data over rather than answering a question, so there is nothing
 * to pre-populate: the rows here are what the provider captured, not what it serves. It takes a
 * single provider on purpose, because a list would mean the same call records going to several
 * destinations at once, which is the opposite of what replacing this is usually for.
 */
object CallLogUploadProviders {

    private fun prefs(context: Context) = ProviderPrefs(context, SEAM_KEY)

    fun selection(context: Context): ProviderSelection = prefs(context).selection()

    /** Persists the choice and registers it, taking effect on the next call the SDK makes. */
    fun setSelection(context: Context, selection: ProviderSelection) {
        prefs(context).setSelection(selection)
        apply(context)
    }

    fun captured(context: Context): List<CapturedUpload> =
        prefs(context).rows().mapNotNull { row ->
            val parts = row.split(FIELD_SEPARATOR)
            val date = parts.getOrNull(1)?.toLongOrNull()
            if (parts.size == 2 && date != null) CapturedUpload(parts[0], date) else null
        }

    fun clearCaptured(context: Context) {
        prefs(context).setRows(emptyList())
    }

    internal fun capture(context: Context, callLogs: List<CallLog>) {
        val rows = (captured(context) + callLogs.map { CapturedUpload(it.number, it.date) })
            .takeLast(MAX_CAPTURED)
            .map { it.number + FIELD_SEPARATOR + it.date }
        prefs(context).setRows(rows)
    }

    /** Registers the stored choice; called from `Application.onCreate` and on every toggle. */
    fun apply(context: Context) {
        TrustallCallLog.setProvider(
            if (selection(context).custom) DemoCallLogUploadProvider(context) else null
        )
    }

    private const val SEAM_KEY = "calllog"
    private const val FIELD_SEPARATOR = "\u0001"
    private const val MAX_CAPTURED = 50
}

/** Keeps the records on the device instead of sending them anywhere. */
internal class DemoCallLogUploadProvider(context: Context) : CallLogUploadProvider {

    private val appContext = context.applicationContext

    override suspend fun uploadCallLogs(callLogs: List<CallLog>): UploadResult {
        CallLogUploadProviders.capture(appContext, callLogs)
        return UploadResult.Success
    }
}
