package com.velwa.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.velwa.app.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo
        binding.logoImage.alpha = 0f
        binding.logoImage.animate().alpha(1f).setDuration(800).start()

        binding.appName.alpha = 0f
        binding.appName.animate().alpha(1f).setStartDelay(300).setDuration(600).start()

        binding.tagline.alpha = 0f
        binding.tagline.animate().alpha(1f).setStartDelay(600).setDuration(600).start()

        // Navigate to Main after 2.2s
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2200)
    }
}
