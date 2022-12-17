package com.krish.jobspot.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import androidx.navigation.fragment.NavHostFragment
import com.krish.jobspot.R
import com.krish.jobspot.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.homeNavHostContainer) as NavHostFragment
        val navController = navHostFragment.navController
        val popMenu = PopupMenu(this, null)
        popMenu.inflate(R.menu.home_menu)
        val menu = popMenu.menu
        binding.bottomHomeNav.setupWithNavController(navController = navController, menu = menu)
    }
}