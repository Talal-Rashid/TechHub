package com.talal.techhub.models

data class AdminInvite(
    var id: String = "",
    var email: String = "",
    var code: String = "",
    var role: String = "sub_admin",
    var used: Boolean = false,
    var invitedBy: String = "",
    var createdAt: Long = 0L,
    var permissions: Map<String, Boolean> = emptyMap()
)