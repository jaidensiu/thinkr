package com.example.thinkr.ui.document_options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DocumentOptionsViewModel : ViewModel() {
    private val _state = MutableStateFlow(DocumentOptionsState())
    val state = _state.asStateFlow()

    init {
        // TODO: for demo purposes
        viewModelScope.launch {
            delay(5000)
            onReady()
        }
    }

    fun onReady() {
        //TODO: This is called when the study material is ready from the backend
        _state.update { it.copy(isReady = true) }
    }
}
