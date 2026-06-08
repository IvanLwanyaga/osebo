package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemShopPerformanceBinding
import com.osebo.ai.models.ShopPerformance

class ShopPerformanceAdapter(private val list: List<ShopPerformance>) :
    RecyclerView.Adapter<ShopPerformanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemShopPerformanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.txtShopName.text = item.shopName
        holder.binding.txtShopSales.text = "UGX %,.0f".format(item.sales)
        holder.binding.txtShopExpenses.text = "UGX %,.0f".format(item.expenses)
    }

    override fun getItemCount() = list.size
}
