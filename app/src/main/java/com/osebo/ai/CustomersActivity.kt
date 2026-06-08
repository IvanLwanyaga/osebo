package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.osebo.ai.adapters.CustomerAdapter
import com.osebo.ai.databinding.ActivityCustomersBinding
import com.osebo.ai.models.Customer

class CustomersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomersBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val customerList = mutableListOf<Customer>()
    private val filteredList = mutableListOf<Customer>()
    private lateinit var adapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        startCustomerListener()
    }

    private fun setupUI() {
        adapter = CustomerAdapter(filteredList) { customer ->
            // Open customer details if needed
        }

        binding.recyclerCustomers.apply {
            layoutManager = LinearLayoutManager(this@CustomersActivity)
            this.adapter = this@CustomersActivity.adapter
        }

        binding.swipeRefresh.setOnRefreshListener { 
            binding.swipeRefresh.isRefreshing = false 
        }

        binding.btnAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCustomers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun startCustomerListener() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("customers")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error listening for customers", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                customerList.clear()
                value?.documents?.forEach { doc ->
                    val customer = doc.toObject(Customer::class.java)?.copy(id = doc.id)
                    customer?.let { customerList.add(it) }
                }
                
                binding.txtTotalCustomers.text = customerList.size.toString()
                filterCustomers(binding.etSearch.text.toString())
            }
    }

    private fun filterCustomers(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(customerList)
        } else {
            val q = query.lowercase()
            filteredList.addAll(customerList.filter { 
                it.name.lowercase().contains(q) || it.phone.contains(q) || it.email.lowercase().contains(q)
            })
        }
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        binding.layoutEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerCustomers.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
    }
}
