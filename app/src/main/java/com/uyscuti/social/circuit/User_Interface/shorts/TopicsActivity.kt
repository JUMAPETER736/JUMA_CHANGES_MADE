package com.uyscuti.social.circuit.User_Interface.shorts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.AddShortsTopicAdapter
import com.uyscuti.social.circuit.databinding.ActivityTopicsBinding
import com.uyscuti.social.circuit.model.AddShortsTopicModel

class TopicsActivity : AppCompatActivity() {

    private lateinit var topicsBinding: ActivityTopicsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicsBinding = ActivityTopicsBinding.inflate(layoutInflater)
        setContentView(topicsBinding.root)

        setSupportActionBar(topicsBinding.toolbar)
        supportActionBar?.title = ""

        topicsBinding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24_black)
        topicsBinding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val topicList = listOf<AddShortsTopicModel>(
            AddShortsTopicModel(128131,
                "Fashion & Beauty", listOf(
                    "Styles & Trends",
                    "Clothing & Accessories",
                    "Accessories",
                    "Hair Care",
                    "Skin Care",
                    "Shirts & Tops",
                    "Makeup",
                    "Hairstyles",
                    "Footwear",
                    "Accessories",
                    "Jewelry",
                    "Nail Care",
                    "Body Art", "Costumes","Free Care Products","Pants & Bottoms","Outerwear", "Activewear","Swimwear","Hats"
                )
            ),
            AddShortsTopicModel(129409,
                "Animals", listOf(
                    "Cute Animals", "Dogs", "Funny Animals", "Pet Care", "Animal-Human Bonding",
                    "Cats", "Horses","Animal Encounters", "Goats", "Cattle","Wildlife",
                    "Aquatic", "Cows & Bulls", "Lion", "Elephant", "Birds",
                    "Bugs & Worms", "Aquatic", "Aquariums & Zoos",
                    "Primates", "Farm Animals", "Rabbits", "Reptiles"

                    )
            ),
//
            AddShortsTopicModel(
                128665,
                "Transportation", listOf(
                    "Car Culture", "Cars & Trucks", "Motorcycles","Heavy Machinery",
                    "SUVs","Motorsports","Sports Cars","Bikes","Repairs & Maintenance",
                    "Automotive Tech", "Car Racing", "Vintage & Classic"
                )
            ),
            AddShortsTopicModel(
                127828,
                "Food & Drink", listOf(
                    "Recipes", "Alcoholic Beverages", "Chocolate","Vegetables",
                    "Seafood","BBQ & Grilling","Desserts","Snacks","Breads",
                    "Street Food", "Kitchen Accessories", "Ice Cream",
                    "Pizza","Veganism","Baking","Coffee","Meat","Dairy","Beer","Wine","Cheese","Cocktails","Breakfast & Brunch",
                    "Juices & Smoothies","Rice, Grains & Noodles","Cakes","Restaurants","Fruits","Cookies",
                    "Cupcakes","Wine","Cheese","Gin"
                )
            ),

            AddShortsTopicModel(
                127912,
                "Visual Arts", listOf(
                    "Visual Arts", "Digital Art", "Textile Arts","Photography",
                    "Painting","Drawing"
                )
            ),

            AddShortsTopicModel(
                128640,
                "Travel", listOf(
                    "Destinations", "Landmarks & Destinations", "Tourist Attractions","Outdoors",
                    "Vacation Activities","Water Activities", "Beaches","Hotels & Lodging","Amusement Parks","Jungle & Rainforest",
                )
            ),

            AddShortsTopicModel(
                9917,
                "Sports", listOf(
                    "Football", "Strength Training", "Gym Workouts","Soccer",
                    "Cricket","Cycling","Tips","BodyWeight Workouts","Basketball","Water Sports"
                    ,"Weightlifting","Home Workouts & Fitness Activities","Water Sports","Running",
                    "Yoga","Wrestling","Boxing","Baseball","Winter Sports","Volleyball","Golf","Martial Arts"
                )
            ),
            AddShortsTopicModel(
                129409,
                "Science & Tech", listOf(
                    "Technology", "Coding", "Phones & Accessories",
                    "Audio Electronics", "Consumer Tech", "Disciplines"
                )
            ),

            AddShortsTopicModel(
                128150,
                "Relationships", listOf(
                    "Friendships", "Birthdays", "Friendship Humor",
                    "Relationship to Self"
                )
            ),

            AddShortsTopicModel(
                1042441,
                "Performing Arts", listOf(
                    "Dance", "Lip Syncing", "Performances",
                    "Instruments", "Singers", "Comedy"
                )
            ),

            AddShortsTopicModel(
                127918,
                "Games", listOf(
                    "Video Games", "Toys", "Fifa",
                )
            ),

            AddShortsTopicModel(
                127871,
                "TV & Movies", listOf(
                    "Celebrities", "Animation", "Comedy",
                    "Hollywood", "Bollywood", "Anime", "Malawian", "American",
                    "Romance", "Fantasy"
                )
            ),

            AddShortsTopicModel(
                127969,
                "Home & Garden", listOf(
                    "Gardening & Landscaping", "Plants & Trees", "Construction & Renovation",
                    "Urban Farming", "Home & Garden Hacks", "Home Furnishings", "Interior Design"
                )
            ),

            AddShortsTopicModel(
                128202,
                "Business", listOf(
                    "Small Businesses", "Entrepreneurship", "Career Development",
                    "Commerce", "Financial Markets"
                )
            ),

            AddShortsTopicModel(
                127891,
                "Education", listOf(
                    "Teaching Methods"
                )
            ),

            )

        topicsBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = AddShortsTopicAdapter(this, topicList)
        topicsBinding.recyclerView.adapter = adapter

        topicsBinding.textViewDone.setOnClickListener {
            val selectedSubtopics = adapter.getSelectedSubtopics().toList()
            Log.d("selectedSubtopics", selectedSubtopics.toString())
            // In TopicsActivity where you want to finish and return a result
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("selectedSubtopics", ArrayList(selectedSubtopics)) // Optional: Include any data you want to pass back

            setResult(Activity.RESULT_OK, resultIntent)
            finish()

//            val intent = Intent(this, UploadShortsActivity::class.java)
//            intent.putStringArrayListExtra("selectedSubtopics", ArrayList(selectedSubtopics))
//            finish()
//            startActivity(intent)

        }
    }
}