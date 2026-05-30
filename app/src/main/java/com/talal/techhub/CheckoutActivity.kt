package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.CartItem

class CheckoutActivity : AppCompatActivity() {

    private lateinit var txtOrderSummary: TextView
    private var cartItems = listOf<CartItem>()
    private var total = 0.0

    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        findViewById<TextView>(R.id.topBarTitle).text = "Checkout"

        txtOrderSummary = findViewById(R.id.txtOrderSummary)

        val address = findViewById<EditText>(R.id.addressEditText)
        val phone = findViewById<EditText>(R.id.phoneEditText)

        loadCartSummary()

        findViewById<Button>(R.id.btnPlaceOrder).setOnClickListener {
            if (address.text.toString().trim().isEmpty() || phone.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter address and phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            placeCodOrder(address.text.toString().trim(), phone.text.toString().trim())
        }
    }

    private fun loadCartSummary() {
        val userId = uid ?: return

        FirebaseRefs.carts.child(userId).get().addOnSuccessListener { snapshot ->
            cartItems = snapshot.children.mapNotNull { it.getValue(CartItem::class.java) }
            total = cartItems.sumOf { it.price * it.quantity }

            txtOrderSummary.text = if (cartItems.isEmpty()) {
                "Cart is empty."
            } else {
                "Order Summary\n\n" +
                        cartItems.joinToString("\n") {
                            "${it.title} x${it.quantity} = Rs ${it.price * it.quantity}"
                        } +
                        "\n\nTotal: Rs $total\nPayment: Cash on Delivery"
            }
        }
    }

    private fun placeCodOrder(address: String, phone: String) {
        val userId = uid ?: return

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        validateStockBeforeOrder(
            onValid = {
                createOrder(userId, address, phone)
            },
            onInvalid = { message ->
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                loadCartSummary()
            }
        )
    }

    private fun validateStockBeforeOrder(onValid: () -> Unit, onInvalid: (String) -> Unit) {
        var checked = 0

        for (item in cartItems) {
            FirebaseRefs.products.child(item.productId).child("stock").get()
                .addOnSuccessListener { stockSnapshot ->
                    checked++

                    val stock = stockSnapshot.getValue(Int::class.java) ?: 0

                    if (item.quantity > stock) {
                        onInvalid("${item.title} has only $stock left in stock")
                        return@addOnSuccessListener
                    }

                    if (checked == cartItems.size) {
                        onValid()
                    }
                }
                .addOnFailureListener {
                    onInvalid("Failed to check stock")
                }
        }
    }

    private fun createOrder(userId: String, address: String, phone: String) {
        val orderId = FirebaseRefs.orders.push().key ?: return

        val order = mapOf(
            "id" to orderId,
            "userId" to userId,
            "items" to cartItems,
            "total" to total,
            "address" to address,
            "phone" to phone,
            "paymentMethod" to "cash_on_delivery",
            "paymentStatus" to "cod_pending",
            "orderStatus" to "placed",
            "createdAt" to System.currentTimeMillis()
        )

        FirebaseRefs.orders.child(orderId).setValue(order)
            .addOnSuccessListener {
                deductStock()
                FirebaseRefs.carts.child(userId).removeValue()
                Toast.makeText(this, "COD order placed", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deductStock() {
        for (item in cartItems) {
            val stockRef = FirebaseRefs.products.child(item.productId).child("stock")

            stockRef.get().addOnSuccessListener { snapshot ->
                val currentStock = snapshot.getValue(Int::class.java) ?: 0
                val newStock = (currentStock - item.quantity).coerceAtLeast(0)
                stockRef.setValue(newStock)
            }
        }
    }
}