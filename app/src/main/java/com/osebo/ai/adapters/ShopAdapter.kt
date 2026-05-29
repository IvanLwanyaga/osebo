package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.R
import com.osebo.ai.models.Shop

class ShopAdapter(
    private val shopList: List<Shop>
) : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {

    class ShopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtShopName: TextView = itemView.findViewById(R.id.txtShopName)
        val txtLocation: TextView = itemView.findViewById(R.id.txtLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop, parent, false)

        return ShopViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {

        val shop = shopList[position]

        holder.txtShopName.text = shop.shopName
        holder.txtLocation.text = shop.location
    }

    override fun getItemCount(): Int = shopList.size
}