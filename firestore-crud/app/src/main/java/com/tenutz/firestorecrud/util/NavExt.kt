package com.tenutz.firestorecrud.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.tenutz.firestorecrud.R
import com.tenutz.firestorecrud.application.MainActivity

val MainActivity.navHostFragment get() = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
val MainActivity.navController get() = navHostFragment.navController
val Fragment.mainActivity get() =  (requireActivity() as MainActivity)
val MainActivity.currentFragment get() = supportFragmentManager.findFragmentById(R.id.container)
val MainActivity.currentDestinationId get() = currentFragment?.let { NavHostFragment.findNavController(it).currentDestination?.id }