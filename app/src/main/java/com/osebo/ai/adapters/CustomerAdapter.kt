package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemCustomerRowBinding
import com.osebo.ai.models.Customer

class CustomerAdapter(
    private var customers: List<Customer>,
    private val onCustomerClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCustomerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.binding.tvName.text = customer.name
        holder.binding.tvEmailPhone.text = if (customer.email.isNotEmpty()) customer.email else customer.phone
        holder.binding.tvLastPurchase.text = customer.lastPurchase.ifEmpty { "No purchases" }
        holder.binding.tvTotalSpent.text = "UGX ${customer.totalSpent}"
        holder.binding.tvOrders.text = customer.orders.toString()
        holder.binding.tvTier.text = customer.tier

        holder.itemView.setOnClickListener { onCustomerClick(customer) }
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: List<Customer>) {
        customers = newList
        notifyDataSetChanged()
    }
}
