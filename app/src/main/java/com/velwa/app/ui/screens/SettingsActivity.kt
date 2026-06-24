package com.velwa.app.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.velwa.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupSettings()
    }

    private fun setupSettings() {
        // Theme toggle
        binding.darkModeSwitch.isChecked = true // always dark by default
        binding.darkModeSwitch.setOnCheckedChangeListener { _, _ ->
            Toast.makeText(this, "Theme change ho raha hai...", Toast.LENGTH_SHORT).show()
        }

        // Notification toggle
        binding.notifSwitch.setOnCheckedChangeListener { _, checked ->
            val msg = if (checked) "Notifications ON" else "Notifications OFF"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Auto-scan toggle
        binding.autoScanSwitch.setOnCheckedChangeListener { _, checked ->
            val msg = if (checked) "Auto-scan ON hai" else "Auto-scan OFF"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // About
        binding.aboutBtn.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Velwa v1.0")
                .setMessage("Velwa — Next-Gen Bluetooth Manager\n\nBanaya gaya with ❤️ by Krishna\n\nGitHub: github.com/krishna/velwa")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
