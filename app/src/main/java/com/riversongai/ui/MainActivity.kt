// app/src/main/java/com/riversongai/ui/MainActivity.kt
//
// This file defines the MainActivity, which is the main entry point and host Activity
// for the River Song Android application. It sets up the primary user interface,
// typically containing a Navigation Host Fragment to manage the navigation
// between different screens (Fragments) like LoginScreen and HomeFragment.

package com.riversongai.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.riversongai.R // Assumes R.layout.activity_main and R.id.nav_host_fragment will exist
import com.riversongai.databinding.ActivityMainBinding // Assuming View Binding is enabled

// Section: Constants
// Request code for handling runtime permissions.
private const val REQUEST_PERMISSIONS_CODE = 101

// Section: MainActivity Class Definition
// MainActivity extends AppCompatActivity to ensure compatibility across different Android versions.
// It serves as the main container for the app's UI, typically hosting the Navigation Component.
class MainActivity : AppCompatActivity() {

    // Section: View Binding Setup
    // View Binding for type-safe access to layout views in activity_main.xml.
    private lateinit var binding: ActivityMainBinding

    // Section: Navigation Controller
    // The NavController manages app navigation, allowing transitions between destinations (Fragments).
    private lateinit var navController: NavController

    // Section: Activity Lifecycle: onCreate
    // Called when the activity is first created. This is where you initialize the UI,
    // set up navigation, and request necessary runtime permissions.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "MainActivity created.")

        // Initialize View Binding for the activity's layout.
        // You'll need to create 'activity_main.xml' in res/layout directory.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Section: Navigation Component Setup
        // Find the NavHostFragment, which is the container for navigation destinations.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Get the NavController associated with the NavHostFragment.
        navController = navHostFragment.navController

        // Optional: Set up the ActionBar/Toolbar with the NavController
        // This will automatically update the title and handle the Up button.
        // If you don't use a standard ActionBar, you can remove this.
        setupActionBarWithNavController(navController)

        // Section: Request Runtime Permissions
        // Checks and requests necessary runtime permissions declared in AndroidManifest.xml.
        // This is crucial for functionalities like camera, microphone, and location.
        requestRuntimePermissions()

        Log.d("MainActivity", "Navigation and permissions setup complete.")
    }

    // Section: Handle Up Navigation
    // Called when the user chooses to navigate Up within your application's task.
    // This is typically triggered by the Up button in the ActionBar.
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // Section: Runtime Permissions Request
    // This private function checks for and requests permissions required by the app.
    private fun requestRuntimePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Add permissions from your AndroidManifest.xml that require runtime approval
        // For Android 6.0 (API 23) and above, certain permissions need explicit user approval at runtime.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        // Add other dangerous permissions declared in Manifest if needed (e.g., READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_CODE
            )
            Log.d("MainActivity", "Requesting runtime permissions: ${permissionsToRequest.joinToString()}")
        } else {
            Log.d("MainActivity", "All required runtime permissions already granted.")
        }
    }

    // Section: Permissions Request Result Callback
    // This method is called after the user responds to a permission request dialog.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "$permission granted.")
                } else {
                    Log.w("MainActivity", "$permission denied.")
                    // Optional: Show a Toast or dialog explaining why the permission is needed.
                    // Toast.makeText(this, "$permission is required for full functionality.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}