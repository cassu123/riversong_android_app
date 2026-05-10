package com.riversongai.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.riversongai.R
import com.riversongai.databinding.FragmentUserDashboardBinding
import com.riversongai.ui.viewmodel.UserDashboardViewModel
import com.riversongai.utils.ThemeManager
import com.riversongai.utils.UIStyleManager
import com.riversongai.data.remote.RiverSongApiService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserDashboardScreen : Fragment() {

    private var _binding: FragmentUserDashboardBinding? = null
    private val binding get() = _binding!!
    private val userDashboardViewModel: UserDashboardViewModel by viewModel()
    private val apiService: RiverSongApiService by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUserDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.switchMVMode.isChecked = UIStyleManager.isMVModeEnabled(requireContext())
        binding.switchMVMode.setOnCheckedChangeListener { _, isChecked ->
            UIStyleManager.setMVMode(requireContext(), isChecked)
            requireActivity().recreate()
        }

        setupThemeGrid()
        setupListeners()
        observeViewModel()
        userDashboardViewModel.loadDashboardData()
    }

    // ── INTERFACE SKIN visual grid ──────────────────────────────────────────

    private fun setupThemeGrid() {
        val currentTheme = ThemeManager.getSelectedTheme(requireContext())
        APP_THEMES.chunked(2).forEach { pair ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).also { it.bottomMargin = 8.dp }
            }
            pair.forEachIndexed { idx, theme ->
                val card = buildThemeCard(theme, theme.key == currentTheme)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (idx == 0) params.marginEnd = 4.dp else params.marginStart = 4.dp
                card.layoutParams = params
                row.addView(card)
            }
            if (pair.size == 1) {
                row.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                })
            }
            binding.themeGrid.addView(row)
        }
    }

    private fun buildThemeCard(theme: ThemeOption, isSelected: Boolean): MaterialCardView {
        val ctx = requireContext()
        val primary = Color.parseColor(theme.primaryHex)
        val bg      = Color.parseColor(theme.bgHex)

        val card = MaterialCardView(ctx).apply {
            radius = 3f * ctx.resources.displayMetrics.density
            setCardBackgroundColor(bg)
            strokeWidth = if (isSelected) 2.dp else 1.dp
            strokeColor = if (isSelected) primary else Color.argb(60, 200, 200, 200)
            cardElevation = 0f
            isClickable = true
            isFocusable = true
            setOnClickListener {
                ThemeManager.setTheme(ctx, theme.key)
                viewLifecycleOwner.lifecycleScope.launch {
                    ThemeManager.saveThemeToServer(ctx, apiService, theme.key)
                }
                requireActivity().recreate()
            }
        }

        val inner = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
        card.addView(inner)

        // Preview area
        val preview = FrameLayout(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 72.dp
            )
            setBackgroundColor(bg)
        }
        preview.addView(View(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(10.dp, FrameLayout.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.argb(76, 0, 0, 0))
        })
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ).also { it.leftMargin = 18.dp }
            setPadding(0, 0, 8.dp, 0)
        }
        col.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(14.dp, 14.dp).also { it.bottomMargin = 8.dp }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL; setStroke(2.dp, primary); setColor(Color.TRANSPARENT)
            }
        })
        col.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(40.dp, 4.dp).also { it.bottomMargin = 4.dp }
            setBackgroundColor(primary)
        })
        col.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(60.dp, 4.dp)
            setBackgroundColor(primary); alpha = 0.5f
        })
        preview.addView(col)
        inner.addView(preview)

        inner.addView(TextView(ctx).apply {
            text = theme.label.uppercase()
            setTextColor(primary)
            textSize = 8f; letterSpacing = 0.14f; gravity = Gravity.CENTER
            setPadding(6.dp, 6.dp, 6.dp, if (isSelected) 2.dp else 6.dp)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
        })

        if (isSelected) {
            inner.addView(LinearLayout(ctx).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 6.dp }
                addView(View(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(6.dp, 6.dp)
                    background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(primary) }
                })
            })
        }
        return card
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density + 0.5f).toInt()

    // ── Profile + password ──────────────────────────────────────────────────

    private fun setupListeners() {
        binding.buttonSaveProfile.setOnClickListener {
            val first = binding.editTextFirstName.text.toString()
            val last  = binding.editTextLastName.text.toString()
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
            } else binding.layoutNewPassword.error = null

            if (newPass != confirm) {
                binding.layoutConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            } else binding.layoutConfirmPassword.error = null

            userDashboardViewModel.changePassword(current, newPass)
        }

        binding.buttonLogout.setOnClickListener { userDashboardViewModel.logout() }
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
                binding.editTextLastName.setText(
                    if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""
                )
                binding.chipRole.text = it.role.replaceFirstChar { c -> c.uppercase() }
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
                binding.textViewSmartHomeSummary.text = if (it.totalDevices == 0)
                    getString(R.string.profile_ha_not_connected)
                else
                    getString(R.string.profile_smart_home_summary, it.activeDevices, it.offlineDevices, it.totalDevices)
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
