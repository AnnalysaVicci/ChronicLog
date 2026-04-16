package com.anna.chroniclog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.anna.chroniclog.databinding.ActivityMainBinding
import com.anna.chroniclog.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set up layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setup toolbar
        setSupportActionBar(binding.toolbar)

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
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
    }

    /*
    // called when settings cog is clicked
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                //  navigate to settings / logout dialog
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

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