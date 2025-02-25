package com.example.thinkr.ui.home

import android.net.Uri
import com.example.thinkr.domain.DocumentManager
import com.example.thinkr.domain.model.DocumentItem

data class HomeScreenState(
    val documentManager: DocumentManager,
    val showDialog: Boolean = false,
    val clickedDocumentItem: DocumentItem? = null
)

sealed class HomeScreenAction {
    object BackButtonClicked : HomeScreenAction()
    object ProfileButtonClicked : HomeScreenAction()
    data class DocumentItemClicked(val documentItem: DocumentItem) : HomeScreenAction()
    object AddButtonClicked : HomeScreenAction()
    object DismissDialog : HomeScreenAction()
    data class FileSelected(val selectedUri: Uri) : HomeScreenAction()
}
