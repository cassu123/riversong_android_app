package com.riversongai.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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
    private lateinit var appBarConfig: AppBarConfiguration
    private val sessionManager: SessionManager by inject()

    private val mainDestinations = setOf(
        R.id.homeFragment,
        R.id.speakFragment,
        R.id.chatFragment,
        R.id.feedsFragment,
        R.id.memoryFragment,
        R.id.routinesFragment,
        R.id.settingsFragment,
        R.id.userDashboardScreen,
        // Placeholder screens (all shown with toolbar + bottom nav)
        R.id.inventoryFragment,
        R.id.maintenanceFragment,
        R.id.storeFragment,
        R.id.homeNodeFragment,
        R.id.analyticsFragment,
        R.id.googleFragment,
        R.id.readingFragment,
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

        appBarConfig = AppBarConfiguration(mainDestinations, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfig)
        binding.navigationView.setupWithNavController(navController)

        setupBottomNav()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isMainDest = destination.id in mainDestinations
            binding.appBarLayout.isVisible = isMainDest
            binding.customBottomNav.isVisible = isMainDest
            updateNavActiveState(destination.id)

            val lockMode = if (isMainDest)
                androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
            else
                androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            binding.drawerLayout.setDrawerLockMode(lockMode)
        }

        if (sessionManager.isLoggedIn() &&
            navController.currentDestination?.id == R.id.loginScreen) {
            navController.navigate(R.id.action_loginScreen_to_homeFragment)
        }

        configureNavForRole()
        requestRuntimePermissions()
    }

    private fun configureNavForRole() {
        val isAdmin = sessionManager.isAdmin()
        val drawerMenu = binding.navigationView.menu
        
        // These items only visible to admins
        drawerMenu.findItem(R.id.routinesFragment)?.isVisible = isAdmin
        drawerMenu.findItem(R.id.homeNodeFragment)?.isVisible = isAdmin
        
        // Store isAdmin in rs_prefs for fragments to read
        getSharedPreferences("rs_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("is_admin", isAdmin).apply()
    }

    private fun setupBottomNav() {
        val navOptions = NavOptions.Builder().setLaunchSingleTop(true).build()

        binding.navItemChat.setOnClickListener {
            if (navController.currentDestination?.id != R.id.chatFragment)
                navController.navigate(R.id.chatFragment, null, navOptions)
        }
        binding.navItemSpeak.setOnClickListener {
            if (navController.currentDestination?.id != R.id.speakFragment)
                navController.navigate(R.id.speakFragment, null, navOptions)
        }
        binding.navItemFeed.setOnClickListener {
            if (navController.currentDestination?.id != R.id.feedsFragment)
                navController.navigate(R.id.feedsFragment, null, navOptions)
        }
    }

    private fun updateNavActiveState(destinationId: Int) {
        val primary          = resolveThemeColor(com.google.android.material.R.attr.colorPrimary)
        val onSurfaceVariant = resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
        val primaryContainer = resolveThemeColor(com.google.android.material.R.attr.colorPrimaryContainer)
        val onPrimaryContainer = resolveThemeColor(com.google.android.material.R.attr.colorOnPrimaryContainer)
        val onPrimary        = resolveThemeColor(com.google.android.material.R.attr.colorOnPrimary)

        binding.navIconChat.imageTintList = ColorStateList.valueOf(onSurfaceVariant)
        binding.navLabelChat.setTextColor(onSurfaceVariant)
        binding.navSpeakCard.setCardBackgroundColor(primaryContainer)
        binding.navIconSpeak.imageTintList = ColorStateList.valueOf(onPrimaryContainer)
        binding.navLabelSpeak.setTextColor(onSurfaceVariant)
        binding.navIconFeed.imageTintList = ColorStateList.valueOf(onSurfaceVariant)
        binding.navLabelFeed.setTextColor(onSurfaceVariant)

        when (destinationId) {
            R.id.chatFragment -> {
                binding.navIconChat.imageTintList = ColorStateList.valueOf(primary)
                binding.navLabelChat.setTextColor(primary)
            }
            R.id.speakFragment -> {
                binding.navSpeakCard.setCardBackgroundColor(primary)
                binding.navIconSpeak.imageTintList = ColorStateList.valueOf(onPrimary)
                binding.navLabelSpeak.setTextColor(primary)
            }
            R.id.feedsFragment -> {
                binding.navIconFeed.imageTintList = ColorStateList.valueOf(primary)
                binding.navLabelFeed.setTextColor(primary)
            }
        }
    }

    private fun resolveThemeColor(@AttrRes attr: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attr, tv, true)
        return tv.data
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp()
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
