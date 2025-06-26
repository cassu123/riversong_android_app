// app/src/main/java/com/riversongai/ui/UserDashboardScreen.kt
//
// This file defines the UserDashboardScreen Fragment, which serves as a personalized
// dashboard for a logged-in user within the River Song Android application.
// It displays user-specific information, summaries of activities, and provides
// quick access to relevant features based on the user's role and preferences.

package com.riversongai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.riversongai.R // Assumes R.layout.fragment_user_dashboard will exist
import com.riversongai.databinding.FragmentUserDashboardBinding // Assuming View Binding is enabled
import com.riversongai.ui.viewmodel.UserDashboardViewModel // Will need to create this ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel // Koin for ViewModel injection

// Section: UserDashboardScreen Fragment Definition
// This Fragment is responsible for presenting the user's personalized dashboard.
// It observes data from UserDashboardViewModel to display dynamic content.
class UserDashboardScreen : Fragment() {

    // Section: View Binding Setup
    // View Binding for type-safe access to layout views in fragment_user_dashboard.xml.
    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!

    // Section: ViewModel Injection
    // Injects the UserDashboardViewModel using Koin. This ViewModel fetches and
    // prepares data for display on the user dashboard.
    private val userDashboardViewModel: UserDashboardViewModel by viewModel()

    // Section: Fragment Lifecycle: onCreateView
    // Inflates the layout for the user dashboard screen and initializes View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        // You'll need to create 'fragment_user_dashboard.xml' in res/layout directory.
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        Log.d("UserDashboard", "UserDashboardScreen layout inflated.")
        return binding.root
    }

    // Section: Fragment Lifecycle: onViewCreated
    // Sets up UI elements, attaches observers to the ViewModel's LiveData,
    // and initiates the data loading process for the dashboard.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("UserDashboard", "UserDashboardScreen view created.")

        // Section: Observe User Profile Data
        // Observes the current user data from the ViewModel to update welcome message and profile info.
        userDashboardViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewDashboardWelcome.text = "Hello, ${it.firstName ?: it.username}!"
                binding.textViewUserRole.text = "Role: ${it.role.name.replace("_", " ")}"
                Log.d("UserDashboard", "User profile updated for: ${it.username}")
                // Update other UI elements with user details (e.g., profile picture, email)
            }
        }

        // Section: Observe Smart Home Summary
        // Observes summary data about smart home devices (e.g., number of active devices).
        userDashboardViewModel.smartHomeSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewSmartHomeSummary.text = "Smart Home: ${it.activeDevices} active devices"
                Log.d("UserDashboard", "Smart home summary updated: ${it.activeDevices} active devices.")
                // Update specific UI for smart home overview
            }
        }

        // Section: Observe Activity Tracking Summary (e.g., steps, active minutes)
        // Observes summaries related to activity recognition.
        userDashboardViewModel.activitySummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewActivitySummary.text = "Activity: ${it.stepsTaken} steps today."
                Log.d("UserDashboard", "Activity summary updated: ${it.stepsTaken} steps.")
                // Update UI for activity tracking
            }
        }

        // Section: Initial Data Load
        // Trigger the ViewModel to load the dashboard data when the view is first created.
        userDashboardViewModel.loadDashboardData("your_auth_token_here") // Replace with actual token management
    }

    // Section: Fragment Lifecycle: onDestroyView
    // Cleans up the View Binding instance to prevent memory leaks when the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("UserDashboard", "UserDashboardScreen view destroyed.")
    }
}