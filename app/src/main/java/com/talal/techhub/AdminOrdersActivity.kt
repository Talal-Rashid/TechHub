package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Order
import java.text.SimpleDateFormat
import java.util.*

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_orders)

        findViewById<TextView>(R.id.topBarTitle).text = "All Orders"

        container = findViewById(R.id.ordersContainer)

        loadOrders()
    }

    private fun loadOrders() {
        FirebaseRefs.orders.get().addOnSuccessListener { snapshot ->
            container.removeAllViews()

            val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                .sortedByDescending { it.createdAt }

            if (orders.isEmpty()) {
                addText("No orders yet.")
                return@addOnSuccessListener
            }

            orders.forEach { addOrderCard(it) }
        }
    }

    private fun addOrderCard(order: Order) {
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(order.createdAt))

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val info = TextView(this).apply {
            text = """
                Order ID: ${order.id}
                Date: $date
                User ID: ${order.userId}
                
                Items:
                ${order.items.joinToString("\n") { "- ${it.title} x${it.quantity}" }}
                
                Total: Rs ${order.total}
                Payment: ${order.paymentMethod}
                Payment Status: ${order.paymentStatus}
                Order Status: ${order.orderStatus}
                
                Address: ${order.address}
                Phone: ${order.phone}
            """.trimIndent()
            textSize = 15f
            setTextColor(getColor(R.color.primary_text))
        }

        val markDelivered = Button(this).apply {
            text = "Mark Delivered"
            setOnClickListener {
                FirebaseRefs.orders.child(order.id).child("orderStatus")
                    .setValue("delivered")
                    .addOnSuccessListener { loadOrders() }
            }
        }

        val markCancelled = Button(this).apply {
            text = "Cancel Order"
            setOnClickListener {
                FirebaseRefs.orders.child(order.id).child("orderStatus")
                    .setValue("cancelled")
                    .addOnSuccessListener { loadOrders() }
            }
        }

        card.addView(info)
        card.addView(markDelivered)
        card.addView(markCancelled)

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