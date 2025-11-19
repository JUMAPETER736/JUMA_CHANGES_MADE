package com.uyscuti.social.business.retro

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.uyscuti.social.business.R

class FullScreenActivity : AppCompatActivity() {

    private lateinit var fullscreenImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.catalogue_image_view)



        var imageView = findViewById<ImageView>(R.id.view_catalogue_image)

        val imageResource = intent.getIntExtra("imageResource", -1)
        if (imageResource != -1) {
            fullscreenImageView.setImageResource(imageResource)
        }
        imageView.setOnClickListener {
//            val intent = Intent(this, FullscreenImageActivity::class.java)


        }
        startActivity(intent)

    }
}
