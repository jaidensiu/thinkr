package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.AuthResponse
import com.example.thinkr.data.remote.RemoteApiImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(private val remoteApi: RemoteApiImpl) : AuthRepository {
    override suspend fun login(token: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(remoteApi.postAuthBearerToken(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
