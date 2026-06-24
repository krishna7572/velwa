package com.velwa.app.data.bluetooth

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.velwa.app.data.models.ConnectionState
import com.velwa.app.data.models.DeviceType
import com.velwa.app.data.models.VelwaDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VelwaBluetoothManager(private val context: Context) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _scannedDevices = MutableStateFlow<List<VelwaDevice>>(emptyList())
    val scannedDevices: StateFlow<List<VelwaDevice>> = _scannedDevices

    private val _connectedDevices = MutableStateFlow<List<VelwaDevice>>(emptyList())
    val connectedDevices: StateFlow<List<VelwaDevice>> = _connectedDevices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError

    private val scannedList = mutableListOf<VelwaDevice>()
    private val connectedGatts = mutableMapOf<String, BluetoothGatt>()
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 12000L

    // ── Bluetooth Enabled Check ─────────────────────────
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun hasPermissions(): Boolean {
        val perms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return perms.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ── Scan for Classic BT Devices ─────────────────────
    fun startClassicScan() {
        if (!hasPermissions()) {
            _scanError.value = "Bluetooth permission nahi mili"
            return
        }
        scannedList.clear()
        _scannedDevices.value = emptyList()
        _isScanning.value = true

        try {
            bluetoothAdapter?.startDiscovery()
            handler.postDelayed({ stopScan() }, SCAN_PERIOD)
        } catch (e: SecurityException) {
            _scanError.value = "Permission error: ${e.message}"
            _isScanning.value = false
        }
    }

    fun stopScan() {
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (_: SecurityException) {}
        _isScanning.value = false
    }

    // Called from BroadcastReceiver
    fun onDeviceFound(device: BluetoothDevice) {
        try {
            val name = device.name ?: return
            val existing = scannedList.find { it.address == device.address }
            if (existing == null) {
                val velwaDevice = VelwaDevice(
                    address = device.address,
                    name = name,
                    deviceType = guessDeviceType(device),
                    connectionState = if (isDeviceConnected(device)) ConnectionState.CONNECTED
                    else ConnectionState.DISCONNECTED
                )
                scannedList.add(velwaDevice)
                _scannedDevices.value = scannedList.toList()
            }
        } catch (_: SecurityException) {}
    }

    // ── Get Already Paired Devices ──────────────────────
    fun getPairedDevices(): List<VelwaDevice> {
        if (!hasPermissions()) return emptyList()
        return try {
            bluetoothAdapter?.bondedDevices?.map { device ->
                VelwaDevice(
                    address = device.address,
                    name = device.name ?: "Unknown Device",
                    deviceType = guessDeviceType(device),
                    connectionState = if (isDeviceConnected(device)) ConnectionState.CONNECTED
                    else ConnectionState.DISCONNECTED,
                    batteryLevel = getBatteryLevel(device)
                )
            } ?: emptyList()
        } catch (_: SecurityException) { emptyList() }
    }

    // ── Connect Device ───────────────────────────────────
    fun connectDevice(address: String, onResult: (Boolean, String) -> Unit) {
        if (!hasPermissions()) {
            onResult(false, "Permission nahi mili")
            return
        }
        try {
            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device == null) {
                onResult(false, "Device nahi mila")
                return
            }
            // For A2DP (audio) devices, we use the profile proxy
            bluetoothAdapter?.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    if (profile == BluetoothProfile.A2DP) {
                        onResult(true, "Connect ho gaya!")
                    }
                }
                override fun onServiceDisconnected(profile: Int) {
                    onResult(false, "Disconnect ho gaya")
                }
            }, BluetoothProfile.A2DP)
        } catch (e: SecurityException) {
            onResult(false, "Security error: ${e.message}")
        }
    }

    // ── Disconnect Device ────────────────────────────────
    fun disconnectDevice(address: String) {
        connectedGatts[address]?.let { gatt ->
            try { gatt.disconnect() } catch (_: SecurityException) {}
        }
    }

    // ── Volume Control ───────────────────────────────────
    fun setVolume(volumePercent: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val vol = (volumePercent / 100f * maxVol).toInt().coerceIn(0, maxVol)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
    }

    fun getVolume(): Int {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (maxVol > 0) (curVol * 100f / maxVol).toInt() else 0
    }

    fun volumeUp() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun volumeDown() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }

    fun muteToggle() {
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_TOGGLE_MUTE,
            AudioManager.FLAG_SHOW_UI
        )
    }

    // ── Battery Level ────────────────────────────────────
    fun getBatteryLevel(device: BluetoothDevice): Int {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            method.invoke(device) as? Int ?: -1
        } catch (_: Exception) { -1 }
    }

    // ── Helpers ──────────────────────────────────────────
    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return try {
            bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).any {
                it.address == device.address
            }
        } catch (_: SecurityException) { false }
    }

    private fun guessDeviceType(device: BluetoothDevice): DeviceType {
        return try {
            val name = device.name?.lowercase() ?: return DeviceType.UNKNOWN
            val btClass = device.bluetoothClass
            when {
                name.contains("bud") || name.contains("earbud") || name.contains("pod") -> DeviceType.EARBUDS
                name.contains("headphone") || name.contains("headset") || name.contains("wh-") -> DeviceType.HEADPHONES
                name.contains("speaker") || name.contains("jbl") || name.contains("bose") -> DeviceType.SPEAKER
                name.contains("keyboard") -> DeviceType.KEYBOARD
                name.contains("mouse") -> DeviceType.MOUSE
                name.contains("watch") || name.contains("band") -> DeviceType.WATCH
                btClass?.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO -> DeviceType.EARBUDS
                btClass?.majorDeviceClass == BluetoothClass.Device.Major.COMPUTER -> DeviceType.KEYBOARD
                btClass?.majorDeviceClass == BluetoothClass.Device.Major.PHONE -> DeviceType.PHONE
                else -> DeviceType.UNKNOWN
            }
        } catch (_: SecurityException) { DeviceType.UNKNOWN }
    }

    fun cleanup() {
        stopScan()
        connectedGatts.values.forEach {
            try { it.close() } catch (_: SecurityException) {}
        }
        connectedGatts.clear()
    }
}
