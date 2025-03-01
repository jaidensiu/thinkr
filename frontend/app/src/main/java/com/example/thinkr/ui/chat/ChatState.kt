package com.example.thinkr.ui.chat

import androidx.compose.runtime.State
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class Message(
    val id: String,
    val content: State<String>,
    val timestamp: Long,
    val isSender: Boolean
)

val Message.formattedTime: String
    get() {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
