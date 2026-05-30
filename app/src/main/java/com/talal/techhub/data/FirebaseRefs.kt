package com.talal.techhub.data

import com.google.firebase.database.FirebaseDatabase

object FirebaseRefs {
    private const val DB_URL =
        "https://techhub-f054e-default-rtdb.asia-southeast1.firebasedatabase.app/"

    val db = FirebaseDatabase.getInstance(DB_URL)

    val users = db.getReference("users")
    val products = db.getReference("products")
    val carts = db.getReference("carts")
    val orders = db.getReference("orders")
}