package com.talal.techhub.models

data class Product(
    var id: String = "",
    var vendorId: String = "",
    var title: String = "",
    var brand: String = "",
    var category: String = "",
    var subCategory: String = "",
    var price: Double = 0.0,
    var stock: Int = 0,
    var description: String = "",
    var imageUrl: String = "",
    var specs: Map<String, String> = emptyMap(),
    var active: Boolean = true,
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
)