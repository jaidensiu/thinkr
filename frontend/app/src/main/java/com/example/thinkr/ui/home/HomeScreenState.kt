package com.example.thinkr.ui.home

import android.net.Uri
import com.example.thinkr.data.repositories.DocRepositoryImpl
import com.example.thinkr.data.models.DocumentItem

data class HomeScreenState(
    val docRepositoryImpl: DocRepositoryImpl,
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
