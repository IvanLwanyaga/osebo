package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemCartBinding
import com.osebo.ai.models.CartItem

class CartAdapter(
    private var items: List<CartItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvQty.text = "Qty: ${item.quantity}"
        holder.binding.tvPrice.text = "UGX %,.0f".format(item.price * item.quantity)
    }

    override fun getItemCount() = items.size
}
