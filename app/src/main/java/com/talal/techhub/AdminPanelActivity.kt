package com.talal.techhub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.talal.techhub.adapters.AdminUserAdapter
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.User
import com.talal.techhub.utils.PermissionUtils

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminUserAdapter
    private val pendingVendors = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        findViewById<TextView>(R.id.topBarTitle).text = "Admin"

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardInviteSubAdmin).setOnClickListener {
            startActivity(Intent(this, InviteSubAdminActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardAdminAddProduct).setOnClickListener {
            startActivity(Intent(this, VendorAddProductActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardReviewProducts).setOnClickListener {
            startActivity(Intent(this, AdminReviewProductsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardPcBuildOrders).setOnClickListener {
            startActivity(Intent(this, AdminPcBuildOrdersActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.cardAdminOrders).setOnClickListener {
            startActivity(Intent(this, AdminOrdersActivity::class.java))
        }

        recyclerView = findViewById(R.id.recyclerViewPendingUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminUserAdapter(
            pendingVendors,
            onAcceptClick = { approveVendor(it) },
            onRejectClick = { rejectVendor(it) }
        )

        recyclerView.adapter = adapter

        applyAdminPermissions()
        loadPendingVendors()
    }

    private fun applyAdminPermissions() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseRefs.users.child(uid).get()
            .addOnSuccessListener { snapshot ->
                val currentUser = snapshot.getValue(User::class.java) ?: return@addOnSuccessListener

                val isMaster = currentUser.role == "master_admin" || currentUser.role == "admin"

                if (isMaster) return@addOnSuccessListener

                findViewById<LinearLayout>(R.id.cardInviteSubAdmin).visibility = View.GONE

                findViewById<LinearLayout>(R.id.cardVendorApprovals).visibility =
                    if (PermissionUtils.hasPermission(currentUser, PermissionUtils.MANAGE_VENDORS)) View.VISIBLE else View.GONE

                findViewById<LinearLayout>(R.id.cardAdminProducts).visibility =
                    if (PermissionUtils.hasPermission(currentUser, PermissionUtils.MANAGE_PRODUCTS)) View.VISIBLE else View.GONE

                findViewById<LinearLayout>(R.id.cardReviewProducts).visibility =
                    if (PermissionUtils.hasPermission(currentUser, PermissionUtils.HIDE_PRODUCTS)) View.VISIBLE else View.GONE

                findViewById<LinearLayout>(R.id.cardAdminOrders).visibility =
                    if (PermissionUtils.hasPermission(currentUser, PermissionUtils.MANAGE_ORDERS)) View.VISIBLE else View.GONE

                findViewById<LinearLayout>(R.id.cardPcBuildOrders).visibility =
                    if (PermissionUtils.hasPermission(currentUser, PermissionUtils.MANAGE_PC_BUILDS)) View.VISIBLE else View.GONE
            }
    }

    private fun loadPendingVendors() {
        FirebaseRefs.users
            .orderByChild("approved")
            .equalTo(false)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pendingVendors.clear()

                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)

                        if (user != null && user.role == "vendor") {
                            user.id = child.key
                            pendingVendors.add(user)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AdminPanelActivity,
                        "Failed to load vendors: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun approveVendor(user: User) {
        val uid = user.id ?: return

        FirebaseRefs.users.child(uid).child("approved").setValue(true)
            .addOnSuccessListener {
                Toast.makeText(this, "${user.name} approved", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectVendor(user: User) {
        val uid = user.id ?: return

        FirebaseRefs.users.child(uid).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "${user.name} rejected", Toast.LENGTH_SHORT).show()
            }
    }
}