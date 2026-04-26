package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.Device
import com.riversongai.databinding.ItemDeviceBinding

class DeviceAdapter(
    private val onControlAction: (device: Device, command: String, value: String?) -> Unit
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
            binding.textViewDeviceName.text = device.name
            binding.textViewDeviceType.text = "${device.type} • ${device.location}"
            binding.textViewDeviceStatus.text = device.status.replaceFirstChar { it.uppercase() }

            val isOn = device.isOn ?: (device.status == "online" || device.status == "on")
            binding.switchDevice.isChecked = isOn

            binding.switchDevice.setOnCheckedChangeListener(null)
            binding.switchDevice.setOnCheckedChangeListener { _, checked ->
                onControlAction(device, if (checked) "turn_on" else "turn_off", null)
            }

            device.temperature?.let {
                binding.textViewDeviceDetail.text = "%.1f°C".format(it)
            }
            device.brightness?.let {
                binding.textViewDeviceDetail.text = "Brightness: $it%"
            }
            device.batteryLevel?.let {
                binding.textViewDeviceDetail.text = "Battery: $it%"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Device, newItem: Device) = oldItem == newItem
    }
}
