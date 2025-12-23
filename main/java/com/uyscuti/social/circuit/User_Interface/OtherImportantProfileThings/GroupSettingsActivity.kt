package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityGroupSettingsBinding

class GroupSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        binding.toolbar.setNavigationIcon(R.drawable.back_svgrepo_com)

        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24)

        navigationIcon?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)

            val wrappedDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.black))
            val drawableMargin = InsetDrawable(wrappedDrawable, 0, 0, 0, 0)
            binding.toolbar.navigationContentDescription = "Navigate up"
            binding.toolbar.navigationIcon = drawableMargin
        }


        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}