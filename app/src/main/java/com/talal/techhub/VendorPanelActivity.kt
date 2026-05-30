package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VendorPanelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor)

        findViewById<TextView>(R.id.topBarTitle).text = "Vendor"

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardAddProduct).setOnClickListener {
            startActivity(Intent(this, VendorAddProductActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardManageProducts).setOnClickListener {
            startActivity(Intent(this, VendorProductsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardVendorOrders).setOnClickListener {
            startActivity(Intent(this, VendorOrdersActivity::class.java))
        }
    }
}