package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val spinner = findViewById<Spinner>(R.id.spinnerRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val roles = arrayOf("User", "Vendor", "Admin")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val role = spinner.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hardcoded master admin check
            val masterAdminEmail = "admin@techhub.com"
            val masterAdminPassword = "admin123"

            if (role == "Admin" && (email != masterAdminEmail || password != masterAdminPassword)) {
                Toast.makeText(this, "Only master admin can register here", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = auth.currentUser?.uid ?: return@addOnSuccessListener
                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "role" to role,
                        "approved" to if (role == "User" || role == "Admin") true else false
                    )

                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()

                            when {
                                role == "Admin" -> startActivity(Intent(this, AdminPanelActivity::class.java))
                                role == "Vendor" -> {
                                    Toast.makeText(this, "Waiting for Admin Approval", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                }
                                else -> startActivity(Intent(this, UserHomeActivity::class.java))
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
