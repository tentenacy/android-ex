package com.tenutz.firestorecrud.application

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tenutz.firestorecrud.R
import com.tenutz.firestorecrud.data.User
import com.tenutz.firestorecrud.util.currentFragment
import com.tenutz.firestorecrud.util.navController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        navController.handleDeepLink(intent)
    }

    fun signOut() {
        Firebase.auth.signOut()
        User.clear()
        currentFragment?.findNavController()?.navigate(
            R.id.navigation_main,
            null,
            NavOptions.Builder().setPopUpTo(R.id.profileFragment, true).build()
        )
    }
}