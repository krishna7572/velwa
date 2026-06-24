package com.velwa.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceType {
    EARBUDS, HEADPHONES, SPEAKER, KEYBOARD, MOUSE, WATCH, PHONE, UNKNOWN
}

enum class ConnectionState {
    CONNECTED, DISCONNECTED, CONNECTING, PAIRING
}

@Entity(tableName = "saved_devices")
data class VelwaDevice(
    @PrimaryKey
    val address: String,
    val name: String,
    val alias: String = name,
    val deviceType: DeviceType = DeviceType.UNKNOWN,
    val isFavorite: Boolean = false,
    val lastConnected: Long = 0L,
    val autoConnect: Boolean = false,
    val iconResName: String = "ic_device_earbuds",

    // Runtime (not stored in DB)
    @androidx.room.Ignore val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    @androidx.room.Ignore val batteryLevel: Int = -1, // -1 = unknown
    @androidx.room.Ignore val volume: Int = 50,
    @androidx.room.Ignore val isPlaying: Boolean = false,
    @androidx.room.Ignore val signalStrength: Int = 0 // RSSI
) {
    fun getDeviceIcon(): Int {
        return when (deviceType) {
            DeviceType.EARBUDS -> com.velwa.app.R.drawable.ic_device_earbuds
            DeviceType.HEADPHONES -> com.velwa.app.R.drawable.ic_device_headphones
            DeviceType.SPEAKER -> com.velwa.app.R.drawable.ic_device_speaker
            DeviceType.KEYBOARD -> com.velwa.app.R.drawable.ic_device_keyboard
            DeviceType.MOUSE -> com.velwa.app.R.drawable.ic_device_mouse
            DeviceType.WATCH -> com.velwa.app.R.drawable.ic_device_watch
            DeviceType.PHONE -> com.velwa.app.R.drawable.ic_device_phone
            DeviceType.UNKNOWN -> com.velwa.app.R.drawable.ic_device_unknown
        }
    }

    fun getBatteryIcon(): Int {
        return when {
            batteryLevel < 0 -> com.velwa.app.R.drawable.ic_battery_unknown
            batteryLevel <= 20 -> com.velwa.app.R.drawable.ic_battery_low
            batteryLevel <= 50 -> com.velwa.app.R.drawable.ic_battery_mid
            else -> com.velwa.app.R.drawable.ic_battery_full
        }
    }

    fun isConnected() = connectionState == ConnectionState.CONNECTED
}
