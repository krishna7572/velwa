package com.velwa.app.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.velwa.app.R
import com.velwa.app.data.models.ConnectionState
import com.velwa.app.data.models.VelwaDevice
import com.velwa.app.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onConnect: (VelwaDevice) -> Unit,
    private val onDisconnect: (VelwaDevice) -> Unit,
    private val onClick: (VelwaDevice) -> Unit,
    private val onFavorite: (VelwaDevice) -> Unit
) : ListAdapter<VelwaDevice, DeviceAdapter.DeviceViewHolder>(DiffCallback()) {

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: VelwaDevice) {
            binding.deviceName.text = device.alias
            binding.deviceAddress.text = device.address
            binding.deviceIcon.setImageResource(device.getDeviceIcon())
            binding.deviceTypeChip.text = device.deviceType.name
                .lowercase().replaceFirstChar { it.uppercase() }

            // Connection state
            val (statusText, statusColor) = when (device.connectionState) {
                ConnectionState.CONNECTED -> Pair("● Connected", R.color.neon_green)
                ConnectionState.CONNECTING -> Pair("◌ Connecting...", R.color.neon_blue)
                ConnectionState.PAIRING -> Pair("◌ Pairing...", R.color.neon_purple)
                ConnectionState.DISCONNECTED -> Pair("○ Disconnected", R.color.text_secondary)
            }
            binding.connectionStatus.text = statusText
            binding.connectionStatus.setTextColor(binding.root.context.getColor(statusColor))

            // Battery
            if (device.batteryLevel >= 0) {
                binding.batteryIcon.setImageResource(device.getBatteryIcon())
                binding.batteryText.text = "${device.batteryLevel}%"
                binding.batteryGroup.visibility = View.VISIBLE
            } else {
                binding.batteryGroup.visibility = View.GONE
            }

            // Favorite
            binding.favoriteIcon.setImageResource(
                if (device.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite
            )

            // Connect/Disconnect button
            if (device.isConnected()) {
                binding.actionBtn.text = "Disconnect"
                binding.actionBtn.setBackgroundColor(binding.root.context.getColor(R.color.error_red))
            } else {
                binding.actionBtn.text = "Connect"
                binding.actionBtn.setBackgroundColor(binding.root.context.getColor(R.color.neon_blue))
            }

            // Listeners
            binding.root.setOnClickListener { onClick(device) }
            binding.actionBtn.setOnClickListener {
                if (device.isConnected()) onDisconnect(device)
                else onConnect(device)
            }
            binding.favoriteIcon.setOnClickListener { onFavorite(device) }

            // Animated pulse if connected
            if (device.connectionState == ConnectionState.CONNECTED) {
                binding.connectedPulse.visibility = View.VISIBLE
            } else {
                binding.connectedPulse.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<VelwaDevice>() {
        override fun areItemsTheSame(o: VelwaDevice, n: VelwaDevice) = o.address == n.address
        override fun areContentsTheSame(o: VelwaDevice, n: VelwaDevice) = o == n
    }
}
