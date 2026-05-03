package com.riversongai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.riversongai.data.model.Device
import com.riversongai.databinding.ItemDeviceBinding
import com.riversongai.databinding.ItemRoomHeaderBinding

private const val TYPE_HEADER = 0
private const val TYPE_DEVICE = 1

class SmartHomeGroupedAdapter(
    private val onItemClick: (device: Device) -> Unit,
    private val onQuickToggle: (device: Device, checked: Boolean) -> Unit
) : ListAdapter<SmartHomeListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SmartHomeListItem.Header -> TYPE_HEADER
            is SmartHomeListItem.DeviceItem -> TYPE_DEVICE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(ItemRoomHeaderBinding.inflate(inflater, parent, false))
        } else {
            DeviceViewHolder(ItemDeviceBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is HeaderViewHolder && item is SmartHomeListItem.Header) {
            holder.bind(item.room)
        } else if (holder is DeviceViewHolder && item is SmartHomeListItem.DeviceItem) {
            holder.bind(item.device)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemRoomHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: String) {
            binding.textViewRoomName.text = room
        }
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Device) {
            binding.textViewDeviceName.text = "${device.icon}  ${device.name}"
            binding.textViewDeviceType.text = device.domain.replaceFirstChar { it.uppercase() }
            binding.textViewDeviceStatus.text = device.stateDisplay

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
                onQuickToggle(device, checked)
            }

            binding.root.setOnClickListener {
                onItemClick(device)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SmartHomeListItem>() {
        override fun areItemsTheSame(oldItem: SmartHomeListItem, newItem: SmartHomeListItem): Boolean {
            return if (oldItem is SmartHomeListItem.Header && newItem is SmartHomeListItem.Header) {
                oldItem.room == newItem.room
            } else if (oldItem is SmartHomeListItem.DeviceItem && newItem is SmartHomeListItem.DeviceItem) {
                oldItem.device.entityId == newItem.device.entityId
            } else false
        }

        override fun areContentsTheSame(oldItem: SmartHomeListItem, newItem: SmartHomeListItem): Boolean {
            return oldItem == newItem
        }
    }
}
