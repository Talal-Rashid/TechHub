package com.talal.techhub.models

data class Review(
    var id: String = "",
    var productId: String = "",
    var userId: String = "",
    var orderId: String = "",
    var rating: Int = 0,
    var comment: String = "",
    var createdAt: Long = 0L
)