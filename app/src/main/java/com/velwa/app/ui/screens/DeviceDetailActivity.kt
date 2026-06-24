package com.velwa.app.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.velwa.app.R
import com.velwa.app.data.models.ConnectionState
import com.velwa.app.data.models.VelwaDevice
import com.velwa.app.databinding.ActivityDeviceDetailBinding
import com.velwa.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class DeviceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceDetailBinding
    private val viewModel: MainViewModel by viewModels()
    private var device: VelwaDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get device from intent
        val address = intent.getStringExtra("device_address") ?: run { finish(); return }
        viewModel.savedDevices.observe(this) { devices ->
            device = devices.find { it.address == address }
            device?.let { populateUI(it) }
        }

        setupToolbar()
        setupVolumeControls()
        setupActionButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun populateUI(d: VelwaDevice) {
        supportActionBar?.title = d.alias

        binding.deviceIcon.setImageResource(d.getDeviceIcon())
        binding.deviceName.text = d.alias
        binding.deviceAddress.text = d.address
        binding.deviceType.text = d.deviceType.name.lowercase().replaceFirstChar { it.uppercase() }

        // Battery
        if (d.batteryLevel >= 0) {
            binding.batteryIcon.setImageResource(d.getBatteryIcon())
            binding.batteryText.text = "${d.batteryLevel}%"
            binding.batteryGroup.visibility = View.VISIBLE
        } else {
            binding.batteryGroup.visibility = View.GONE
        }

        // Connection state
        val (stateText, stateColor) = when (d.connectionState) {
            ConnectionState.CONNECTED -> Pair("Connected ✓", getColor(R.color.neon_green))
            ConnectionState.CONNECTING -> Pair("Connecting...", getColor(R.color.neon_blue))
            ConnectionState.PAIRING -> Pair("Pairing...", getColor(R.color.neon_purple))
            ConnectionState.DISCONNECTED -> Pair("Disconnected", getColor(R.color.text_secondary))
        }
        binding.connectionStatus.text = stateText
        binding.connectionStatus.setTextColor(stateColor)

        // Connect/disconnect button
        if (d.isConnected()) {
            binding.connectBtn.text = "Disconnect"
            binding.connectBtn.setBackgroundColor(getColor(R.color.error_red))
        } else {
            binding.connectBtn.text = "Connect Karo"
            binding.connectBtn.setBackgroundColor(getColor(R.color.neon_blue))
        }

        // Favorite toggle
        binding.favoriteBtn.setImageResource(
            if (d.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
        )

        // Auto-connect switch
        binding.autoConnectSwitch.isChecked = d.autoConnect
    }

    private fun setupVolumeControls() {
        lifecycleScope.launch {
            viewModel.volume.collect { vol ->
                binding.volumeSeekbar.progress = vol
                binding.volumeValue.text = "$vol%"
            }
        }

        binding.volumeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setVolume(progress)
                    binding.volumeValue.text = "$progress%"
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        binding.volDownBtn.setOnClickListener { viewModel.volumeDown() }
        binding.volUpBtn.setOnClickListener { viewModel.volumeUp() }
        binding.muteBtn.setOnClickListener { viewModel.muteToggle() }
    }

    private fun setupActionButtons() {
        // Connect/Disconnect
        binding.connectBtn.setOnClickListener {
            device?.let { d ->
                if (d.isConnected()) viewModel.disconnectDevice(d)
                else viewModel.connectDevice(d)
            }
        }

        // Favorite
        binding.favoriteBtn.setOnClickListener {
            device?.let { viewModel.toggleFavorite(it) }
        }

        // Rename / Alias
        binding.renameBtn.setOnClickListener {
            device?.let { d ->
                val input = android.widget.EditText(this).apply {
                    setText(d.alias)
                    hint = "Naya naam likho"
                }
                AlertDialog.Builder(this)
                    .setTitle("Device ka naam badlo")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newName = input.text.toString().trim()
                        if (newName.isNotEmpty()) {
                            viewModel.updateAlias(d, newName)
                            Toast.makeText(this, "Naam badal gaya!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        // Auto-connect
        binding.autoConnectSwitch.setOnCheckedChangeListener { _, checked ->
            device?.let { viewModel.setAutoConnect(it, checked) }
        }

        // Remove device
        binding.removeBtn.setOnClickListener {
            device?.let { d ->
                AlertDialog.Builder(this)
                    .setTitle("Device hatao?")
                    .setMessage("${d.alias} ko remove karein?")
                    .setPositiveButton("Haan, hatao") { _, _ ->
                        viewModel.removeDevice(d)
                        finish()
                    }
                    .setNegativeButton("Nahi", null)
                    .show()
            }
        }
    }
}
