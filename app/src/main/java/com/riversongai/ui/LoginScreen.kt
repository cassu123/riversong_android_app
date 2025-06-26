// app/src/main/java/com/riversongai/ui/LoginScreen.kt
//
// This file defines the LoginScreen Fragment, which serves as the user interface
// for handling user authentication within the River Song Android application.
// It provides input fields for username/email and password, handles login attempts,
// and manages navigation based on authentication success or failure.

package com.riversongai.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController // For navigation
import com.riversongai.R // Assumes R.layout.fragment_login will exist
import com.riversongai.databinding.FragmentLoginBinding // Assuming View Binding is enabled
import com.riversongai.ui.viewmodel.LoginViewModel // Will need to create this ViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel // Koin for ViewModel injection

// Section: LoginScreen Fragment Definition
// This Fragment manages the user login interface, interacting with a ViewModel
// to perform authentication and update the UI accordingly.
class LoginScreen : Fragment() {

    // Section: View Binding Setup
    // View Binding for type-safe access to layout views.
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Section: ViewModel Injection
    // Injects the LoginViewModel using Koin. This ViewModel will handle the
    // business logic for user login and provide LiveData for UI updates.
    private val loginViewModel: LoginViewModel by viewModel()

    // Section: Fragment Lifecycle: onCreateView
    // Inflates the layout for the login screen and initializes View Binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        // You'll need to create 'fragment_login.xml' in res/layout directory.
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        Log.d("LoginScreen", "LoginScreen layout inflated.")
        return binding.root
    }

    // Section: Fragment Lifecycle: onViewCreated
    // Sets up UI elements, attaches observers to the ViewModel's LiveData,
    // and defines click listeners for login and registration actions.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("LoginScreen", "LoginScreen view created.")

        // Section: Observe Login State
        // Observes the loginResult LiveData from the ViewModel.
        // Updates UI based on loading, success, or error states.
        loginViewModel.loginResult.observe(viewLifecycleLifecycleOwner, Observer { result ->
            result.onSuccess { user ->
                // Login successful
                Toast.makeText(context, "Welcome, ${user.username}!", Toast.LENGTH_SHORT).show()
                Log.d("LoginScreen", "Login successful for user: ${user.username}")
                // Section: Navigation on Success
                // Navigate to the HomeFragment or main dashboard after successful login.
                // Assuming you have a navigation action defined in your navigation graph.
                findNavController().navigate(R.id.action_loginScreen_to_homeFragment) // Replace with your actual navigation action ID
            }.onFailure { exception ->
                // Login failed
                Toast.makeText(context, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("LoginScreen", "Login failed: ${exception.message}")
            }
        })

        // Section: Handle Login Button Click
        // Sets up the click listener for the login button.
        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                Log.d("LoginScreen", "Attempting login for user: $username")
                loginViewModel.login(username, password)
            } else {
                Toast.makeText(context, "Please enter username and password.", Toast.LENGTH_SHORT).show()
            }
        }

        // Section: Handle Register Button Click
        // Sets up the click listener for the register button, navigating to a registration screen.
        binding.buttonRegister.setOnClickListener {
            Log.d("LoginScreen", "Navigating to registration screen.")
            // Navigate to your registration fragment/activity.
            findNavController().navigate(R.id.action_loginScreen_to_registerScreen) // Replace with your actual navigation action ID
        }
    }

    // Section: Fragment Lifecycle: onDestroyView
    // Cleans up the View Binding instance to prevent memory leaks when the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("LoginScreen", "LoginScreen view destroyed.")
    }
}