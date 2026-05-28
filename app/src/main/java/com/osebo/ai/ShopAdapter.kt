package com.osebo.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemShopBinding

class ShopAdapter(private val shops: List<Shop>) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(val binding: ItemShopBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shops[position]

        holder.binding.tvShopName.text = shop.name
        holder.binding.tvShopInfo.text = shop.info
        holder.binding.tvSales.text = shop.sales
        holder.binding.tvGrowth.text = shop.growth
        holder.binding.tvGrowth.setTextColor(
            if (shop.growth.contains("↑"))
                ContextCompat.getColor(holder.itemView.context, R.color.success)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.danger)
        )
    }

    override fun getItemCount() = shops.size
}