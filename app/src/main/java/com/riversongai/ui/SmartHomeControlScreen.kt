package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R
import com.riversongai.databinding.FragmentSmartHomeControlBinding
import com.riversongai.ui.adapter.DeviceAdapter
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SmartHomeControlScreen : Fragment() {

    private var _binding: FragmentSmartHomeControlBinding? = null
    private val binding get() = _binding!!

    private val smartHomeControlViewModel: SmartHomeControlViewModel by viewModel()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartHomeControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deviceAdapter = DeviceAdapter { device, action, brightnessPct ->
            smartHomeControlViewModel.controlDevice(device.entityId, action, brightnessPct)
        }
        binding.recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deviceAdapter
        }

        smartHomeControlViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                findNavController().navigate(
                    R.id.loginScreen, null,
                    NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
                )
            }
        }

        smartHomeControlViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                deviceAdapter.submitList(it)
                val active = it.count { d -> d.isOn }
                binding.textViewStatus.text = "${it.size} devices · $active on"
            }
        }

        smartHomeControlViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if (isLoading) binding.textViewStatus.text = "Loading…"
        }

        smartHomeControlViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                smartHomeControlViewModel.clearError()
            }
        }

        binding.buttonRefresh.setOnClickListener {
            smartHomeControlViewModel.fetchDevices()
        }

        smartHomeControlViewModel.fetchDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
