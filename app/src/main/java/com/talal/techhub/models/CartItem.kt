package com.talal.techhub.models

data class CartItem(
    var productId: String = "",
    var title: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    var vendorId: String = ""
)