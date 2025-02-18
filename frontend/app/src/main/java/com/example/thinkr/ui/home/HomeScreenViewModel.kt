package com.example.thinkr.ui.home

import androidx.lifecycle.ViewModel
import com.example.thinkr.domain.model.DocumentItem
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.navigation.NavController
import com.example.thinkr.app.Route
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {
    private fun getItems(): List<DocumentItem> {
        // TODO: In the future here will be an HTTP call to retrieve the items
        return listOf(
            DocumentItem("defaultUser", "Item1"),
            DocumentItem("defaultUser", "Item2"),
            DocumentItem("defaultUser", "Item3"),
        )
    }

    private val _state = MutableStateFlow(
        HomeScreenState(items = getItems())
    )

    var state: StateFlow<HomeScreenState> = _state.asStateFlow()

    fun onAction(action: HomeScreenAction, navController: NavController) {
        when (action) {
            HomeScreenAction.BackButtonClicked -> {
                // Route to Login page
            }
            HomeScreenAction.ProfileButtonClicked -> {
                // Route to Profile
            }
            is HomeScreenAction.DocumentItemClicked -> {
                navController.navigate(Route.DocumentOptions.createRoute(action.documentItem))
            }
            HomeScreenAction.AddButtonClicked -> {
                // Handle add button click action
                _state.value = _state.value.copy(showDialog = true)
            }
            HomeScreenAction.DismissDialog -> {
                // Handle dismiss dialog action
                _state.value = _state.value.copy(showDialog = false)
            }
            HomeScreenAction.OpenFilePicker -> {
                // Handle open file picker action

            }
        }
    }
}
