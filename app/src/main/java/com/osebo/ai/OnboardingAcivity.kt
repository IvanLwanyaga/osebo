package com.osebo.ai

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: ImageButton
    private lateinit var btnSkip: TextView
    private lateinit var btnGetStarted: Button
    private lateinit var dotsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding_acivity)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.tvSkip)
        btnGetStarted = findViewById(R.id.btnGetStarted)
        dotsLayout = findViewById(R.id.dotsLayout)

        val items = listOf(
            OnboardingItem(
                "Welcome",
                "Manage everything in one place",
                R.drawable.ic_launcher_foreground
            ),
            OnboardingItem(
                "Smart Control",
                "Track sales, inventory & finance easily",
                R.drawable.ic_launcher_foreground
            ),
            OnboardingItem(
                "Grow Faster",
                "AI insights for better decisions",
                R.drawable.ic_launcher_foreground
            )
        )

        viewPager.adapter = OnboardingAdapter(items)

        setupDots(items.size)
        setActiveDot(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                setActiveDot(position)

                if (position == items.lastIndex) {
                    btnNext.visibility = View.GONE
                    btnGetStarted.visibility = View.VISIBLE
                } else {
                    btnNext.visibility = View.VISIBLE
                    btnGetStarted.visibility = View.GONE
                }
            }
        })

        // NEXT
        btnNext.setOnClickListener {
            if (viewPager.currentItem < items.lastIndex) {
                viewPager.currentItem++
            }
        }

        // SKIP → LOGIN
        btnSkip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // GET STARTED → REGISTER
        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun setupDots(size: Int) {
        dotsLayout.removeAllViews()

        for (i in 0 until size) {
            val dot = View(this)
            dot.setBackgroundResource(R.drawable.dot_inactive)

            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)

            dotsLayout.addView(dot, params)
        }
    }

    private fun setActiveDot(index: Int) {
        for (i in 0 until dotsLayout.childCount) {
            val dot = dotsLayout.getChildAt(i)

            if (i == index) {
                dot.setBackgroundResource(R.drawable.dot_active)
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive)
            }
        }
    }
}