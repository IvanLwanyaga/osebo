package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.models.CartItem
import com.osebo.ai.databinding.ItemCartBinding

class SaleItemAdapter(
    private val items: MutableList<CartItem>,
    private val onUpdate: () -> Unit
) : RecyclerView.Adapter<SaleItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvName.text = item.name
        holder.binding.tvQty.text = "${item.name} × ${item.quantity}"
        holder.binding.tvPrice.text = String.format("%,.0f", item.price * item.quantity)

        holder.itemView.setOnClickListener {
            // Option to increase quantity or show detail
            item.quantity++
            notifyItemChanged(position)
            onUpdate()
        }

        holder.itemView.setOnLongClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
            onUpdate()
            true
        }
    }

    override fun getItemCount() = items.size
}
