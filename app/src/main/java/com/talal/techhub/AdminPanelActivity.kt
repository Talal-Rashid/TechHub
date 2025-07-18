package com.talal.techhub

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.talal.techhub.adapters.AdminUserAdapter
import com.talal.techhub.models.User

class AdminPanelActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var userList: MutableList<User>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminUserAdapter
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        logoutBtn = findViewById(R.id.btnLogout)
        logoutBtn.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewPendingUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = mutableListOf()
        adapter = AdminUserAdapter(userList, ::approveUser, ::rejectUser)
        recyclerView.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().getReference("users")
        loadPendingUsers()
    }

    private fun loadPendingUsers() {
        databaseRef.orderByChild("approved").equalTo(false)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)
                        user?.let {
                            it.id = child.key  // set ID manually
                            userList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminPanelActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun approveUser(user: User) {
        user.id?.let {
            databaseRef.child(it).child("approved").setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(this, "${user.name} approved", Toast.LENGTH_SHORT).show()
                    userList.remove(user)
                    adapter.notifyDataSetChanged()
                }
        }
    }

    private fun rejectUser(user: User) {
        user.id?.let {
            databaseRef.child(it).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "${user.name} rejected", Toast.LENGTH_SHORT).show()
                    userList.remove(user)
                    adapter.notifyDataSetChanged()
                }
        }
    }
}
