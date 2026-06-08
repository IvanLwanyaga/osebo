package com.osebo.ai.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.databinding.ItemBillingShopBinding
import com.osebo.ai.models.Shop
import java.text.SimpleDateFormat
import java.util.*

class BillingAdapter(
    private val shops: List<Shop>,
    private val onRenew: (Shop) -> Unit
) : RecyclerView.Adapter<BillingAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(val binding: ItemBillingShopBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBillingShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shops[position]
        holder.binding.txtShopName.text = shop.name

        // Billing status is currently stored in User doc for simplicity in this MVP
        // In a complex app, each shop would have its own subscription field
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val status = doc.getString("subscriptionStatus") ?: "NOT ACTIVATED"
                holder.binding.txtStatus.text = status
                
                if (status == "ACTIVATED") {
                    holder.binding.layoutDetails.visibility = View.VISIBLE
                    holder.binding.txtStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                    
                    val packageName = doc.getString("subscriptionPackage") ?: "Basic"
                    val expiry = doc.getLong("subscriptionExpiry") ?: 0L
                    
                    holder.binding.txtPackage.text = "Package: $packageName"
                    
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    holder.binding.txtExpiry.text = "Expires: ${sdf.format(Date(expiry))}"
                    
                    val diff = expiry - System.currentTimeMillis()
                    val daysLeft = diff / (1000 * 60 * 60 * 24)
                    holder.binding.txtDaysLeft.text = "$daysLeft days left"
                } else {
                    holder.binding.layoutDetails.visibility = View.GONE
                    holder.binding.txtStatus.setTextColor(android.graphics.Color.RED)
                }
            }

        holder.binding.btnRenew.setOnClickListener {
            onRenew(shop)
        }
    }

    override fun getItemCount() = shops.size
}