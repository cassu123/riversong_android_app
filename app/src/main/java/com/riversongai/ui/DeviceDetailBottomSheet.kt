package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.riversongai.R
import com.riversongai.databinding.LayoutDeviceDetailBottomSheetBinding
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeviceDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutDeviceDetailBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val smartHomeControlViewModel: SmartHomeControlViewModel by viewModel()

    private var entityId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entityId = arguments?.getString("entity_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutDeviceDetailBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = entityId ?: run {
            dismiss()
            return
        }

        smartHomeControlViewModel.devices.observe(viewLifecycleOwner) { devices ->
            val device = devices.find { it.entityId == id }
            if (device != null) {
                binding.textViewDeviceName.text = device.name
                binding.textViewDeviceIcon.text = device.icon
                binding.chipDeviceState.text = device.stateDisplay
                
                val isLight = device.domain == "light"
                binding.layoutBrightness.isVisible = isLight
                if (isLight) {
                    val brightness = device.brightnessPercent ?: 0
                    binding.textViewBrightnessLabel.text = "Brightness: $brightness%"
                    binding.sliderBrightness.value = brightness.toFloat()
                }
            }
        }

        binding.sliderBrightness.addOnSliderTouchListener(object : com.google.android.material.slider.Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) {}
            override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                smartHomeControlViewModel.controlDevice(id, "set_brightness", slider.value.toInt())
            }
        })

        binding.sliderBrightness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                binding.textViewBrightnessLabel.text = "Brightness: ${value.toInt()}%"
            }
        }

        binding.buttonTurnOn.setOnClickListener {
            smartHomeControlViewModel.controlDevice(id, "turn_on")
        }

        binding.buttonTurnOff.setOnClickListener {
            smartHomeControlViewModel.controlDevice(id, "turn_off")
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialog

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(entityId: String): DeviceDetailBottomSheet {
            return DeviceDetailBottomSheet().apply {
                arguments = bundleOf("entity_id" to entityId)
            }
        }
    }
}
