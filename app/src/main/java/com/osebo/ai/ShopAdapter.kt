package com.osebo.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemShopBinding

class ShopAdapter(private val shops: List<Shop>) :
    RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(val binding: ItemShopBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shops[position]

        holder.binding.txtShopName.text = shop.name
        holder.binding.txtLocation.text = shop.location
    }

    override fun getItemCount() = shops.size
}