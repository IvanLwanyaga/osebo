package com.osebo.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.databinding.ItemSaleBinding
import com.osebo.ai.models.Sale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleAdapter(private val sales: List<Sale>) :
    RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    class SaleViewHolder(val binding: ItemSaleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val sale = sales[position]

        holder.binding.txtSaleAmount.text = "UGX %,.0f".format(sale.totalAmount)
        
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(sale.createdAt))
        holder.binding.txtSaleDate.text = "$dateStr • ${sale.paymentMethod}"
        
        // Product name is not in the Sale model summary, so we use a generic label
        holder.binding.txtSaleProduct.text = "Sale #${sale.id.takeLast(6)}"
    }

    override fun getItemCount() = sales.size
}