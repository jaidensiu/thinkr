package com.example.thinkr.ui.chat


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        // TODO: Remove this and replace it with the messages received from the backend
        _state.update { currentState ->
            currentState.copy(
                messages = listOf(
                    Message(
                        id = "1",
                        content = mutableStateOf("Hello there!"),
                        timestamp = System.currentTimeMillis() - 3600000,
                        isSender = false
                    ),
                    Message(
                        id = "2",
                        content = mutableStateOf("Hi! How are you?"),
                        timestamp = System.currentTimeMillis() - 3500000,
                        isSender = true
                    ),
                    Message(
                        id = "3",
                        content = mutableStateOf("I'm doing great, thanks for asking. How about you?"),
                        timestamp = System.currentTimeMillis() - 3400000,
                        isSender = false
                    )
                )
            )
        }
    }

    fun onSendMessage(content: String) {
        if (content.isBlank()) return

        val newMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = mutableStateOf(content),
            timestamp = System.currentTimeMillis(),
            isSender = true
        )

        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + newMessage
            )
        }


        // TODO: Remove this, just for demonstration purposes
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            onReceiveMessage("I received your message: \"$content\"")
        }
    }

    fun onReceiveMessage(content: String) {
        val contentStream = mutableStateOf("")
        if (content.isBlank()) return

        val newMessage = Message(
            id = System.currentTimeMillis().toString(),
            content = contentStream,
            timestamp = System.currentTimeMillis(),
            isSender = false
        )

        _state.update { currentState ->
            currentState.copy(
                messages = currentState.messages + newMessage
            )
        }

        // Gives a "streaming" effect to the message
        viewModelScope.launch {
            content.forEach { char ->
                contentStream.value += char
                kotlinx.coroutines.delay(MESSAGE_STREAM_DELAY)
            }
        }
    }

    companion object {
        private const val MESSAGE_STREAM_DELAY = 50L
    }
}
