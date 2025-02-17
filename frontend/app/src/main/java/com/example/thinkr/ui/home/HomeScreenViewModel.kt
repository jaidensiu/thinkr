package com.example.thinkr.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel : ViewModel() {
    private fun getItems(): List<Item> {
        // TODO: In the future here will be an HTTP call to retrieve the items
        return listOf(
            Item("Item 1", "https://via.placeholder.com/40"),
            Item("Item 2", "https://via.placeholder.com/40"),
            Item("Item 3", "https://via.placeholder.com/40")
        )
    }

    private val _state = MutableStateFlow(
        HomeScreenState(items = getItems())
    )

    var state: StateFlow<HomeScreenState> = _state.asStateFlow()


    fun onAction(action: HomeScreenAction) {
        when (action) {
            HomeScreenAction.LeftButtonClicked -> {
                // Handle left button action
            }
            HomeScreenAction.RightButtonClicked -> {
                // Handle right button action
            }
            HomeScreenAction.ItemClicked -> {
                // Handle item click action
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
