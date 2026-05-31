package com.talal.techhub

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.talal.techhub.data.FirebaseRefs
import java.text.SimpleDateFormat
import java.util.*

class AdminPcBuildOrdersActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pc_build_orders)

        findViewById<TextView>(R.id.topBarTitle).text = "PC Build Orders"

        container = findViewById(R.id.ordersContainer)

        loadOrders()
    }

    private fun loadOrders() {
        FirebaseRefs.pcBuildOrders.get().addOnSuccessListener { snapshot ->
            container.removeAllViews()

            if (!snapshot.exists()) {
                addText("No custom PC build orders yet.")
                return@addOnSuccessListener
            }

            for (child in snapshot.children) {
                val order = child.value as? Map<*, *> ?: continue
                addOrderCard(child.key ?: "", order)
            }
        }
    }

    private fun addOrderCard(orderId: String, order: Map<*, *>) {
        val createdAt = order["createdAt"] as? Long ?: 0L
        val date = if (createdAt > 0) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(createdAt))
        } else {
            "Unknown"
        }

        val components = order["components"] as? List<*> ?: emptyList<Any>()

        val componentText = components.joinToString("\n") {
            val c = it as? Map<*, *>
            "- ${c?.get("title")} (${c?.get("subCategory")})"
        }

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(R.drawable.bg_dashboard_card)
        }

        val info = TextView(this).apply {
            text = """
                Order ID: $orderId
                Date: $date
                User ID: ${order["userId"]}
                Total: Rs ${order["total"]}
                Assembly Status: ${order["assemblyStatus"]}
                Payment: ${order["paymentMethod"]}
                
                Components:
                $componentText
            """.trimIndent()
            textSize = 15f
            setTextColor(getColor(R.color.primary_text))
        }

        val markReview = Button(this).apply {
            text = "Mark Under Assembly Review"
            setOnClickListener {
                FirebaseRefs.pcBuildOrders.child(orderId).child("assemblyStatus")
                    .setValue("under_assembly_review")
                    .addOnSuccessListener { loadOrders() }
            }
        }

        val markReady = Button(this).apply {
            text = "Mark Ready for Delivery"
            setOnClickListener {
                FirebaseRefs.pcBuildOrders.child(orderId).child("assemblyStatus")
                    .setValue("ready_for_delivery")
                    .addOnSuccessListener { loadOrders() }
            }
        }

        card.addView(info)
        card.addView(markReview)
        card.addView(markReady)

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