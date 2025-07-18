package com.talal.techhub.models

data class User(
    var id: String? = null,
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val userType: String = "",
    val approved: Boolean = false
)
