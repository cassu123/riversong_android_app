package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.riversongai.R
import com.riversongai.databinding.FragmentHomeBinding
import com.riversongai.ui.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewDate.text = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

        homeViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) {
                findNavController().navigate(R.id.loginScreen, null,
                    NavOptions.Builder().setPopUpTo(R.id.main_nav_graph, true).build())
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewWelcome.text = "Welcome, ${it.firstName ?: it.username}!"
            }
        }

        homeViewModel.devices.observe(viewLifecycleOwner) { devices ->
            devices?.let {
                val activeCount = devices.count { d -> d.isOn == true || d.status == "online" }
                binding.textViewDeviceCount.text = "${it.size} devices • $activeCount active"
            }
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                binding.textViewError.isVisible = true
                binding.textViewError.text = it
                homeViewModel.clearError()
            }
        }

        binding.buttonControlLight.setOnClickListener {
            homeViewModel.controlLightExample("light_id_123", true)
            Toast.makeText(context, "Light toggled.", Toast.LENGTH_SHORT).show()
        }

        binding.buttonViewDevices.setOnClickListener {
            findNavController().navigate(R.id.smartHomeControlScreen)
        }

        homeViewModel.loadUserDataAndDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
