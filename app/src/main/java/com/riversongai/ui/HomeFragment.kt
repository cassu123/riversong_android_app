package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.riversongai.R
import com.riversongai.data.model.Device
import com.riversongai.databinding.FragmentHomeBinding
import com.riversongai.ui.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewDate.text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        homeViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                findNavController().navigate(
                    R.id.loginScreen, null,
                    NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
                )
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                val greetingRes = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> R.string.home_greeting_morning
                    in 12..16 -> R.string.home_greeting_afternoon
                    else -> R.string.home_greeting_evening
                }
                binding.textViewGreeting.text = getString(greetingRes) + ", ${it.firstName}!"
            }
        }

        homeViewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                binding.textViewTemp.text = "%.0f°".format(weather.current.tempC)
                binding.textViewWeatherStatus.text = weather.current.conditionText
            } else {
                binding.textViewTemp.text = "--°"
                binding.textViewWeatherStatus.text = getString(R.string.home_weather_unavailable)
            }
        }

        homeViewModel.devices.observe(viewLifecycleOwner) { devices ->
            updateRecentDevices(devices.orEmpty().take(3))
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                homeViewModel.clearError()
            }
        }

        binding.editTextQuickChat.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val text = binding.editTextQuickChat.text.toString().trim()
                if (text.isNotBlank()) {
                    val bundle = bundleOf("message" to text)
                    findNavController().navigate(R.id.chatFragment, bundle)
                    binding.editTextQuickChat.text?.clear()
                }
                true
            } else false
        }

        binding.buttonViewAllDevices.setOnClickListener {
            findNavController().navigate(R.id.smartHomeControlScreen)
        }

        homeViewModel.loadUserDataAndDevices()
    }

    private fun updateRecentDevices(devices: List<Device>) {
        binding.layoutRecentDevices.removeAllViews()
        binding.textViewNoDevices.isVisible = devices.isEmpty()
        binding.textViewNoDevices.text = getString(R.string.home_no_devices)
        
        val inflater = LayoutInflater.from(context)
        devices.forEach { device ->
            val itemView = inflater.inflate(R.layout.item_device, binding.layoutRecentDevices, false)
            itemView.findViewById<TextView>(R.id.textViewDeviceName).text = "${device.icon} ${device.name}"
            itemView.findViewById<TextView>(R.id.textViewDeviceStatus).text = device.stateDisplay

            val toggle = itemView.findViewById<SwitchCompat>(R.id.switchDevice)
            toggle.setOnCheckedChangeListener(null)
            toggle.isChecked = device.isOn
            toggle.setOnCheckedChangeListener { _, checked ->
                homeViewModel.toggleDevice(device.entityId, checked)
            }
            
            binding.layoutRecentDevices.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
