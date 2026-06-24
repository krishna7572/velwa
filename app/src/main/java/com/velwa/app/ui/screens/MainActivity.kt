package com.velwa.app.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.velwa.app.R
import com.velwa.app.data.bluetooth.BluetoothReceiver
import com.velwa.app.databinding.ActivityMainBinding
import com.velwa.app.ui.components.DevicePagerAdapter
import com.velwa.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var btReceiver: BluetoothReceiver

    private val BT_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            setupUI()
        } else {
            Toast.makeText(this, "Bluetooth permission chahiye!", Toast.LENGTH_LONG).show()
        }
    }

    private val enableBtLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (viewModel.btManager.isBluetoothEnabled()) {
            viewModel.updateBtState(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissionsAndSetup()
    }

    private fun checkPermissionsAndSetup() {
        val missing = BT_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            setupUI()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun setupUI() {
        setupViewPager()
        setupBottomNav()
        setupBtToggle()
        setupFab()
        registerBtReceiver()
        observeViewModel()

        // Enable BT if off
        if (!viewModel.btManager.isBluetoothEnabled()) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = DevicePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Devices"
                1 -> "Scan"
                2 -> "Favorites"
                else -> ""
            }
            tab.setIcon(when (pos) {
                0 -> R.drawable.ic_bluetooth
                1 -> R.drawable.ic_scan
                2 -> R.drawable.ic_favorite
                else -> R.drawable.ic_bluetooth
            })
        }.attach()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.viewPager.currentItem = 0
                R.id.nav_scan -> binding.viewPager.currentItem = 1
                R.id.nav_favorites -> binding.viewPager.currentItem = 2
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBtToggle() {
        binding.btToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !viewModel.btManager.isBluetoothEnabled()) {
                enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    private fun setupFab() {
        binding.fabScan.setOnClickListener {
            binding.viewPager.currentItem = 1
            viewModel.startScan()
            binding.fabScan.animate().rotationBy(360f).setDuration(600).start()
        }
    }

    private fun registerBtReceiver() {
        btReceiver = BluetoothReceiver(
            btManager = viewModel.btManager,
            onStateChanged = { enabled ->
                viewModel.updateBtState(enabled)
                binding.btToggle.isChecked = enabled
            },
            onDeviceFound = { device ->
                viewModel.onDeviceFoundFromReceiver(device)
            },
            onDiscoveryFinished = {
                // Scan done
            }
        )
        registerReceiver(btReceiver, BluetoothReceiver.getIntentFilter())
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.statusMessage.collect { msg ->
                msg?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.btEnabled.collect { enabled ->
                binding.btToggle.isChecked = enabled
                binding.btStatusText.text = if (enabled) "Bluetooth ON" else "Bluetooth OFF"
                binding.fabScan.visibility = if (enabled) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(btReceiver) } catch (_: Exception) {}
    }
}
