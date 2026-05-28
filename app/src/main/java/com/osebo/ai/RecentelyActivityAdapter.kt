package com.osebo.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemRecentActivityBinding

class RecentActivityAdapter(private val activities: List<RecentActivity>) :
    RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder>() {

    class ActivityViewHolder(val binding: ItemRecentActivityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemRecentActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        holder.binding.tvTitle.text = activity.title
        holder.binding.tvTime.text = activity.time
        holder.binding.tvAmount.text = activity.amount

        val color = if (activity.isPositive) R.color.success else R.color.danger
        holder.binding.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.context, color))

        // Set icon (you can improve with real drawables)
        holder.binding.ivIcon.text = if (activity.isPositive) "🛍️" else "📤"
    }

    override fun getItemCount() = activities.size
}