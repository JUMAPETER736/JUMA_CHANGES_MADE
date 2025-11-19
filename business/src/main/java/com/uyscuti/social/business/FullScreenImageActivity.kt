package com.uyscuti.social.business

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.uyscuti.social.business.adapter.FullScreenPagerAdapter

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var fullscreenImageView: ImageView
    private lateinit var fullScreenPagerAdapter: FullScreenPagerAdapter

    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_screen_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imageUrls = intent.getStringArrayListExtra("imageUrls") ?: arrayListOf()
        val position = intent.getIntExtra("position", 0)

        fullScreenPagerAdapter = FullScreenPagerAdapter(imageUrls, this)

        val dotsIndicator: DotsIndicator = findViewById(R.id.worm_dots_indicator)

        if (imageUrls.size < 2) {
            dotsIndicator.visibility = View.GONE
        }

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = fullScreenPagerAdapter
        viewPager.offscreenPageLimit = 10
        viewPager.setCurrentItem(position, false)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    fullScreenPagerAdapter.playPlayer()
                } else {
                    fullScreenPagerAdapter.pausePlayer()
                }
            }
        })
        dotsIndicator.attachTo(viewPager)


        val imageUrl = intent.getStringExtra("imageUrl")

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)
        toolbar.navigationIcon?.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            PorterDuff.Mode.SRC_IN
        )

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        fullScreenPagerAdapter.releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        fullScreenPagerAdapter.pausePlayer()
    }
}