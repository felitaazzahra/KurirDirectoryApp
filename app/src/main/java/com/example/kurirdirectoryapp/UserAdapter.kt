package com.example.kurirdirectoryapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var listUser: List<UserResponse> = emptyList()
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.textViewName)
        val tvCompany: TextView = itemView.findViewById(R.id.textViewCompany)
        val tvEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        val tvPhone: TextView = itemView.findViewById(R.id.textViewPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = listUser[position]
        holder.tvName.text = user.name
        holder.tvCompany.text = "Perusahaan: ${user.company.companyName}"
        holder.tvEmail.text = "Email: ${user.email}"
        holder.tvPhone.text = "Telepon: ${user.phone}"
    }

    override fun getItemCount(): Int = listUser.size

    fun perbaruiDaftar(listBaru: List<UserResponse>) {
        this.listUser = listBaru
        notifyDataSetChanged()
    }
}
