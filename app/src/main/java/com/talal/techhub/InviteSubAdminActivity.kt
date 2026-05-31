package com.talal.techhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs

class InviteSubAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_sub_admin)

        findViewById<TextView>(R.id.topBarTitle).text = "Invite Sub Admin"

        val emailField = findViewById<EditText>(R.id.emailEditText)

        findViewById<Button>(R.id.btnInvite).setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val inviteId = FirebaseRefs.adminInvites.push().key ?: return@setOnClickListener
            val code = "TECHHUB-${System.currentTimeMillis()}"

            val invite = mapOf(
                "id" to inviteId,
                "email" to email,
                "role" to "sub_admin",
                "code" to code,
                "used" to false,
                "invitedBy" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
                "createdAt" to System.currentTimeMillis()
            )

            FirebaseRefs.adminInvites.child(inviteId).setValue(invite)
                .addOnSuccessListener {
                    openEmailApp(email, code)
                }
        }
    }

    private fun openEmailApp(email: String, code: String) {
        val subject = "TechHub Sub Admin Invite"
        val body = """
            You have been invited as a TechHub Sub Admin.
            
            Invite Code:
            $code
            
            Register/login with this email and contact the main admin for activation.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        startActivity(Intent.createChooser(intent, "Send invite email"))
    }
}