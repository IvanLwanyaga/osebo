package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.R
import com.osebo.ai.models.Product

class InventoryAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_row, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class InventoryViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tvName)
        private val productCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val productStock: TextView = itemView.findViewById(R.id.tvStock)
        private val productPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(product: Product) {
            productName.text = product.name
            productCategory.text = product.category
            productStock.text = product.stock.toString()

            // Color coding based on stock levels
            val context = itemView.context
            when {
                product.stock <= 0 -> {
                    productStock.setTextColor(context.getColor(R.color.danger))
                }
                product.stock <= product.lowStockAlert -> {
                    productStock.setTextColor(context.getColor(R.color.warning))
                }
                else -> {
                    productStock.setTextColor(context.getColor(R.color.success))
                }
            }

            productPrice.text = "UGX ${String.format("%,.0f", product.price)}"
        }
    }
}