package com.uyscuti.sharedmodule.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uyscuti.sharedmodule.databinding.ActivitySearchShortBinding
import com.uyscuti.sharedmodule.R


class SearchShortActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchShortBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchShortBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}