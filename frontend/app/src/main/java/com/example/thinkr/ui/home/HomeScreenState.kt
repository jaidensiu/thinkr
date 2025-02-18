package com.example.thinkr.ui.home

import com.example.thinkr.domain.model.DocumentItem

data class HomeScreenState(
    val items: List<DocumentItem> = emptyList(),
    val showDialog: Boolean = false,
    val clickedDocumentItem: DocumentItem? = null
)

sealed class HomeScreenAction {
    object BackButtonClicked : HomeScreenAction()
    object ProfileButtonClicked : HomeScreenAction()
    data class DocumentItemClicked(val documentItem: DocumentItem) : HomeScreenAction()
    object AddButtonClicked : HomeScreenAction()
    object DismissDialog : HomeScreenAction()
    object OpenFilePicker : HomeScreenAction()
}
