package com.gogolook.trustall.demo.feature.callerid

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CallerIdLogManager {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun addLog(message: String) {
        val timestamp = timeFormat.format(Date())
        val logEntry = "[$timestamp] $message"
        _logs.update { listOf(logEntry) + it }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
