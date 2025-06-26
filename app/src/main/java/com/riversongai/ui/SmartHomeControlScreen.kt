// app/src/main/java/com/riversongai/ui/SmartHomeControlScreen.kt
//
// This file defines the SmartHomeControlScreen Fragment, which provides the user interface
// for viewing and interacting with smart home devices connected to the River Song AI system.
// It displays a list of devices, their current status, and enables users to send control commands.

package com.riversongai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.riversongai.R // Assumes R.layout.fragment_smart_home_control will exist
import com.riversongai.data.model.Device
import com.riversongai.databinding.FragmentSmartHomeControlBinding // Assuming View Binding is enabled
import com.riversongai.ui.adapter.DeviceAdapter // Will need to create this Adapter
import com.riversongai.ui.viewmodel.SmartHomeControlViewModel // Will need to create this ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel // Koin for ViewModel injection

// Section: SmartHomeControlScreen Fragment Definition
// This Fragment displays a list of smart home devices and allows users to interact with them.
// It fetches device data and sends control commands via a ViewModel.
class SmartHomeControlScreen : Fragment() {

    // Section: View Binding Setup
    // View Binding for type-safe access to layout views in fragment_smart_home_control.xml.
    private var _binding: FragmentSmartHomeControlBinding? = null
    private val binding get() = _binding!!

    // Section: ViewModel Injection
    // Injects the SmartHomeControlViewModel using Koin. This ViewModel will handle
    // the business logic for fetching devices and sending control commands.
    private val smartHomeControlViewModel: SmartHomeControlViewModel by viewModel()

    // Section: RecyclerView Adapter
    // Adapter for displaying the list of devices in a RecyclerView.
    private lateinit var deviceAdapter: DeviceAdapter

    // Section: Fragment Lifecycle: onCreateView
    // Inflates the layout for the smart home control screen and initializes View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        // You'll need to create 'fragment_smart_home_control.xml' in res/layout directory.
        _binding = FragmentSmartHomeControlBinding.inflate(inflater, container, false)
        Log.d("SmartHomeControl", "SmartHomeControlScreen layout inflated.")
        return binding.root
    }

    // Section: Fragment Lifecycle: onViewCreated
    // Sets up the RecyclerView, attaches observers to the ViewModel's LiveData,
    // and initiates the data loading process.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SmartHomeControl", "SmartHomeControlScreen view created.")

        // Section: Setup RecyclerView
        // Initializes the RecyclerView with a LinearLayoutManager and the DeviceAdapter.
        deviceAdapter = DeviceAdapter { device, command, value ->
            // Lambda function to handle device control actions from the adapter.
            // This will be called when a user interacts with a device item in the list.
            Log.d("SmartHomeControl", "Sending command '$command' with value '$value' to device: ${device.name}")
            // Trigger the ViewModel to send the control command.
            smartHomeControlViewModel.controlDevice(device.id, command, value)
        }
        binding.recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deviceAdapter
        }

        // Section: Observe ViewModel Data
        // Observes the list of devices provided by the ViewModel.
        // When the list updates, submit it to the adapter to refresh the UI.
        smartHomeControlViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                deviceAdapter.submitList(it) // Update the RecyclerView
                binding.progressBar.visibility = View.GONE // Hide loading indicator
                binding.textViewStatus.text = "Loaded ${it.size} devices."
                Log.d("SmartHomeControl", "Devices list updated. Total: ${it.size}")
            }
        }

        // Section: Observe Loading State
        // Observes the loading status from the ViewModel to show/hide a progress bar.
        smartHomeControlViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.textViewStatus.text = if (isLoading) "Loading devices..." else binding.textViewStatus.text
            Log.d("SmartHomeControl", "Loading state: $isLoading")
        }

        // Section: Observe Error State
        // Observes any error messages from the ViewModel and displays them as a Toast.
        smartHomeControlViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                binding.textViewStatus.text = "Error: $it"
                Log.e("SmartHomeControl", "Error observed: $it")
                smartHomeControlViewModel.errorMessage.value = null // Consume the error message
            }
        }

        // Section: Initial Data Load
        // Initiates fetching the device list when the screen is first created.
        smartHomeControlViewModel.fetchDevices("your_auth_token_here") // Replace with actual token management
    }

    // Section: Fragment Lifecycle: onDestroyView
    // Cleans up the View Binding instance to prevent memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("SmartHomeControl", "SmartHomeControlScreen view destroyed.")
    }
}