package com.example.thinkr.ui.home

import android.net.Uri
import com.example.thinkr.data.models.Document

data class HomeScreenState(
    val showDialog: Boolean = false,
    val clickedDocumentItem: Document? = null
)

sealed class HomeScreenAction {
    object BackButtonClicked : HomeScreenAction()
    object ProfileButtonClicked : HomeScreenAction()
    data class DocumentItemClicked(val documentItem: Document) : HomeScreenAction()
    object AddButtonClicked : HomeScreenAction()
    object DismissDialog : HomeScreenAction()
    data class FileSelected(val selectedUri: Uri) : HomeScreenAction()
}
