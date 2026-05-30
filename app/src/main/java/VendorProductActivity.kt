package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Product

class VendorProductsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_products)

        findViewById<TextView>(R.id.topBarTitle).text = "My Products"

        container = findViewById(R.id.vendorProductsContainer)

        loadVendorProducts()
    }

    private fun loadVendorProducts() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseRefs.products.get().addOnSuccessListener { snapshot ->
            container.removeAllViews()

            var found = false

            for (child in snapshot.children) {
                val product = child.getValue(Product::class.java)

                if (product != null && product.vendorId == uid) {
                    found = true

                    val card = TextView(this).apply {
                        text = """
                            ${product.title}
                            ${product.category} > ${product.subCategory}
                            Rs ${product.price}
                            Stock: ${product.stock}
                            Active: ${product.active}
                        """.trimIndent()
                        textSize = 16f
                        setPadding(24, 24, 24, 24)
                        setBackgroundResource(R.drawable.bg_dashboard_card)
                    }

                    container.addView(card)
                }
            }

            if (!found) {
                container.addView(TextView(this).apply {
                    text = "You have not added any products yet."
                    textSize = 16f
                })
            }
        }
    }
}