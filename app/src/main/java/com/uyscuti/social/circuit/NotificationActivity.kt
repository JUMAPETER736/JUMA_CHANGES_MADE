package com.uyscuti.social.circuit

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.uyscuti.social.circuit.databinding.ActivityNotificationBinding
import com.uyscuti.social.circuit.ui.fragments.chat.NotificationHostFragment
import com.uyscuti.social.circuit.ui.fragments.chat.NotificationsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationFragment = NotificationHostFragment.newInstance("","")
        callFragment(notificationFragment)

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun callFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

}