package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.riversongai.databinding.FragmentRegisterBinding
import com.riversongai.ui.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterScreen : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonRegister.isEnabled = !isLoading
        }

        registerViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(
                    context,
                    "Account created! An admin will approve your account before you can sign in.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            }.onFailure { exception ->
                Toast.makeText(context, exception.message ?: "Registration failed", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            val displayName = binding.editTextDisplayName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirm = binding.editTextConfirmPassword.text.toString()

            binding.layoutDisplayName.error = null
            binding.layoutEmail.error = null
            binding.layoutPassword.error = null
            binding.layoutConfirmPassword.error = null

            when {
                displayName.isEmpty() -> binding.layoutDisplayName.error = "Name is required"
                email.isEmpty() -> binding.layoutEmail.error = "Email is required"
                password.isEmpty() -> binding.layoutPassword.error = "Password is required"
                password.length < 12 -> binding.layoutPassword.error = "Password must be at least 12 characters"
                password != confirm -> binding.layoutConfirmPassword.error = "Passwords do not match"
                else -> registerViewModel.register(displayName, email, password)
            }
        }

        binding.textViewSignIn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
