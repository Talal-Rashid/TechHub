package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs

class SettingsActivity : AppCompatActivity() {

    private lateinit var txtUserInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<TextView>(R.id.topBarTitle).text = "Settings"

        txtUserInfo = findViewById(R.id.txtUserInfo)

        loadUserInfo()

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            txtUserInfo.text = "No user logged in."
            return
        }

        FirebaseRefs.users.child(user.uid).get()
            .addOnSuccessListener { snapshot ->
                val name = snapshot.child("name").getValue(String::class.java) ?: "-"
                val email = snapshot.child("email").getValue(String::class.java) ?: user.email.orEmpty()
                val phone = snapshot.child("phone").getValue(String::class.java) ?: "-"
                val role = snapshot.child("role").getValue(String::class.java) ?: "-"

                txtUserInfo.text = """
                    Name: $name
                    Email: $email
                    Phone: $phone
                    Role: $role
                """.trimIndent()
            }
    }
}