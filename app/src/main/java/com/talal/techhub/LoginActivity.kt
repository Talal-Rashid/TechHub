package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase
        .getInstance("https://techhub-f054e-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailField = findViewById<EditText>(R.id.emailEditText)
        val passwordField = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signupButton = findViewById<Button>(R.id.signupButton)

        signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Trying login...", Toast.LENGTH_SHORT).show()

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid

                    Toast.makeText(this, "Auth success. UID: $uid", Toast.LENGTH_LONG).show()
                    Log.d("LOGIN_DEBUG", "Auth UID: $uid")

                    if (uid == null) {
                        Toast.makeText(this, "Login failed. UID missing.", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    database.child(uid).get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.exists()) {
                                Toast.makeText(this, "User DB record not found for UID: $uid", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            val role = snapshot.child("role").getValue(String::class.java)
                                ?.trim()
                                ?.lowercase()
                                ?: "user"

                            val approved = snapshot.child("approved").getValue(Boolean::class.java) ?: false

                            Toast.makeText(this, "Role: $role | Approved: $approved", Toast.LENGTH_LONG).show()
                            Log.d("LOGIN_DEBUG", "Role: $role, Approved: $approved")

                            if (role == "vendor" && !approved) {
                                Toast.makeText(this, "Vendor waiting for admin approval", Toast.LENGTH_LONG).show()
                                auth.signOut()
                                return@addOnSuccessListener
                            }

                            val destination = when (role) {
                                "admin" -> AdminPanelActivity::class.java
                                "vendor" -> VendorPanelActivity::class.java
                                else -> UserHomeActivity::class.java
                            }

                            startActivity(Intent(this, destination))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_LONG).show()
                            Log.e("LOGIN_DEBUG", "Database error", it)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Auth failed: ${it.message}", Toast.LENGTH_LONG).show()
                    Log.e("LOGIN_DEBUG", "Auth failed", it)
                }
        }
    }
}