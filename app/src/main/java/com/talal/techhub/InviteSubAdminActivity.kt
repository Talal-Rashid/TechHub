package com.talal.techhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.utils.PermissionUtils

class InviteSubAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_sub_admin)

        findViewById<TextView>(R.id.topBarTitle).text = "Invite Sub Admin"

        val emailField = findViewById<EditText>(R.id.emailEditText)

        findViewById<Button>(R.id.btnInvite).setOnClickListener {
            val email = emailField.text.toString().trim().lowercase()

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createInvite(email)
        }
    }

    private fun createInvite(email: String) {
        val inviteId = FirebaseRefs.adminInvites.push().key ?: return
        val code = "TECHHUB-${(100000..999999).random()}"

        val permissions = mapOf(
            PermissionUtils.MANAGE_VENDORS to findViewById<CheckBox>(R.id.checkManageVendors).isChecked,
            PermissionUtils.MANAGE_PRODUCTS to findViewById<CheckBox>(R.id.checkManageProducts).isChecked,
            PermissionUtils.MANAGE_ORDERS to findViewById<CheckBox>(R.id.checkManageOrders).isChecked,
            PermissionUtils.MANAGE_PC_BUILDS to findViewById<CheckBox>(R.id.checkManagePcBuilds).isChecked,
            PermissionUtils.MANAGE_USERS to findViewById<CheckBox>(R.id.checkManageUsers).isChecked,
            PermissionUtils.HIDE_PRODUCTS to findViewById<CheckBox>(R.id.checkHideProducts).isChecked
        )

        val invite = mapOf(
            "id" to inviteId,
            "email" to email,
            "role" to "sub_admin",
            "code" to code,
            "used" to false,
            "invitedBy" to (FirebaseAuth.getInstance().currentUser?.uid ?: ""),
            "createdAt" to System.currentTimeMillis(),
            "permissions" to permissions
        )

        FirebaseRefs.adminInvites.child(inviteId).setValue(invite)
            .addOnSuccessListener {
                Toast.makeText(this, "Invite created", Toast.LENGTH_SHORT).show()
                openEmailApp(email, code)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Invite failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun openEmailApp(email: String, code: String) {
        val subject = "TechHub Sub Admin Invite"
        val body = """
            You have been invited as a TechHub Sub Admin.
            
            Invite Code:
            $code
            
            Register in the TechHub app using this same email and invite code.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        startActivity(Intent.createChooser(intent, "Send invite email"))
    }
}