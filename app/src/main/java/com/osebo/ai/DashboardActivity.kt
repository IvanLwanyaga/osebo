package com.osebo.ai

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        // =========================
        // TOOLBAR
        // =========================

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        // =========================
        // USERNAME
        // =========================

        val tvUserName =
            findViewById<TextView>(R.id.tvUserName)

        tvUserName.text =
            "Welcome Back 👋"

        // =========================
        // BUTTONS
        // =========================

        val btnAddShop =
            findViewById<MaterialButton>(R.id.btnAddShop)

        val btnReports =
            findViewById<MaterialButton>(R.id.btnReports)

        val fabAdd =
            findViewById<FloatingActionButton>(R.id.fabAdd)

        btnAddShop.setOnClickListener {

            Toast.makeText(
                this,
                "Add Shop Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnReports.setOnClickListener {

            Toast.makeText(
                this,
                "Reports Opened",
                Toast.LENGTH_SHORT
            ).show()
        }

        fabAdd.setOnClickListener {

            Toast.makeText(
                this,
                "Floating Action Button Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // LINE CHART
        // =========================

        val lineChart =
            findViewById<LineChart>(R.id.lineChart)

        lineChart.description.isEnabled = false

        lineChart.setTouchEnabled(true)

        lineChart.animateX(1000)

        // =========================
        // RECYCLER VIEW
        // =========================

        val rvShops =
            findViewById<RecyclerView>(R.id.rvShops)

        rvShops.layoutManager =
            LinearLayoutManager(this)

        // =========================
        // BOTTOM NAVIGATION
        // =========================

        val bottomNavigation =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {

                    Toast.makeText(
                        this,
                        "Home",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                R.id.nav_sales -> {

                    Toast.makeText(
                        this,
                        "Sales",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                R.id.nav_profile -> {

                    Toast.makeText(
                        this,
                        "Profile",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                else -> false
            }
        }
    }
}