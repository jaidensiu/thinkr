package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.AuthResponse

interface AuthRepository {
    suspend fun login(token: String): Result<AuthResponse>
}
