package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Order
import java.text.SimpleDateFormat
import java.util.*

class VendorOrdersActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_orders)

        findViewById<TextView>(R.id.topBarTitle).text = "Vendor Orders"

        container = findViewById(R.id.vendorOrdersContainer)

        loadVendorOrders()
    }

    private fun loadVendorOrders() {
        val vendorId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseRefs.orders.get()
            .addOnSuccessListener { snapshot ->
                container.removeAllViews()

                val orders = snapshot.children.mapNotNull {
                    it.getValue(Order::class.java)
                }.filter { order ->
                    order.items.any { it.vendorId == vendorId }
                }.sortedByDescending {
                    it.createdAt
                }

                if (orders.isEmpty()) {
                    addPlainText("No orders for your products yet.")
                    return@addOnSuccessListener
                }

                orders.forEach { order ->
                    addOrderCard(order, vendorId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load orders: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addOrderCard(order: Order, vendorId: String) {
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(order.createdAt))

        val vendorItems = order.items.filter { it.vendorId == vendorId }
        val vendorTotal = vendorItems.sumOf { it.price * it.quantity }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val info = TextView(this).apply {
            text = """
                Order ID: ${order.id}
                Date: $date
                
                Your Items:
                ${vendorItems.joinToString("\n") { "- ${it.title} x${it.quantity} = Rs ${it.price * it.quantity}" }}
                
                Vendor Total: Rs $vendorTotal
                Order Status: ${order.orderStatus}
                Payment: ${order.paymentMethod}
                Payment Status: ${order.paymentStatus}
                
                Customer Phone: ${order.phone}
                Delivery Address: ${order.address}
            """.trimIndent()
            textSize = 15f
            setTextColor(getColor(R.color.primary_text))
        }

        val markPacked = Button(this).apply {
            text = "Mark Packed"
            setOnClickListener {
                FirebaseRefs.orders.child(order.id).child("orderStatus")
                    .setValue("packed_by_vendor")
                    .addOnSuccessListener {
                        Toast.makeText(this@VendorOrdersActivity, "Order marked packed", Toast.LENGTH_SHORT).show()
                        loadVendorOrders()
                    }
            }
        }

        val markShipped = Button(this).apply {
            text = "Mark Shipped"
            setOnClickListener {
                FirebaseRefs.orders.child(order.id).child("orderStatus")
                    .setValue("shipped")
                    .addOnSuccessListener {
                        Toast.makeText(this@VendorOrdersActivity, "Order marked shipped", Toast.LENGTH_SHORT).show()
                        loadVendorOrders()
                    }
            }
        }

        card.addView(info)

        if (order.orderStatus != "delivered" && order.orderStatus != "cancelled") {
            card.addView(markPacked)
            card.addView(markShipped)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        container.addView(card, params)
    }

    private fun addPlainText(text: String) {
        container.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(getColor(R.color.primary_text))
        })
    }
}