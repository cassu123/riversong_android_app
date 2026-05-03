package com.riversongai.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.riversongai.R
import com.riversongai.databinding.ActivityMainBinding
import com.riversongai.utils.SessionManager
import org.koin.android.ext.android.inject

private const val REQUEST_PERMISSIONS_CODE = 101

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val sessionManager: SessionManager by inject()

    private val mainDestinations = setOf(
        R.id.homeFragment,
        R.id.speakFragment,
        R.id.chatFragment,
        R.id.smartHomeControlScreen,
        R.id.feedsFragment,
        R.id.memoryFragment,
        R.id.routinesFragment,
        R.id.settingsFragment,
        R.id.userDashboardScreen
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        com.riversongai.utils.ThemeManager.applyThemeToActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(mainDestinations, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.navigationView.setupWithNavController(navController)

        // Show/hide bottom nav and toolbar based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isMainDest = destination.id in mainDestinations
            // We use drawer for regular navigation on mobile as per design
            binding.bottomNavigationView.isVisible = false 
            binding.appBarLayout.isVisible = isMainDest
            
            if (!isMainDest) {
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        // Only navigate if we are actually on the login screen — otherwise the
        // action doesn't exist on the current destination and will crash.
        if (sessionManager.isLoggedIn() &&
            navController.currentDestination?.id == R.id.loginScreen) {
            navController.navigate(R.id.action_loginScreen_to_homeFragment)
        }

        requestRuntimePermissions()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun requestRuntimePermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQUEST_PERMISSIONS_CODE)
        }
    }
}
