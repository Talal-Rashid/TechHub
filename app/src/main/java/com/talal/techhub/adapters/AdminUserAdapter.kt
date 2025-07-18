package com.talal.techhub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.talal.techhub.R
import com.talal.techhub.models.User

class AdminUserAdapter(
    private val userList: List<User>,
    private val onAcceptClick: (User) -> Unit,
    private val onRejectClick: (User) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.txtName)
        val emailText: TextView = itemView.findViewById(R.id.txtEmail)
        val acceptButton: Button = itemView.findViewById(R.id.btnAccept)
        val rejectButton: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = "Name: ${user.name}"
        holder.emailText.text = "Email: ${user.email}"
        holder.acceptButton.setOnClickListener { onAcceptClick(user) }
        holder.rejectButton.setOnClickListener { onRejectClick(user) }
    }

    override fun getItemCount(): Int = userList.size
}
