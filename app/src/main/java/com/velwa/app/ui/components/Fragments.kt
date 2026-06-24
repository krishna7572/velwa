package com.velwa.app.ui.components

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.velwa.app.data.models.ConnectionState
import com.velwa.app.data.models.VelwaDevice
import com.velwa.app.databinding.FragmentDevicesBinding
import com.velwa.app.databinding.FragmentScanBinding
import com.velwa.app.databinding.FragmentFavoritesBinding
import com.velwa.app.ui.screens.DeviceDetailActivity
import com.velwa.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// ── Pager Adapter ────────────────────────────────────────
class DevicePagerAdapter(activity: androidx.fragment.app.FragmentActivity) :
    FragmentStateAdapter(activity) {
    override fun getItemCount() = 3
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DevicesFragment()
        1 -> ScanFragment()
        2 -> FavoritesFragment()
        else -> DevicesFragment()
    }
}

// ── Devices Fragment (Saved/Paired) ──────────────────────
class DevicesFragment : Fragment() {
    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: DeviceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DeviceAdapter(
            onConnect = { device -> viewModel.connectDevice(device) },
            onDisconnect = { device -> viewModel.disconnectDevice(device) },
            onClick = { device -> openDetail(device) },
            onFavorite = { device -> viewModel.toggleFavorite(device) }
        )
        binding.devicesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.devicesRecycler.adapter = adapter

        viewModel.savedDevices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            binding.emptyView.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        }

        // Also show paired devices
        binding.pairedHeader.setOnClickListener {
            val paired = viewModel.getPairedDevices()
            if (paired.isEmpty()) {
                Toast.makeText(requireContext(), "Koi paired device nahi mila", Toast.LENGTH_SHORT).show()
            } else {
                paired.forEach { viewModel.saveDevice(it) }
            }
        }
    }

    private fun openDetail(device: VelwaDevice) {
        startActivity(Intent(requireContext(), DeviceDetailActivity::class.java).apply {
            putExtra("device_address", device.address)
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Scan Fragment ────────────────────────────────────────
class ScanFragment : Fragment() {
    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: DeviceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DeviceAdapter(
            onConnect = { device ->
                viewModel.saveDevice(device)
                viewModel.connectDevice(device)
            },
            onDisconnect = { device -> viewModel.disconnectDevice(device) },
            onClick = {},
            onFavorite = {}
        )
        binding.scanRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.scanRecycler.adapter = adapter

        binding.scanBtn.setOnClickListener {
            viewModel.startScan()
        }

        lifecycleScope.launch {
            viewModel.scannedDevices.collect { devices ->
                adapter.submitList(devices)
                binding.scanEmptyText.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.isScanning.collect { scanning ->
                binding.scanProgress.visibility = if (scanning) View.VISIBLE else View.GONE
                binding.scanBtn.text = if (scanning) "Scan ho raha hai..." else "Scan Shuru Karo"
                binding.scanBtn.isEnabled = !scanning
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

// ── Favorites Fragment ───────────────────────────────────
class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: DeviceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DeviceAdapter(
            onConnect = { device -> viewModel.connectDevice(device) },
            onDisconnect = { device -> viewModel.disconnectDevice(device) },
            onClick = { device ->
                startActivity(Intent(requireContext(), DeviceDetailActivity::class.java).apply {
                    putExtra("device_address", device.address)
                })
            },
            onFavorite = { device -> viewModel.toggleFavorite(device) }
        )
        binding.favRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.favRecycler.adapter = adapter

        viewModel.favoriteDevices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            binding.favEmptyView.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
