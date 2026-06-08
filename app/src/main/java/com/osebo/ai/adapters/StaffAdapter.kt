package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemStaffBinding
import com.osebo.ai.models.User

class StaffAdapter(private val staff: List<User>) :
    RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val user = staff[position]

        holder.binding.txtStaffName.text = user.displayName
        holder.binding.txtStaffRole.text = user.role
        holder.binding.txtStaffEmail.text = user.email
    }

    override fun getItemCount() = staff.size
}