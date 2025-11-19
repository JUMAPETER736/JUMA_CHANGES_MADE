package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityNotificationsSettingsBinding

class NotificationsSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationsSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_notifications_settings)
        binding = ActivityNotificationsSettingsBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(binding.root)

    }
}