package com.riversongai.ui.adapter

import com.riversongai.data.model.Device

sealed class SmartHomeListItem {
    data class Header(val room: String) : SmartHomeListItem()
    data class DeviceItem(val device: Device) : SmartHomeListItem()
}
