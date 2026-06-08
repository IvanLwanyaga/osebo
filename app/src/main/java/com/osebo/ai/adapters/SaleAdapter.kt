package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.osebo.ai.R
import com.osebo.ai.databinding.ItemSaleHistoryBinding
import com.osebo.ai.models.Sale
import java.text.SimpleDateFormat
import java.util.*

class SaleAdapter(
    private var sales: List<Sale>,
    private val onItemClick: (Sale) -> Unit
) : RecyclerView.Adapter<SaleAdapter.SaleViewHolder>() {

    inner class SaleViewHolder(private val binding: ItemSaleHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sale: Sale) {
            binding.apply {
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val dateString = dateFormat.format(Date(sale.createdAt))

                tvSaleId.text = sale.id

                val customerIcon = when (sale.customerType) {
                    "VIP" -> "⭐ "
                    "Wholesale" -> "📦 wholesale "
                    "Registered" -> "👤 "
                    else -> "🚶 "
                }
                tvCustomerName.text = "$customerIcon${sale.customerName}"

                tvDate.text = dateString
                tvTotalAmount.text = "UGX ${String.format("%,.0f", sale.totalAmount)}"
                tvPaymentMethod.text = sale.paymentMethod

                if (sale.customerPhone.isNotEmpty()) {
                    tvCustomerPhone.visibility = View.VISIBLE
                    tvCustomerPhone.text = sale.customerPhone
                } else {
                    tvCustomerPhone.visibility = View.GONE
                }

                if (sale.discountAmount > 0) {
                    tvDiscount.visibility = View.VISIBLE
                    tvDiscount.text = "Discount: UGX ${String.format("%,.0f", sale.discountAmount)}"
                } else {
                    tvDiscount.visibility = View.GONE
                }

                when (sale.status.lowercase()) {
                    "completed" -> {
                        tvStatus.text = "Completed"
                        tvStatus.setChipBackgroundColorResource(R.color.success)
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"))
                    }
                    "pending" -> {
                        tvStatus.text = "Pending"
                        tvStatus.setChipBackgroundColorResource(R.color.warning)
                        tvStatus.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
                    }
                    else -> {
                        tvStatus.text = sale.status
                        tvStatus.setChipBackgroundColorResource(R.color.border)
                    }
                }

                tvItemCount.text = "${sale.items.size} item(s)"
                root.setOnClickListener { onItemClick(sale) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSaleHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(sales[position])
    }

    override fun getItemCount() = sales.size

    fun updateSales(newSales: List<Sale>) {
        sales = newSales
        notifyDataSetChanged()
    }
}
