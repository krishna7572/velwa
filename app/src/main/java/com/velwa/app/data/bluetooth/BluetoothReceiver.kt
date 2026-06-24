package com.velwa.app.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BluetoothReceiver(
    private val btManager: VelwaBluetoothManager,
    private val onStateChanged: (Boolean) -> Unit,
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onDiscoveryFinished: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                onStateChanged(state == BluetoothAdapter.STATE_ON)
            }
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let {
                    btManager.onDeviceFound(it)
                    onDeviceFound(it)
                }
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryFinished()
            }
        }
    }

    companion object {
        fun getIntentFilter() = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
    }
}

// Boot receiver to restore auto-connect devices
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, BluetoothService::class.java).apply {
                action = BluetoothService.ACTION_AUTO_CONNECT
            }
            context.startForegroundService(serviceIntent)
        }
    }
}
