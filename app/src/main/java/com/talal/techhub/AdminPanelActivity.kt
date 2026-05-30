package com.talal.techhub

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.talal.techhub.adapters.AdminUserAdapter
import com.talal.techhub.data.FirebaseRefs
import com.talal.techhub.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.ImageView

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminUserAdapter
    private val pendingVendors = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        findViewById<TextView>(R.id.topBarTitle).text = "TechHub"
        findViewById<TextView>(R.id.topBarTitle).text = "Admin"
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        recyclerView = findViewById(R.id.recyclerViewPendingUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdminUserAdapter(
            pendingVendors,
            onAcceptClick = { approveVendor(it) },
            onRejectClick = { rejectVendor(it) }
        )

        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadPendingVendors()
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

                    if (pendingVendors.isEmpty()) {
                        Toast.makeText(
                            this@AdminPanelActivity,
                            "No pending vendors",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
            .addOnFailureListener {
                Toast.makeText(this, "Approval failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun rejectVendor(user: User) {
        val uid = user.id ?: return

        FirebaseRefs.users.child(uid).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "${user.name} rejected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Reject failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}