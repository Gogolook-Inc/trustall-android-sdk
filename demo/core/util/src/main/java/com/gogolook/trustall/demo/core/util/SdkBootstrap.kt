package com.gogolook.trustall.demo.core.util

import android.app.Application
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.model.SdkConfig
import java.util.UUID

/**
 * The one place the demo calls `Trustall.initialize()`.
 *
 * It exists because the Auth screen initializes too, not just `DemoApplication`: changing the
 * device identifier re-runs initialization in place rather than waiting for a restart. Both paths
 * have to pass the same licence and debug flag or the comparison between them means nothing, so
 * the process-wide half of the config is remembered here once and only the identifier varies.
 */
object SdkBootstrap {

    /** Exactly 32 lowercase hex characters, the only shape `SdkConfig.deviceId` accepts. */
    val DEVICE_ID_FORMAT = Regex("[0-9a-f]{32}")

    private lateinit var licenseId: String
    private var isDebug: Boolean = true

    /** Call from `Application.onCreate` before the first [initialize]. */
    fun remember(licenseId: String, isDebug: Boolean) {
        this.licenseId = licenseId
        this.isDebug = isDebug
    }

    /**
     * Initializes the SDK, optionally handing it [deviceId].
     *
     * Safe to call again on a running SDK. `Trustall.initialize()` persists whatever it is given,
     * so a later call that passes null reads back the identifier a previous one supplied — the
     * SDK, not the demo, is what remembers it.
     *
     * @param deviceId 32 lowercase hex characters to adopt, or null to keep whatever is stored
     * @throws IllegalArgumentException if [deviceId] is not in that shape
     */
    suspend fun initialize(app: Application, deviceId: String? = null) {
        Trustall.initialize(
            app,
            SdkConfig(
                licenseId = licenseId,
                isDebug = isDebug,
                deviceId = deviceId?.let { id -> suspend { id } },
            ),
        )
    }

    /** A UUID with the dashes removed, which is the shape the SDK itself generates. */
    fun randomDeviceId(): String = UUID.randomUUID().toString().replace("-", "")
}
