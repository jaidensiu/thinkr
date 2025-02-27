package com.example.thinkr.data.repositories

import com.example.thinkr.data.models.User

class UserRepositoryImpl: UserRepository {
    private var _signedInUser: User? = null
    override fun setUser(user: User) {
        _signedInUser = user
    }

    override fun getUser(): User? {
        if (_signedInUser == null) {
            return null
        }
        return User(_signedInUser!!.email, _signedInUser!!.name, _signedInUser!!.googleId, _signedInUser!!.userId, _signedInUser!!.subscribed)
    }

    override fun delUser() {
        _signedInUser = null
    }
}