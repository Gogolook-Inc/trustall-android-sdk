package com.gogolook.trustall.demo.core.ui

import com.gogolook.trustall.core.urlscan.model.UrlScanResult
import com.gogolook.trustall.msgfilter.model.FilterType
import com.gogolook.trustall.smsflow.model.IncomingSms

/**
 * An incoming SMS together with the URL scan progress/results and message filter
 * classification of its body. Shared between the SMS Flow feature screen and the
 * system alert overlay.
 */
data class ScannedSms(
    val id: String,
    val sms: IncomingSms,
    val isScanning: Boolean = false,
    val urlResults: List<UrlScanResult> = emptyList(),
    val filterType: FilterType? = null,
)
