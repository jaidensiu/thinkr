package com.example.thinkr.app

import android.net.Uri
import com.example.thinkr.domain.model.DocumentItem
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
            fun createRoute(document: DocumentItem): String {
                val json = Json.encodeToString(document)
                return ROUTE.replace("{documentJson}", Uri.encode(json))
            }
        }
    }
}
