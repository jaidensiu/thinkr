package com.example.thinkr.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinkr.data.repositories.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LandingScreenViewModel(private val repository: AuthRepositoryImpl) : ViewModel() {
    private val _state = MutableStateFlow(LandingScreenState())
    val state = _state.asStateFlow()

    fun onEditUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onEditPassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onGoogleSignInResult(token: String?) {
        if (token == null) {
            _state.update { it.copy(error = "Sign in failed") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.login(token).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                },
                onFailure = { exception ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
            )
        }
    }
}
