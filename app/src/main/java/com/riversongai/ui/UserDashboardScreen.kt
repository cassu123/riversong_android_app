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

import androidx.core.view.isVisible
import com.riversongai.utils.ThemeManager

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

        setupUI()
        setupListeners()
        observeViewModel()
        
        userDashboardViewModel.loadDashboardData()
    }

    private fun setupUI() {
        binding.textViewFactsCountLabel.text = getString(R.string.profile_facts_stat)
        binding.textViewRoutinesCountLabel.text = getString(R.string.profile_routines_stat)
        binding.textViewSmartHomeTitle.text = getString(R.string.profile_smart_home_title)
        binding.buttonLogout.text = getString(R.string.profile_sign_out)
        
        updateThemeSelectionUI()
    }

    private fun setupListeners() {
        binding.buttonSaveProfile.setOnClickListener {
            val first = binding.editTextFirstName.text.toString()
            val last = binding.editTextLastName.text.toString()
            val callsign = binding.editTextCallsign.text.toString().takeIf { it.isNotBlank() }
            userDashboardViewModel.updateProfile(first, last, callsign)
        }

        binding.buttonUpdatePassword.setOnClickListener {
            val current = binding.editTextCurrentPassword.text.toString()
            val newPass = binding.editTextNewPassword.text.toString()
            val confirm = binding.editTextConfirmPassword.text.toString()

            if (newPass.length < 8) {
                binding.layoutNewPassword.error = "Minimum 8 characters"
                return@setOnClickListener
            } else {
                binding.layoutNewPassword.error = null
            }

            if (newPass != confirm) {
                binding.layoutConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else {
                binding.layoutConfirmPassword.error = null
            }

            userDashboardViewModel.changePassword(current, newPass)
        }

        binding.cardThemeDefault.setOnClickListener { switchTheme(ThemeManager.THEME_DEFAULT) }
        binding.cardThemeDark.setOnClickListener { switchTheme(ThemeManager.THEME_DARK) }
        binding.cardThemeOcean.setOnClickListener { switchTheme(ThemeManager.THEME_OCEAN) }
        binding.cardThemeSunset.setOnClickListener { switchTheme(ThemeManager.THEME_SUNSET) }

        binding.buttonLogout.setOnClickListener {
            userDashboardViewModel.logout()
        }
    }

    private fun switchTheme(themeKey: String) {
        ThemeManager.setTheme(requireContext(), themeKey)
        updateThemeSelectionUI()
        // Re-apply to activity to see immediate effect for custom themes
        ThemeManager.applyThemeToActivity(requireActivity())
        // For some changes we might need to recreate
        if (themeKey == ThemeManager.THEME_OCEAN || themeKey == ThemeManager.THEME_SUNSET || 
            ThemeManager.getSelectedTheme(requireContext()) == ThemeManager.THEME_OCEAN ||
            ThemeManager.getSelectedTheme(requireContext()) == ThemeManager.THEME_SUNSET) {
            requireActivity().recreate()
        }
    }

    private fun updateThemeSelectionUI() {
        val selected = ThemeManager.getSelectedTheme(requireContext())
        binding.imageCheckDefault.isVisible = selected == ThemeManager.THEME_DEFAULT
        binding.imageCheckDark.isVisible = selected == ThemeManager.THEME_DARK
        binding.imageCheckOcean.isVisible = selected == ThemeManager.THEME_OCEAN
        binding.imageCheckSunset.isVisible = selected == ThemeManager.THEME_SUNSET
    }

    private fun observeViewModel() {
        userDashboardViewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired == true) navigateToLogin()
        }

        userDashboardViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.textViewDisplayName.text = it.displayName
                binding.textViewEmail.text = it.email
                binding.textViewAvatarInitial.text = it.displayName.take(1).uppercase()
                
                val parts = it.displayName.split(" ")
                binding.editTextFirstName.setText(parts.firstOrNull() ?: "")
                binding.editTextLastName.setText(if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else "")
                
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
                    getString(R.string.profile_ha_not_connected)
                } else {
                    getString(R.string.profile_smart_home_summary, it.activeDevices, it.offlineDevices, it.totalDevices)
                }
            }
        }

        userDashboardViewModel.profileUpdateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.textViewProfileStatus.isVisible = true
                binding.textViewProfileStatus.text = it
            }
        }

        userDashboardViewModel.passwordChangeResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.textViewPasswordStatus.isVisible = true
                binding.textViewPasswordStatus.text = it
                if (it.contains("successfully", ignoreCase = true)) {
                    binding.editTextCurrentPassword.text?.clear()
                    binding.editTextNewPassword.text?.clear()
                    binding.editTextConfirmPassword.text?.clear()
                }
            }
        }

        userDashboardViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                userDashboardViewModel.clearError()
            }
        }
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
