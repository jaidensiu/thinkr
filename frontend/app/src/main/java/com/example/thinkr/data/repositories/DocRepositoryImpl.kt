package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.Document
import com.example.thinkr.data.remote.RemoteApiImpl
import java.io.InputStream

class DocRepositoryImpl(private val remoteApi: RemoteApiImpl): DocRepository {
    override suspend fun getDocuments(
        userId: String,
        documentIds: List<String>?
    ): List<Document> {
        return remoteApi.getDocuments(userId, documentIds)
    }

    override suspend fun uploadDocument(
        document: InputStream,
        userId: String,
        documentName: String,
        documentContext: String
    ) {
        remoteApi.uploadDocument(
            document = document,
            userId = userId,
            documentName = documentName,
            documentContext = documentContext
        )
    }
}