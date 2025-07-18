package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class UserHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        setupLogoutButton()
    }
    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }}
