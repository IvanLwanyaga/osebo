package com.osebo.ai

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.osebo.ai.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Rotate logo continuously
        val rotateAnimator = ObjectAnimator.ofFloat(
            binding.ivLogo,
            "rotation",
            0f,
            360f
        )

        rotateAnimator.duration = 1500
        rotateAnimator.repeatCount = ObjectAnimator.INFINITE
        rotateAnimator.interpolator = LinearInterpolator()
        rotateAnimator.start()

        // Redirect after 3 seconds
        binding.root.postDelayed({

            rotateAnimator.cancel()

            startActivity(
                Intent(
                    this@SplashActivity,
                    RegisterActivity::class.java
                )
            )

            finish()

        }, 3000)
    }
}