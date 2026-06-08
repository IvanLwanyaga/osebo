package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemInventoryRowBinding
import com.osebo.ai.models.Product

class InventoryAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInventoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvName.text = product.name
        holder.binding.tvCategory.text = product.category
        holder.binding.tvStock.text = product.quantity.toString()
        holder.binding.tvPrice.text = "UGX ${String.format("%,.0f", product.sellingPrice)}"

        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount() = products.size

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }
}
