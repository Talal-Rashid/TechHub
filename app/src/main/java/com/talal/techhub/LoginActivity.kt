package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val btnGotoRegister = findViewById<Button>(R.id.btnGotoRegister)
            btnGotoRegister.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: return@addOnSuccessListener

                    dbRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                Toast.makeText(this@LoginActivity, "User data not found", Toast.LENGTH_SHORT).show()
                                return
                            }

                            val role = snapshot.child("role").getValue(String::class.java)
                            val approved = snapshot.child("approved").getValue(Boolean::class.java) ?: false

                            when (role) {
                                "Admin" -> {
                                    startActivity(Intent(this@LoginActivity, AdminPanelActivity::class.java))
                                    finish()
                                }
                                "Vendor" -> {
                                    if (approved) {
                                        startActivity(Intent(this@LoginActivity, VendorPanelActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this@LoginActivity, "Vendor not approved yet", Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                    }
                                }
                                "User" -> {
                                    startActivity(Intent(this@LoginActivity, UserHomeActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(this@LoginActivity, "Unknown role", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@LoginActivity, "DB error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        val btnGotoRegister = findViewById<Button>(R.id.btnGotoRegister)
        btnGotoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}
