package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
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
                binding.textViewDisplayName.text = it.displayName
                binding.textViewEmail.text = it.email
                binding.textViewAvatarInitial.text = it.displayName.take(1).uppercase()
                
                val roleLabel = it.role.replaceFirstChar { c -> c.uppercase() }
                binding.chipRole.text = roleLabel
            }
        }

        userDashboardViewModel.factsCount.observe(viewLifecycleOwner) { count ->
            binding.textViewFactsCount.text = count.toString()
        }

        userDashboardViewModel.routinesCount.observe(viewLifecycleOwner) { count ->
            binding.textViewRoutinesCount.text = count.toString()
        }

        userDashboardViewModel.smartHomeSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                binding.textViewSmartHomeSummary.text = if (it.totalDevices == 0) {
                    "Home Assistant not connected"
                } else {
                    "${it.activeDevices} on · ${it.offlineDevices} unavailable · ${it.totalDevices} total"
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
        findNavController().navigate(
            R.id.loginScreen, null,
            NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
