package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.Device
import com.riversongai.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onControlAction: (device: Device, action: String, brightnessPct: Int?) -> Unit
) : ListAdapter<Device, DeviceAdapter.DeviceViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.textViewDeviceName.text = "${device.icon}  ${device.name}"
            binding.textViewDeviceType.text = device.domain.replaceFirstChar { it.uppercase() }
            binding.textViewDeviceStatus.text = device.state.replaceFirstChar { it.uppercase() }

            device.brightnessPercent?.let {
                binding.textViewDeviceDetail.text = "Brightness: $it%"
            } ?: device.currentTemperature?.let {
                binding.textViewDeviceDetail.text = "%.1f°".format(it)
            } ?: run {
                binding.textViewDeviceDetail.text = ""
            }

            binding.switchDevice.setOnCheckedChangeListener(null)
            binding.switchDevice.isChecked = device.isOn
            binding.switchDevice.setOnCheckedChangeListener { _, checked ->
                onControlAction(device, if (checked) "turn_on" else "turn_off", null)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device) =
            oldItem.entityId == newItem.entityId
        override fun areContentsTheSame(oldItem: Device, newItem: Device) =
            oldItem == newItem
    }
}
