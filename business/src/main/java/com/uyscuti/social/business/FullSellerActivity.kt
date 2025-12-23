package com.uyscuti.social.business

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.uyscuti.social.business.adapter.ProfileViewAdapter
import com.uyscuti.social.business.adapter.SellerFullAdapter
import com.uyscuti.social.business.model.Catalogue

//import com.example.business.adapter.ProfileViewAdapter
//import com.example.business.adapter.SellerFullAdapter
//import com.example.business.model.Catalogue

class FullSellerActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var sellerFullAdapter: SellerFullAdapter
    private lateinit var profileViewAdapter: ProfileViewAdapter
    private var isHeartFilled = false
    private lateinit var viewName: TextView



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_seller)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val catalogue: Catalogue? = intent.getSerializableExtra("catalogue") as Catalogue?
        val username: String? = intent.getStringExtra("username")
        val profileImage: String? = intent.getStringExtra("userAvatar")

        if (catalogue != null) {
            recyclerView = findViewById(R.id.image_recycler_edit)

            sellerFullAdapter = SellerFullAdapter(this, catalogue)

            recyclerView.layoutManager = LinearLayoutManager(
                this@FullSellerActivity,
                LinearLayoutManager.VERTICAL,
                false
            )

            recyclerView.adapter = sellerFullAdapter

            if(username != null && profileImage != null){
                sellerFullAdapter.setNameAndAvatar(username, profileImage)

            }
        }
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.back_button)
        setSupportActionBar(toolbar)
        toolbar.title = "Business Catalogue Details"
        toolbar.setNavigationOnClickListener {
            finish()
        }


        // Inflate and add the share icon
        val shareIconView = LayoutInflater.from(this).inflate(R.layout.share_icon_layou, toolbar, false) as LinearLayout
        val shareIcon = shareIconView.findViewById<ImageView>(R.id.shareIcon)
        shareIcon.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing business catalogue!")
            startActivity(Intent.createChooser(shareIntent, "Share Catalogue"))
        }
        toolbar.addView(shareIconView)

        // Inflate and add the heart icon
        val heartIconView = LayoutInflater.from(this).inflate(R.layout.heart_icon_layout, toolbar, false) as LinearLayout
        val heartIcon = heartIconView.findViewById<ImageView>(R.id.heartIcon)
        heartIcon.setOnClickListener {

            if (isHeartFilled){
                heartIcon.setColorFilter(resources.getColor(R.color.blueJeans))
                applyBounceAnimation(heartIcon)
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()

            }else {
                heartIcon.setColorFilter(resources.getColor(R.color.red))

            }
            isHeartFilled = !isHeartFilled

        }
        toolbar.addView(heartIconView)

    }
    private fun applyBounceAnimation(view: ImageView) {
        // Define the bounce animation
        val bounceAnim = ObjectAnimator.ofFloat(view, "translationY", 0f, -50f, 0f)
        bounceAnim.duration = 200 // Duration of the animation in milliseconds

        // Start the animation
        bounceAnim.start()
    }

}
