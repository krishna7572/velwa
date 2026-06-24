package com.velwa.app.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.*
import com.velwa.app.data.bluetooth.VelwaBluetoothManager
import com.velwa.app.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val btManager = VelwaBluetoothManager(application)
    private val db = VelwaDatabase.getDatabase(application)
    private val dao = db.deviceDao()

    // ── DB backed ────────────────────────────────────────
    val savedDevices: LiveData<List<VelwaDevice>> = dao.getAllDevices()
    val favoriteDevices: LiveData<List<VelwaDevice>> = dao.getFavoriteDevices()

    // ── Scan state ───────────────────────────────────────
    val scannedDevices: StateFlow<List<VelwaDevice>> = btManager.scannedDevices
    val isScanning: StateFlow<Boolean> = btManager.isScanning
    val scanError: StateFlow<String?> = btManager.scanError

    // ── UI state ─────────────────────────────────────────
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _selectedDevice = MutableLiveData<VelwaDevice?>()
    val selectedDevice: LiveData<VelwaDevice?> = _selectedDevice

    // ── BT state ─────────────────────────────────────────
    private val _btEnabled = MutableStateFlow(btManager.isBluetoothEnabled())
    val btEnabled: StateFlow<Boolean> = _btEnabled

    // ── Volume ───────────────────────────────────────────
    private val _volume = MutableStateFlow(btManager.getVolume())
    val volume: StateFlow<Int> = _volume

    fun updateBtState(enabled: Boolean) { _btEnabled.value = enabled }

    // ── Scan ─────────────────────────────────────────────
    fun startScan() {
        btManager.startClassicScan()
    }

    fun stopScan() {
        btManager.stopScan()
    }

    fun onDeviceFoundFromReceiver(device: BluetoothDevice) {
        btManager.onDeviceFound(device)
    }

    // ── Connect / Disconnect ─────────────────────────────
    fun connectDevice(device: VelwaDevice) {
        _statusMessage.value = "${device.name} se connect ho raha hai..."
        btManager.connectDevice(device.address) { success, msg ->
            _statusMessage.value = msg
            if (success) {
                viewModelScope.launch {
                    dao.updateLastConnected(device.address, System.currentTimeMillis())
                }
            }
        }
    }

    fun disconnectDevice(device: VelwaDevice) {
        btManager.disconnectDevice(device.address)
        _statusMessage.value = "${device.name} disconnect ho gaya"
    }

    // ── Save / Remove Device ─────────────────────────────
    fun saveDevice(device: VelwaDevice) {
        viewModelScope.launch {
            dao.insertDevice(device)
            _statusMessage.value = "${device.name} save ho gaya!"
        }
    }

    fun removeDevice(device: VelwaDevice) {
        viewModelScope.launch {
            dao.deleteDevice(device)
            _statusMessage.value = "${device.name} remove ho gaya"
        }
    }

    fun toggleFavorite(device: VelwaDevice) {
        viewModelScope.launch {
            dao.setFavorite(device.address, !device.isFavorite)
        }
    }

    fun updateAlias(device: VelwaDevice, alias: String) {
        viewModelScope.launch {
            dao.updateAlias(device.address, alias)
        }
    }

    fun setAutoConnect(device: VelwaDevice, auto: Boolean) {
        viewModelScope.launch {
            dao.setAutoConnect(device.address, auto)
        }
    }

    // ── Volume ───────────────────────────────────────────
    fun setVolume(percent: Int) {
        btManager.setVolume(percent)
        _volume.value = percent
    }

    fun volumeUp() {
        btManager.volumeUp()
        _volume.value = btManager.getVolume()
    }

    fun volumeDown() {
        btManager.volumeDown()
        _volume.value = btManager.getVolume()
    }

    fun muteToggle() {
        btManager.muteToggle()
        _volume.value = btManager.getVolume()
    }

    fun refreshVolume() {
        _volume.value = btManager.getVolume()
    }

    // ── Paired Devices ───────────────────────────────────
    fun getPairedDevices(): List<VelwaDevice> = btManager.getPairedDevices()

    fun selectDevice(device: VelwaDevice) {
        _selectedDevice.value = device
    }

    override fun onCleared() {
        super.onCleared()
        btManager.cleanup()
    }
}
