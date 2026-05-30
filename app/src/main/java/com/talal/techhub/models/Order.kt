package com.talal.techhub.models

data class Order(
    var id: String = "",
    var userId: String = "",
    var items: List<CartItem> = emptyList(),
    var total: Double = 0.0,
    var address: String = "",
    var phone: String = "",
    var paymentMethod: String = "cash_on_delivery",
    var paymentStatus: String = "cod_pending",
    var orderStatus: String = "placed",
    var createdAt: Long = 0L
)