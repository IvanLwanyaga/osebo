package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemTransactionBinding
import com.osebo.ai.models.Sale
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Sale>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sale = transactions[position]
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        
        holder.binding.time.text = sdf.format(Date(sale.createdAt))
        holder.binding.title.text = "SALE #${sale.id.takeLast(6)}"
        holder.binding.amount.text = "UGX ${String.format("%,.0f", sale.totalAmount)}"
    }

    override fun getItemCount() = transactions.size
}