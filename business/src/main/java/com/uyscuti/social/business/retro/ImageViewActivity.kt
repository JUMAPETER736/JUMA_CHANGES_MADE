package com.uyscuti.social.business.retro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.uyscuti.social.business.R
import com.uyscuti.social.business.adapter.ViewAdapter
import com.uyscuti.social.business.adapter.ViewProductAdapter
import com.uyscuti.social.business.model.Catalogue


class ImageViewActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ViewAdapter
    private lateinit var nameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var priceTextView: TextView
    private lateinit var editButton: AppCompatButton
    private lateinit var imageView: AppCompatImageView

    private lateinit var viewPager: ViewPager2
    private lateinit var catalogue: Catalogue

    private lateinit var viewProductAdapter: ViewProductAdapter


    private lateinit var toolbar: Toolbar

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_two)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "View Product"
        toolbar.setNavigationIcon(R.drawable.baseline_chevron_left_24)


        toolbar.setNavigationOnClickListener {
            finish()
        }

//        viewPager = findViewById(R.id.viewPager)

        recyclerView = findViewById(R.id.recycler_view)

//        val springDotsIndicator = findViewById<WormDotsIndicator>(R.id.worm_dots_indicator)
//
//        val editButton = findViewById<AppCompatButton>(R.id.edit_button)
//        val nameTextView = findViewById<TextView>(R.id.view_name_product)
//        val descriptionTextView = findViewById<TextView>(R.id.view_description_product)
//        val priceTextView = findViewById<TextView>(R.id.view_price_product)
//        val imageView = findViewById<AppCompatImageView>(R.id.productImageView)

        // Retrieve the catalogue data from the intent
        val catalogue: Catalogue? = intent.getSerializableExtra("catalogue") as Catalogue?

        // Check if the catalogue data is not null
        if (catalogue != null) {
            // Handle the catalogue data

            viewProductAdapter = ViewProductAdapter(this,catalogue)

            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = viewProductAdapter

//            val catalogueName = catalogue.name
//            val catalogueDescription = catalogue.description
//            val cataloguePrice = catalogue.price
////            val catalogueImages = catalogue.images
//
//
//
//            val fullScreenPagerAdapter = MediaPagerAdapter(catalogue.images, this)
//
//            viewPager.adapter = fullScreenPagerAdapter
//            viewPager.offscreenPageLimit = 10
//
////            viewPager.setCurrentItem(0, false)
//
////            springDotsIndicator.setViewPager2(viewPager)
//
//
//            nameTextView.text = catalogueName
//            descriptionTextView.text = catalogueDescription
//            priceTextView.text = cataloguePrice
//
//            // Handle the edit button click
//            editButton.setOnClickListener {
//
//            }

            // Now you can use the catalogue data as needed
            // For example, set text to TextViews, load images, etc.
        } else {

            // Handle the case where catalogue data is null
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(
            "UpdatedCatalogue",
            "activity onActivityResult: $data  code: $requestCode result: $resultCode"
        )


        if (resultCode == RESULT_OK && requestCode == 123){
            try {

                // Check if the data Intent is not null and contains the expected key
                data?.getSerializableExtra("updated_catalogue")?.let { catalogue ->
                    // Find the existing instance of the fragment by its tag

                    Log.d("UpdatedCatalogue", "catalogue :$catalogue")

                    this.catalogue = catalogue as Catalogue

                    viewProductAdapter.updateCatalogue(catalogue)


                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewProductAdapter.releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        viewProductAdapter.pausePlayer()
    }

}