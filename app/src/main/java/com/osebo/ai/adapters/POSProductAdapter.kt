package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemProductPosBinding
import com.osebo.ai.models.Product

class POSProductAdapter(
    private val products: List<Product>,
    private val onProductSelected: (Product) -> Unit
) : RecyclerView.Adapter<POSProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductPosBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductPosBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.binding.tvName.text = product.name
        holder.binding.tvPrice.text = "UGX %,.0f".format(product.sellingPrice)
        holder.binding.tvStock.text = "In Stock: ${product.quantity}"
        
        holder.binding.root.setOnClickListener { onProductSelected(product) }
    }

    override fun getItemCount() = products.size
}
