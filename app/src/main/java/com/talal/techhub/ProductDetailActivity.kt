package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.CartItem
import com.talal.techhub.models.Product

class ProductDetailActivity : AppCompatActivity() {

    private var product: Product? = null

    private lateinit var txtProductName: TextView
    private lateinit var txtProductMeta: TextView
    private lateinit var txtProductPrice: TextView
    private lateinit var txtStockStatus: TextView
    private lateinit var txtRating: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtSpecs: TextView
    private lateinit var btnAddToCart: Button
    private lateinit var btnBuyNow: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        findViewById<TextView>(R.id.topBarTitle).text = "Product Detail"

        txtProductName = findViewById(R.id.txtProductName)
        txtProductMeta = findViewById(R.id.txtProductMeta)
        txtProductPrice = findViewById(R.id.txtProductPrice)
        txtStockStatus = findViewById(R.id.txtStockStatus)
        txtRating = findViewById(R.id.txtRating)
        txtDescription = findViewById(R.id.txtDescription)
        txtSpecs = findViewById(R.id.txtSpecs)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)

        btnAddToCart.setOnClickListener { addToCart(false) }
        btnBuyNow.setOnClickListener { addToCart(true) }

        val productId = intent.getStringExtra("productId") ?: return finish()

        FirebaseRefs.products.child(productId).get()
            .addOnSuccessListener {
                product = it.getValue(Product::class.java)
                renderProduct()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun renderProduct() {
        val p = product ?: return

        txtProductName.text = p.title
        txtProductMeta.text = "${p.brand} • ${p.category} > ${p.subCategory}"
        txtProductPrice.text = "Rs ${p.price}"
        txtStockStatus.text = if (p.stock > 0) "In Stock: ${p.stock} available" else "Out of Stock"
        txtRating.text = "Rating: Not rated yet"
        txtDescription.text = "Description\n\n${p.description.ifBlank { "No description provided." }}"

        txtSpecs.text = "Specifications\n\n" + if (p.specs.isEmpty()) {
            "No specs added."
        } else {
            p.specs.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        }

        val inStock = p.stock > 0
        btnAddToCart.isEnabled = inStock
        btnBuyNow.isEnabled = inStock
    }

    private fun addToCart(openCheckout: Boolean) {
        val p = product ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        if (p.stock <= 0) {
            Toast.makeText(this, "Product is out of stock", Toast.LENGTH_SHORT).show()
            return
        }

        val cartRef = FirebaseRefs.carts.child(uid).child(p.id)

        cartRef.get().addOnSuccessListener { snapshot ->
            val existing = snapshot.getValue(CartItem::class.java)

            val newQuantity = (existing?.quantity ?: 0) + 1

            val item = CartItem(
                productId = p.id,
                title = p.title,
                price = p.price,
                quantity = newQuantity,
                vendorId = p.vendorId
            )

            cartRef.setValue(item).addOnSuccessListener {
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
                if (openCheckout) {
                    startActivity(Intent(this, CheckoutActivity::class.java))
                }
            }
        }
    }
}