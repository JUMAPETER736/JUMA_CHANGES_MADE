package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivitySearchShortBinding

class SearchShortActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchShortBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchShortBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding.root or binding.main instead of findViewById
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}