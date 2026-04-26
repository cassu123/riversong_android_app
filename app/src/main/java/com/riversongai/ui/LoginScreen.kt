package com.riversongai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.riversongai.R
import com.riversongai.databinding.FragmentLoginBinding
import com.riversongai.ui.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginScreen : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonLogin.isEnabled = !isLoading
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { auth ->
                Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginScreen_to_homeFragment)
            }.onFailure { exception ->
                Toast.makeText(context, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please enter username and password.", Toast.LENGTH_SHORT).show()
            } else {
                loginViewModel.login(username, password)
            }
        }

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_registerScreen)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
