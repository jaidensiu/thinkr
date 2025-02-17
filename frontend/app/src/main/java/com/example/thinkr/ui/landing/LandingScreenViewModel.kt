package com.example.thinkr.ui.landing

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LandingScreenViewModel : ViewModel() {
    private val _state = MutableStateFlow(LandingScreenState())
    val state = _state.asStateFlow()

    fun onEditUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onEditPassword(password: String) {
        _state.update { it.copy(password = password) }
    }
}
