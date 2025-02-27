package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.User

interface UserRepository {
    fun setUser(user: User)
    fun getUser(): User?
    fun delUser()
}