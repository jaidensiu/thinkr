package com.example.thinkr.ui.home

data class HomeScreenState(
    val items: List<Item> = emptyList(),
    val showDialog: Boolean = false
)

data class Item(
    val name: String,
    val logoUrl: String
)

sealed class HomeScreenAction {
    object LeftButtonClicked : HomeScreenAction()
    object RightButtonClicked : HomeScreenAction()
    object ItemClicked : HomeScreenAction()
    object AddButtonClicked : HomeScreenAction()
    object DismissDialog : HomeScreenAction()
    object OpenFilePicker : HomeScreenAction()
}
