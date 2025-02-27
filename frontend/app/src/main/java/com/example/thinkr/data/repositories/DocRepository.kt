package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.Document
import java.io.InputStream

interface DocRepository {
    suspend fun getDocuments(
        userId: String,
        documentIds: List<String>?
    ): List<Document>

    suspend fun uploadDocument(
        document: InputStream,
        userId: String,
        documentName: String,
        documentContext: String
    )
}
