@file:OptIn(InternalAPI::class)

package com.example.thinkr.data.remote

import com.example.thinkr.data.models.AuthResponse
import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.UploadResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.InputStream

class RemoteApiImpl(private val client: HttpClient) : RemoteApi {
    override suspend fun postAuthBearerToken(token: String): AuthResponse {
        val response = client.post(urlString =  BASE_URL + AUTH + LOGIN) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val responseBody = response.bodyAsText()
        return Json.decodeFromString(responseBody)
    }

    override suspend fun getDocuments(userId: String, documentIds: List<String>?): List<Document> {
        val response = client.get(urlString = BASE_URL + DOCUMENT + RETRIEVE) {
            header(HttpHeaders.ContentType, "application/json")
            parameter("userId", userId)
            documentIds?.let {
                parameter("documentIds", it.joinToString(","))
            }
        }
        val responseBody = response.bodyAsText()
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
        val docs = jsonResponse["data"]?.jsonObject?.get("docs")?.jsonArray
        return docs?.map { Json.decodeFromJsonElement(Document.serializer(), it) } ?: emptyList()
    }

    override suspend fun uploadDocument(
        document: InputStream,
        userId: String,
        documentName: String,
        documentContext: String
    ): UploadResponse {
        val response = client.post(urlString = BASE_URL + DOCUMENT + UPLOAD) {
            body = MultiPartFormDataContent(
                formData {
                    append("document", document, Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                        append(HttpHeaders.ContentDisposition, "filename=\"$documentName\"")
                    })
                    append("userId", userId)
                    append("documentName", documentName)
                    append("context", documentContext)
                }
            )
        }
        val responseBody = response.bodyAsText()
        return Json.decodeFromString(responseBody)
    }

    private companion object {
        private const val BASE_URL = "https://ourapi" // TODO: Change this
        private const val AUTH = "/auth"
        private const val LOGIN = "/login"
        private const val DOCUMENT = "/document"
        private const val UPLOAD = "/upload"
        private const val RETRIEVE = "/retrieve"
    }
}
