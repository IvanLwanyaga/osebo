package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemTopSellingBinding

class TopItemAdapter(private val items: List<Pair<String, String>>) :
    RecyclerView.Adapter<TopItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopSellingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopSellingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, value) = items[position]
        holder.binding.txtItemName.text = name
        holder.binding.txtValue.text = value
    }

    override fun getItemCount() = items.size
}