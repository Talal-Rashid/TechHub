package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.CartItem

class CartActivity : AppCompatActivity() {

    private lateinit var cartContainer: LinearLayout
    private lateinit var txtTotal: TextView
    private lateinit var btnCheckout: Button

    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        findViewById<TextView>(R.id.topBarTitle).text = "Cart"

        cartContainer = findViewById(R.id.cartContainer)
        txtTotal = findViewById(R.id.txtTotal)
        btnCheckout = findViewById(R.id.btnCheckout)

        btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        loadCart()
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }

    private fun loadCart() {
        val userId = uid ?: return

        FirebaseRefs.carts.child(userId).get().addOnSuccessListener { snapshot ->
            cartContainer.removeAllViews()

            var total = 0.0
            var hasItems = false

            for (child in snapshot.children) {
                val item = child.getValue(CartItem::class.java)

                if (item != null) {
                    hasItems = true
                    total += item.price * item.quantity
                    addCartCard(item)
                }
            }

            txtTotal.text = "Total: Rs $total"
            btnCheckout.isEnabled = hasItems

            if (!hasItems) {
                cartContainer.addView(TextView(this).apply {
                    text = "Your cart is empty."
                    textSize = 16f
                    setTextColor(getColor(R.color.primary_text))
                })
            }
        }
    }

    private fun addCartCard(item: CartItem) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val info = TextView(this).apply {
            text = "${item.title}\nQty: ${item.quantity} | Rs ${item.price}\nSubtotal: Rs ${item.price * item.quantity}"
            textSize = 16f
            setTextColor(getColor(R.color.primary_text))
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }

        val minus = Button(this).apply {
            text = "-"
            setOnClickListener { decreaseQuantity(item) }
        }

        val plus = Button(this).apply {
            text = "+"
            setOnClickListener { increaseQuantity(item) }
        }

        val remove = Button(this).apply {
            text = "Remove"
            setOnClickListener { removeItem(item) }
        }

        controls.addView(minus, LinearLayout.LayoutParams(0, 48, 1f))
        controls.addView(plus, LinearLayout.LayoutParams(0, 48, 1f))
        controls.addView(remove, LinearLayout.LayoutParams(0, 48, 2f))

        card.addView(info)
        card.addView(controls)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        cartContainer.addView(card, params)
    }

    private fun increaseQuantity(item: CartItem) {
        val userId = uid ?: return

        FirebaseRefs.products.child(item.productId).child("stock").get()
            .addOnSuccessListener { stockSnapshot ->
                val stock = stockSnapshot.getValue(Int::class.java) ?: 0

                if (item.quantity >= stock) {
                    Toast.makeText(this, "Only $stock available in stock", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                FirebaseRefs.carts.child(userId).child(item.productId).child("quantity")
                    .setValue(item.quantity + 1)
                    .addOnSuccessListener { loadCart() }
            }
    }

    private fun decreaseQuantity(item: CartItem) {
        val userId = uid ?: return

        if (item.quantity <= 1) {
            removeItem(item)
            return
        }

        FirebaseRefs.carts.child(userId).child(item.productId).child("quantity")
            .setValue(item.quantity - 1)
            .addOnSuccessListener { loadCart() }
    }

    private fun removeItem(item: CartItem) {
        val userId = uid ?: return

        FirebaseRefs.carts.child(userId).child(item.productId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Removed from cart", Toast.LENGTH_SHORT).show()
                loadCart()
            }
    }
}