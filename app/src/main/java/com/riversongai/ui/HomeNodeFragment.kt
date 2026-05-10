package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.data.model.Device
import com.riversongai.databinding.FragmentHomeNodeBinding
import com.riversongai.ui.adapter.SmartHomeGroupedAdapter
import com.riversongai.ui.adapter.SmartHomeListItem
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeNodeFragment : Fragment(R.layout.fragment_home_node) {

    private var _binding: FragmentHomeNodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SmartHomeControlViewModel by viewModel()
    
    private lateinit var deviceAdapter: SmartHomeGroupedAdapter
    private var currentDomainFilter = "All"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeNodeBinding.bind(view)

        setupUI()
        observeViewModel()
        
        viewModel.fetchStatus()
        viewModel.fetchDevices()
    }

    private fun setupUI() {
        deviceAdapter = SmartHomeGroupedAdapter(
            onItemClick = { /* Maybe show detail? */ },
            onQuickToggle = { device, checked ->
                val action = if (checked) "turn_on" else "turn_off"
                viewModel.controlDevice(device.entityId, action)
            }
        )
        binding.recyclerViewDevices.apply {
            adapter = deviceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchStatus()
            viewModel.fetchDevices()
        }

        binding.buttonRetry.setOnClickListener {
            viewModel.fetchStatus()
            viewModel.fetchDevices()
        }

        binding.chipGroupDomains.setOnCheckedStateChangeListener { _, checkedIds ->
            currentDomainFilter = when (checkedIds.firstOrNull()) {
                R.id.chipLights -> "light"
                R.id.chipSwitches -> "switch"
                R.id.chipFans -> "fan"
                R.id.chipLocks -> "lock"
                R.id.chipClimate -> "climate"
                else -> "All"
            }
            updateFilteredList(viewModel.devices.value.orEmpty())
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.swipeRefresh.isRefreshing = it
        }

        viewModel.status.observe(viewLifecycleOwner) { status ->
            status?.let {
                binding.textStatusTitle.text = if (!it.configured) "Home Assistant Not Connected" 
                    else if (!it.reachable) "Home Assistant Unreachable"
                    else "Connected to Home Assistant"
                
                val dotColor = if (!it.configured) R.color.river_song_outline
                    else if (!it.reachable) android.R.color.holo_red_dark
                    else android.R.color.holo_green_dark
                
                binding.viewStatusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), dotColor)
                )

                binding.textStatusMessage.text = if (!it.configured) "Connect your Home Assistant instance..."
                    else if (!it.reachable) "Check that your HA instance is running"
                    else it.url ?: ""

                binding.layoutSetupInstructions.isVisible = !it.configured
                binding.buttonRetry.isVisible = it.configured && !it.reachable
                binding.scrollViewDomains.isVisible = it.reachable
            }
        }

        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            updateFilteredList(devices)
        }
    }

    private fun updateFilteredList(allDevices: List<Device>) {
        val filtered = if (currentDomainFilter == "All") allDevices 
            else allDevices.filter { it.domain == currentDomainFilter }
        
        val items = filtered.groupBy { it.room ?: "General" }
            .flatMap { (room, list) ->
                listOf(SmartHomeListItem.Header(room)) + list.map { SmartHomeListItem.DeviceItem(it) }
            }
        deviceAdapter.submitList(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
