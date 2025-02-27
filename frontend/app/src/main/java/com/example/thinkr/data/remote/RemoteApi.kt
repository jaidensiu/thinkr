package com.example.thinkr.data.remote

import com.example.thinkr.data.models.AuthResponse
import com.example.thinkr.data.models.Document
import com.example.thinkr.data.models.UploadResponse
import java.io.InputStream

interface RemoteApi {
    suspend fun postAuthBearerToken(token: String): AuthResponse
    suspend fun getDocuments(userId: String, documentIds: List<String>?): List<Document>
    suspend fun uploadDocument(
        document: InputStream,
        userId: String,
        documentName: String,
        documentContext: String
    ): UploadResponse
}
