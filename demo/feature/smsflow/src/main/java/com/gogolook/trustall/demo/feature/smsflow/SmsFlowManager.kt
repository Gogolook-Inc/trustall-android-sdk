package com.gogolook.trustall.demo.feature.smsflow

import com.gogolook.trustall.core.Trustall
import com.gogolook.trustall.core.urlscan.urlScan
import com.gogolook.trustall.demo.core.ui.ScannedSms
import com.gogolook.trustall.msgfilter.messageFilter
import com.gogolook.trustall.msgfilter.model.FilterResult
import com.gogolook.trustall.msgfilter.model.Message
import com.gogolook.trustall.smsflow.smsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for incoming SMS and their URL scan / message filter results,
 * shared by the SMS Flow screen (message list) and the app-level alert overlay. Started
 * once from DemoApplication so messages are captured and analyzed even when no screen is
 * open — including when an SMS wakes a killed process (the SDK flow replays it).
 */
object SmsFlowManager {

    private val _messages = MutableStateFlow<List<ScannedSms>>(emptyList())
    val messages: StateFlow<List<ScannedSms>> = _messages.asStateFlow()

    /** The most recently received message; drives the alert overlay. */
    private val _latest = MutableStateFlow<ScannedSms?>(null)
    val latest: StateFlow<ScannedSms?> = _latest.asStateFlow()

    private var started = false

    fun start(scope: CoroutineScope) {
        if (started) return
        started = true
        scope.launch {
            Trustall.smsFlow.incomingSms.collect { sms ->
                val id = "${sms.sender}_${sms.timestampMillis}"
                if (_messages.value.any { it.id == id }) return@collect

                val entry = ScannedSms(id = id, sms = sms, isScanning = true)
                _messages.update { listOf(entry) + it }
                _latest.value = entry

                scope.launch {
                    val results = try {
                        Trustall.urlScan.scanText(sms.body)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    updateEntry(id) { it.copy(isScanning = false, urlResults = results) }
                }

                scope.launch {
                    val type = try {
                        when (val result = Trustall.messageFilter.filter(Message(key = id, text = sms.body))) {
                            is FilterResult.Success -> result.results[id]
                            is FilterResult.Failure -> null
                        }
                    } catch (e: Exception) {
                        null
                    }
                    if (type != null) {
                        updateEntry(id) { it.copy(filterType = type) }
                    }
                }
            }
        }
    }

    /**
     * Transforms the entry in place so the concurrent URL scan and filter updates
     * cannot overwrite each other's fields. Only refreshes the overlay content if it
     * is still showing this message.
     */
    private fun updateEntry(id: String, transform: (ScannedSms) -> ScannedSms) {
        _messages.update { list -> list.map { if (it.id == id) transform(it) else it } }
        _latest.update { if (it?.id == id) transform(it) else it }
    }
}
