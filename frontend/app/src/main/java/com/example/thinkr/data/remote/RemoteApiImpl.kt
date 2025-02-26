package com.example.thinkr.data.remote

import com.example.thinkr.data.models.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

class RemoteApiImpl(private val client: HttpClient) : RemoteApi {
    override suspend fun postAuthBearerToken(token: String): AuthResponse {
        val response = client.post(urlString =  BASE_URL + AUTH + LOGIN) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val responseBody = response.bodyAsText()
        return Json.decodeFromString(responseBody)
    }

    private companion object {
        private const val BASE_URL = "https://ourapi"
        private const val AUTH = "/auth"
        private const val LOGIN = "/login"
    }
}
