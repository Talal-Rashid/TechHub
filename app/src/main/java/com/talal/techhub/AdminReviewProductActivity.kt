package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Product
import java.text.SimpleDateFormat
import java.util.*

class AdminReviewProductsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_review_products)

        findViewById<TextView>(R.id.topBarTitle).text = "Review Products"

        container = findViewById(R.id.productsContainer)

        loadProducts()
    }

    private fun loadProducts() {
        FirebaseRefs.products.get().addOnSuccessListener { snapshot ->
            container.removeAllViews()

            val products = snapshot.children.mapNotNull { it.getValue(Product::class.java) }
                .sortedByDescending { it.updatedAt }

            if (products.isEmpty()) {
                addText("No products found.")
                return@addOnSuccessListener
            }

            products.forEach { addProductCard(it) }
        }
    }

    private fun addProductCard(product: Product) {
        val date = if (product.updatedAt > 0) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(product.updatedAt))
        } else {
            "Unknown"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val info = TextView(this).apply {
            text = """
                ${product.title}
                ${product.brand}
                ${product.category} > ${product.subCategory}
                Price: Rs ${product.price}
                Stock: ${product.stock}
                Active: ${product.active}
                Updated: $date
                Vendor ID: ${product.vendorId}
            """.trimIndent()
            textSize = 15f
            setTextColor(getColor(R.color.primary_text))
        }

        val toggle = Button(this).apply {
            text = if (product.active) "Deactivate Product" else "Reactivate Product"
            setOnClickListener {
                FirebaseRefs.products.child(product.id).child("active").setValue(!product.active)
                    .addOnSuccessListener { loadProducts() }
            }
        }

        card.addView(info)
        card.addView(toggle)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        container.addView(card, params)
    }

    private fun addText(text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(getColor(R.color.primary_text))
        })
    }
}