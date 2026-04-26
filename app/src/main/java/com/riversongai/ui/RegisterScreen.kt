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
                Toast.makeText(context, "Account created! Welcome.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerScreen_to_homeFragment)
            }.onFailure { exception ->
                Toast.makeText(context, "Registration failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonRegister.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirm = binding.editTextConfirmPassword.text.toString()
            val firstName = binding.editTextFirstName.text.toString().trim()
            val lastName = binding.editTextLastName.text.toString().trim()

            when {
                username.isEmpty() || email.isEmpty() || password.isEmpty() ->
                    Toast.makeText(context, "Username, email and password are required.", Toast.LENGTH_SHORT).show()
                password != confirm ->
                    Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                password.length < 8 ->
                    Toast.makeText(context, "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
                else ->
                    registerViewModel.register(username, email, password, firstName, lastName)
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
