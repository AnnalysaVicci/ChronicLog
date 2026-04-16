package com.anna.chroniclog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.anna.chroniclog.databinding.ActivityMainBinding
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.anna.chroniclog.ui.OnboardingDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup bottom nav bar
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        initMenu()
        binding.bottomNavigationView.setupWithNavController(navController)

        // setup back button on toolbar
        setSupportActionBar(binding.toolbar)
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.healthInformationFragment,
                R.id.addLogFragment,
                R.id.medicationsFragment,
                R.id.logsFragment
            ),
        )
        setupActionBarWithNavController(navController, appBarConfig)

        val showOnboarding = intent.getBooleanExtra("SHOW_ONBOARDING", false)
        if (showOnboarding) {
            val onboardingDialog = OnboardingDialogFragment()
            onboardingDialog.show(supportFragmentManager, "OnboardingDialog")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /*
    fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    } */

    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        //  navigate to settings / logout dialog
                        navController.navigate(R.id.settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        })
    }

}