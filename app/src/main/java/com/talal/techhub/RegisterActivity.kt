package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val nameField = findViewById<EditText>(R.id.nameEditText)
        val emailField = findViewById<EditText>(R.id.emailEditText)
        val phoneField = findViewById<EditText>(R.id.phoneEditText)
        val passwordField = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordField = findViewById<EditText>(R.id.confirmPasswordEditText)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val inviteCodeField = findViewById<EditText>(R.id.inviteCodeEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        val roles = arrayOf("user", "vendor", "sub_admin")

        roleSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        roleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = roleSpinner.selectedItem.toString()
                inviteCodeField.visibility =
                    if (selectedRole == "sub_admin") View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        loginButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim().lowercase()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()
            val inviteCode = inviteCodeField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role == "sub_admin") {
                if (inviteCode.isEmpty()) {
                    Toast.makeText(this, "Enter sub-admin invite code", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                validateSubAdminInviteAndRegister(name, email, phone, password, inviteCode)
            } else {
                registerNormalUser(name, email, phone, password, role)
            }
        }
    }

    private fun registerNormalUser(name: String, email: String, phone: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userRecord = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to role,
                    "approved" to (role == "user"),
                    "permissions" to emptyMap<String, Boolean>()
                )

                FirebaseRefs.users.child(uid).setValue(userRecord)
                    .addOnSuccessListener {
                        if (role == "vendor") {
                            Toast.makeText(this, "Vendor registered. Waiting for admin approval.", Toast.LENGTH_LONG).show()
                            auth.signOut()
                            startActivity(Intent(this, LoginActivity::class.java))
                        } else {
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, UserHomeActivity::class.java))
                        }
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateSubAdminInviteAndRegister(
        name: String,
        email: String,
        phone: String,
        password: String,
        inviteCode: String
    ) {
        FirebaseRefs.adminInvites.get()
            .addOnSuccessListener { snapshot ->
                var matchedInviteId: String? = null
                var permissions: Map<String, Boolean> = emptyMap()

                for (child in snapshot.children) {
                    val inviteEmail = child.child("email").getValue(String::class.java)?.lowercase()
                    val code = child.child("code").getValue(String::class.java)
                    val used = child.child("used").getValue(Boolean::class.java) ?: false

                    if (inviteEmail == email && code == inviteCode && !used) {
                        matchedInviteId = child.key
                        permissions = child.child("permissions").children.associate {
                            it.key.orEmpty() to (it.getValue(Boolean::class.java) ?: false)
                        }
                        break
                    }
                }

                if (matchedInviteId == null) {
                    Toast.makeText(this, "Invalid invite email or code", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                createSubAdminAccount(name, email, phone, password, matchedInviteId!!, permissions)
            }
    }

    private fun createSubAdminAccount(
        name: String,
        email: String,
        phone: String,
        password: String,
        inviteId: String,
        permissions: Map<String, Boolean>
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userRecord = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to "sub_admin",
                    "approved" to true,
                    "permissions" to permissions
                )

                FirebaseRefs.users.child(uid).setValue(userRecord)
                    .addOnSuccessListener {
                        FirebaseRefs.adminInvites.child(inviteId).child("used").setValue(true)
                        Toast.makeText(this, "Sub-admin account created", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, AdminPanelActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}