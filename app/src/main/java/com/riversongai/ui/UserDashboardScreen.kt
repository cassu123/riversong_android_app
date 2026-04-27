package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.riversongai.R
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

        userDashboardViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) navigateToLogin()
        }

        userDashboardViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewDashboardWelcome.text = "Hello, ${it.firstName ?: it.username}!"
                binding.textViewUserRole.text = "Role: ${it.role.name.replace("_", " ")}"
            }
        }

        userDashboardViewModel.smartHomeSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewSmartHomeSummary.text =
                    "${it.activeDevices} active · ${it.offlineDevices} offline · ${it.totalDevices} total"
            }
        }

        userDashboardViewModel.activitySummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewActivitySummary.text = if (it.stepsTaken > 0) {
                    "${it.stepsTaken} steps · ${it.activeMinutes} active minutes today"
                } else {
                    it.summary
                }
            }
        }

        userDashboardViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                userDashboardViewModel.clearError()
            }
        }

        binding.buttonLogout.setOnClickListener {
            userDashboardViewModel.logout()
        }

        userDashboardViewModel.loadDashboardData()
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.loginScreen,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.main_nav_graph, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
