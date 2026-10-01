package com.gogolook.trustall.demo.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process

/**
 * Restarts the app for a clean start.
 *
 * A second `Trustall.initialize()` adopts a new device ID in place, but it rebuilds nothing that
 * was already built: providers, Omnidroid's managers and the network stacks behind them keep
 * running. Restarting is how the demo shows the difference.
 *
 * This runs in its own process, declared in the manifest, which is the point: a process cannot
 * outlive killing itself. It kills the main process by the pid it was handed, launches
 * [MainActivity] fresh, and finishes.
 */
class RestartActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mainPid = intent.getIntExtra(EXTRA_MAIN_PID, -1)
        if (mainPid > 0 && mainPid != Process.myPid()) {
            Process.killProcess(mainPid)
        }

        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    companion object {
        private const val EXTRA_MAIN_PID = "main_pid"

        /** Call from the main process; it does not return, the process is gone moments later. */
        fun restart(context: Context) {
            context.startActivity(
                Intent(context, RestartActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EXTRA_MAIN_PID, Process.myPid())
            )
        }
    }
}
