package com.osebo.ai.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemInventoryBinding
import com.osebo.ai.models.Product

class ProductAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.binding.tvItemName.text = product.name
        holder.binding.tvStock.text = "Stock: ${product.quantity}"
        holder.binding.tvPrice.text = "UGX %,.0f".format(product.unitPrice)
        
        if (product.quantity <= product.minStock) {
            holder.binding.tvStock.setTextColor(Color.RED)
        } else {
            holder.binding.tvStock.setTextColor(Color.parseColor("#6B7280"))
        }
    }

    override fun getItemCount() = products.size
}
