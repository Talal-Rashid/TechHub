package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var ordersContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        findViewById<TextView>(R.id.topBarTitle).text = "My Orders"

        ordersContainer = findViewById(R.id.ordersContainer)

        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseRefs.orders.orderByChild("userId").equalTo(uid).get()
            .addOnSuccessListener { snapshot ->
                ordersContainer.removeAllViews()

                if (!snapshot.exists()) {
                    addPlainText("No orders placed yet.")
                    return@addOnSuccessListener
                }

                val orders = snapshot.children.mapNotNull {
                    it.getValue(Order::class.java)
                }.sortedByDescending {
                    it.createdAt
                }

                for (order in orders) {
                    addOrderCard(order)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load orders: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun addOrderCard(order: Order) {
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(order.createdAt))

        val text = """
            Order ID: ${order.id}
            Date: $date
            
            Items:
            ${order.items.joinToString("\n") { "- ${it.title} x${it.quantity}" }}
            
            Total: Rs ${order.total}
            Payment: ${order.paymentMethod}
            Payment Status: ${order.paymentStatus}
            Order Status: ${order.orderStatus}
            
            Address: ${order.address}
            Phone: ${order.phone}
        """.trimIndent()

        val card = TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(getColor(R.color.primary_text))
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 14)

        ordersContainer.addView(card, params)

        if (order.orderStatus == "delivered") {
            addReviewButtons(order)
        }
    }

    private fun addReviewButtons(order: Order) {
        order.items.forEach { item ->
            val reviewBtn = Button(this).apply {
                text = "Review ${item.title}"
                setOnClickListener {
                    startActivity(
                        Intent(this@OrderHistoryActivity, AddReviewActivity::class.java)
                            .putExtra("productId", item.productId)
                            .putExtra("productTitle", item.title)
                            .putExtra("orderId", order.id)
                    )
                }
            }

            ordersContainer.addView(reviewBtn)
        }
    }

    private fun addPlainText(text: String) {
        ordersContainer.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(getColor(R.color.primary_text))
        })
    }
}