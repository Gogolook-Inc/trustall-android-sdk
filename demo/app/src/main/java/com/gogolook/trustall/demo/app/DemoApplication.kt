package com.gogolook.trustall.demo.app

import android.app.Application
import android.provider.Settings
import android.util.Log
import com.gogolook.trustall.callerid.callerId
import com.gogolook.trustall.callerid.callflow.CallsCallback
import com.gogolook.trustall.callerid.callflow.model.CallEvent
import com.gogolook.trustall.callerid.callflow.model.CallResponse
import com.gogolook.trustall.callerid.callflow.model.PlaceCallResponse
import com.gogolook.trustall.callerid.model.NumberInfo
import com.gogolook.trustall.callerid.model.NumberInfoState
import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.auth.auth
import com.gogolook.trustall.core.model.SdkConfig
import com.gogolook.trustall.demo.core.ui.CallerIdOverlay
import com.gogolook.trustall.demo.core.ui.SmsAlertOverlay
import com.gogolook.trustall.demo.feature.smsflow.SmsFlowManager
import com.gogolook.trustall.core.auth.model.AuthResult
import com.gogolook.trustall.numberblock.numberBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.UUID

class DemoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var overlay: CallerIdOverlay? = null
    private var smsAlertOverlay: SmsAlertOverlay? = null
    private var smsAlertDismissJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            val config = SdkConfig(licenseId = BuildConfig.LICENSE_ID, isDebug = true)
            Trustall.initialize(this@DemoApplication, config)
            if (Trustall.auth.getUserId().isEmpty()) {
                when (val result = Trustall.auth.register(memberId = UUID.randomUUID().toString())) {
                    is AuthResult.Success -> {
                        Log.d("DemoApplication", "Registered successfully")
                    }
                    is AuthResult.Error -> {
                        Log.e("DemoApplication", "Failed to register: $result")
                    }
                }
            }
            
            overlay = CallerIdOverlay(this@DemoApplication)
            setupCallerIdCallback()

            smsAlertOverlay = SmsAlertOverlay(this@DemoApplication)
            setupSmsFlowAlert()
        }
    }

    private fun setupSmsFlowAlert() {
        SmsFlowManager.start(applicationScope)
        applicationScope.launch {
            var lastShownId: String? = null
            SmsFlowManager.latest.collect { latest ->
                // Scan-result updates of the message already on screen reach the overlay
                // through the flow it collects; only a new message (re)opens the popup.
                if (latest == null || latest.id == lastShownId) return@collect
                lastShownId = latest.id

                if (!Settings.canDrawOverlays(this@DemoApplication)) {
                    Log.d("DemoApplication", "Overlay permission missing, skip SMS alert.")
                    return@collect
                }

                smsAlertOverlay?.show(SmsFlowManager.latest)
                smsAlertDismissJob?.cancel()
                smsAlertDismissJob = applicationScope.launch {
                    delay(10_000)
                    smsAlertOverlay?.dismiss()
                }
            }
        }
    }

    private fun setupCallerIdCallback() {
        Trustall.callerId.setCallsCallback(object : CallsCallback {
            override fun onScreenCall(screenCallEvent: CallEvent.ScreenCall) {
                val isBlocked = runBlocking { Trustall.numberBlock.isBlocked(screenCallEvent.number) }
                addCallerIdLog("Screen: ${screenCallEvent.number}, isBlocked: $isBlocked")
                screenCallEvent.updateCall(
                    CallResponse(
                        disallowCall = isBlocked,
                        rejectCall = isBlocked,
                        skipNotification = isBlocked,
                    )
                )
            }

            override fun onIncomingCall(incomingCallEvent: CallEvent.IncomingCall) {
                addCallerIdLog("Incoming: ${incomingCallEvent.number}")
                handleCallOverlay(incomingCallEvent.number)
            }

            override fun onPlaceCall(placeCallEvent: CallEvent.PlaceCall) {
                val isBlocked = runBlocking { Trustall.numberBlock.isBlocked(placeCallEvent.number) }
                addCallerIdLog("Place: ${placeCallEvent.number}, isBlocked: $isBlocked")
                placeCallEvent.updateCall(
                    if (isBlocked) {
                        PlaceCallResponse.CancelCall
                    } else {
                        PlaceCallResponse.PlaceCallUnmodified
                    }
                )
            }

            override fun onOutgoingCall(outgoingCallEvent: CallEvent.OutgoingCall) {
                addCallerIdLog("Outgoing: ${outgoingCallEvent.number}")
                handleCallOverlay(outgoingCallEvent.number)
            }

            override fun onOffhookCall(offhookCallEvent: CallEvent.OffhookCall) {
                addCallerIdLog("Offhook: ${offhookCallEvent.number}")
            }

            override fun onIdleCall(idleCallEvent: CallEvent.IdleCall) {
                addCallerIdLog("Idle: ${idleCallEvent.number}")
                applicationScope.launch(Dispatchers.Main) {
                    overlay?.dismiss()
                }
            }
        })
    }

    private fun handleCallOverlay(number: String) {
        if (!Trustall.callerId.canDrawOverlays()) {
            addCallerIdLog("Overlay permission missing, skip overlay.")
            return
        }

        applicationScope.launch {
            val infoFlow = MutableStateFlow<NumberInfo?>(null)
            withContext(Dispatchers.Main) { overlay?.show(infoFlow, number) }
            Trustall.callerId.getNumberInfo(number).collect { state ->
                val info = when (state) {
                    is NumberInfoState.Partial -> state.numberInfo
                    is NumberInfoState.Finish -> state.numberInfo
                    is NumberInfoState.Loading -> null
                }
                addCallerIdLog("NumberInfo: $info")
                infoFlow.value = info
            }
        }
    }

    private fun addCallerIdLog(message: String) {
        Log.d("DemoApplication", message)
        com.gogolook.trustall.demo.feature.callerid.CallerIdLogManager.addLog(message)
    }


}
