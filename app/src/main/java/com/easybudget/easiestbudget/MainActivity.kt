package com.easybudget.easiestbudget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.easybudget.easiestbudget.databinding.ActivityMainHostBinding

/**
 * The main container activity for the EasiestBudget application.
 * This activity hosts the NavHostFragment which handles all fragment transactions.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding for accessing activity layout components
    private lateinit var binding: ActivityMainHostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = ActivityMainHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the NavController from the NavHostFragment to manage navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Optional: setupActionBarWithNavController(navController)
    }

    /**
     * Handles the "Up" button navigation in the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}