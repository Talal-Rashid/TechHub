package com.talal.techhub.models

data class User(
    var id: String? = null,
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var role: String = "",
    var approved: Boolean = false
)