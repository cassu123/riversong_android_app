// app/src/main/java/com/riversongai/ui/HomeFragment.kt
//
// This file defines the HomeFragment, which serves as a primary user interface screen
// within the River Song Android application. It's responsible for displaying
// aggregated information (like user profile and smart home device status)
// and handling user interactions related to the home dashboard.

package com.riversongai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.riversongai.R // Assumes R.layout.fragment_home will exist
import com.riversongai.databinding.FragmentHomeBinding // Assuming View Binding is enabled
import com.riversongai.ui.viewmodel.HomeViewModel // Will need to create this ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel // Koin for ViewModel injection

// Section: HomeFragment Class Definition
// A Fragment is a reusable component that defines and manages its own layout,
// lifecycle, and behavior, often part of an Activity.
class HomeFragment : Fragment() {

    // Section: View Binding Setup
    // View Binding provides a type-safe way to interact with views in your layout XML.
    // This allows direct access to views without findViewById, improving performance and safety.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!! // Non-null accessor for the binding object

    // Section: ViewModel Injection
    // Injects the HomeViewModel using Koin. The ViewModel will provide data
    // to the UI and handle UI-related business logic.
    private val homeViewModel: HomeViewModel by viewModel()

    // Section: Fragment Lifecycle: onCreateView
    // Called to have the fragment instantiate its user interface view.
    // This is where you inflate your layout and get a reference to the binding object.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using View Binding.
        // R.layout.fragment_home refers to a layout file named 'fragment_home.xml'
        // that you will need to create in your res/layout directory.
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("HomeFragment", "HomeFragment layout inflated.")
        return binding.root
    }

    // Section: Fragment Lifecycle: onViewCreated
    // Called immediately after onCreateView() has returned, but before any saved state has been restored.
    // This is where you typically set up your views, attach observers to LiveData, and load data.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "HomeFragment view created.")

        // Section: Observe ViewModel Data
        // Observes changes in the user and device data provided by the HomeViewModel.
        // When the data changes, the UI will be updated automatically.
        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewWelcome.text = "Welcome, ${it.firstName ?: it.username}!" // Update welcome message
                Log.d("HomeFragment", "User data observed: ${it.username}")
                // Update other UI elements with user data
            }
        }

        homeViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                // Example: Update a RecyclerView or display a count of devices
                binding.textViewDeviceCount.text = "You have ${it.size} smart devices."
                Log.d("HomeFragment", "Devices data observed: ${it.size} devices.")
                // Update UI to show device list or summary
            }
        }

        // Section: Handle UI Interactions (Example)
        // Set up click listeners for buttons or other interactive elements.
        binding.buttonControlLight.setOnClickListener {
            // Example: Trigger a ViewModel method to control a light
            Log.d("HomeFragment", "Control Light button clicked.")
            homeViewModel.controlLightExample("light_id_123", true)
        }

        // Section: Initial Data Load
        // Trigger the ViewModel to load initial data when the view is ready.
        // This is important to display current user and device information on startup.
        homeViewModel.loadUserDataAndDevices("your_auth_token_here") // Replace with actual token management
    }

    // Section: Fragment Lifecycle: onDestroyView
    // Called when the view previously created by onCreateView() has been detached from the fragment.
    // This is crucial for cleaning up view-related resources, especially View Binding.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding reference to prevent memory leaks
        Log.d("HomeFragment", "HomeFragment view destroyed.")
    }
}