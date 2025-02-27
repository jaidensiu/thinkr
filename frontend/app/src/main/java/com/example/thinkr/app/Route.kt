package com.example.thinkr.app

import android.net.Uri
import com.example.thinkr.data.models.DocumentItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface Route {
    @Serializable
    data object RouteGraph : Route

    @Serializable
    data object Landing : Route

    @Serializable
    data object Home : Route

    @Serializable
    data class DocumentOptions(val documentItem: DocumentItem) : Route {
        companion object {
            const val ROUTE = "documentOptions/{documentJson}"
            const val ARGUMENT = "documentJson"
            fun createRoute(document: DocumentItem): String {
                val json = Json.encodeToString(document)
                return ROUTE.replace("{documentJson}", Uri.encode(json))
            }
        }
    }

    @Serializable
    data class DocumentDetails(val selectedUri: String) : Route {
        companion object {
            const val ROUTE = "documentDetails/{selectedUri}"
            const val ARGUMENT = "selectedUri"
            fun createRoute(selectedUri: Uri): String {
                return ROUTE.replace("{selectedUri}", Uri.encode(selectedUri.toString()))
            }
        }
    }

    @Serializable
    data object Profile : Route

    @Serializable
    data object Payment : Route

    @Serializable
    data class Flashcards(val documentItem: DocumentItem) : Route {
        companion object {
            const val ROUTE = "flashcards/{documentJson}"
            const val ARGUMENT = "documentJson"
            fun createRoute(document: DocumentItem): String {
                val json = Json.encodeToString(document)
                return ROUTE.replace("{documentJson}", Uri.encode(json))
            }
        }
    }
}
