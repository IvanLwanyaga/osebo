package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemRecentActivityBinding
import com.osebo.ai.models.RecentActivity

class RecentActivityAdapter(private val list: List<RecentActivity>) :
    RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecentActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvTime.text = item.time
        holder.binding.tvAmount.text = item.amount
        
        val color = if (item.isPositive) android.graphics.Color.parseColor("#10B981") 
                    else android.graphics.Color.parseColor("#EF4444")
        holder.binding.tvAmount.setTextColor(color)
    }

    override fun getItemCount() = list.size
}
