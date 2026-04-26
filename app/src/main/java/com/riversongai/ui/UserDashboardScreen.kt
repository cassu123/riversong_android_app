package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.riversongai.databinding.FragmentUserDashboardBinding
import com.riversongai.ui.viewmodel.UserDashboardViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserDashboardScreen : Fragment() {

    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!

    private val userDashboardViewModel: UserDashboardViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userDashboardViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewDashboardWelcome.text = "Hello, ${it.firstName ?: it.username}!"
                binding.textViewUserRole.text = "Role: ${it.role.name.replace("_", " ")}"
            }
        }

        userDashboardViewModel.smartHomeSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewSmartHomeSummary.text =
                    "Smart Home: ${it.activeDevices} active / ${it.totalDevices} total devices"
            }
        }

        userDashboardViewModel.activitySummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewActivitySummary.text = if (it.stepsTaken > 0) {
                    "Activity: ${it.stepsTaken} steps, ${it.activeMinutes} active minutes today"
                } else {
                    "Activity: ${it.summary}"
                }
            }
        }

        userDashboardViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                userDashboardViewModel.clearError()
            }
        }

        userDashboardViewModel.loadDashboardData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
