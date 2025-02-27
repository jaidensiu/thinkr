package com.example.thinkr.ui.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thinkr.data.repositories.AuthRepositoryImpl
import com.example.thinkr.data.repositories.UserRepositoryImpl
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LandingScreenViewModel(
    private val repository: AuthRepositoryImpl,
    private val userRepo: UserRepositoryImpl
) : ViewModel() {
    private val _state = MutableStateFlow(LandingScreenState())
    val state = _state.asStateFlow()

    fun onEditUsername(username: String) {
        _state.update { it.copy(username = username) }
    }

    fun onEditPassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onGoogleSignInResult(
        account: GoogleSignInAccount,
        onSignOut: () -> Unit
    ) {
        if (account.id == null) {
            _state.update {
                it.copy(
                    error = "Sign in failed for ${account.email} since Google UUID is ${account.id}"
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.login(account.id!!).fold(
                onSuccess = {
                    userRepo.setUser(it.data.user.copy())
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            isAuthenticated = true
                        )
                    }
                },
                onFailure = { exception ->
                    userRepo.delUser()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                    onSignOut()
                }
            )
        }
    }
}
