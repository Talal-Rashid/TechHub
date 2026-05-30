package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UserHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)

        findViewById<TextView>(R.id.topBarTitle).text = "TechHub"

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardBrowseProducts).setOnClickListener {
            startActivity(Intent(this, BrowseProductsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardCheckout).setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardOrders).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardPhoneFinder).setOnClickListener {
            startActivity(Intent(this, PhoneFinderActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardPcBuilder).setOnClickListener {
            startActivity(Intent(this, PcBuilderActivity::class.java))
        }
    }
}