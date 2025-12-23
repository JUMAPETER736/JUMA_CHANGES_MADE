package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
import com.uyscuti.social.circuit.adapter.SettingsAdapter
import com.uyscuti.social.circuit.model.SettingsModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private lateinit var username: String
    private lateinit var avatar: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = getSharedPreferences(PREFS_NAME, 0)
        username = settings.getString("username", "").toString()

        avatar = settings.getString("avatar", "").toString()

        val avatarPath = settings.getString("avatar", "")
        val avatarBitmap: Bitmap? = if (avatarPath!!.isNotEmpty()) {
            BitmapFactory.decodeFile(avatarPath)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.round_user)
        }



        // Create a Bitmap from a resource, file, or any other source.
        val lockBitmap: Bitmap? =  BitmapFactory.decodeResource(resources, R.drawable.google)
        val userBitmap: Bitmap? =  BitmapFactory.decodeResource(resources, R.drawable.round_user)

        toolbar = findViewById(R.id.toolbar)



        setSupportActionBar(toolbar)

        supportActionBar?.title = "Settings"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val settingsList = listOf<SettingsModel>(
            SettingsModel(avatarBitmap, "Username", "Some Text"),
            SettingsModel(lockBitmap, "Privacy", "Block contacts, Disappearing messages"),
            SettingsModel(lockBitmap, "Chats", "Theme, Wallpapers, Chat history"),
            SettingsModel(lockBitmap, "Notifications", "Message, group & call tones"),
            SettingsModel(lockBitmap, "Storage", "Network usage, auto-download"),
            SettingsModel(lockBitmap, "Help", "Help center, contact us, privacy policy"),
            SettingsModel(lockBitmap, "Invite", ""),
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(this, settingsList) {
            listItemClicked(it)
        }


    }

    private fun listItemClicked(selectedItem: SettingsModel) {
        when(selectedItem.title){
            "Username" -> {
                val intent = Intent(this@SettingsActivity, ViewImagesActivity::class.java)
                startActivity(intent)


            }"Privacy" ->{

                Toast.makeText(this, "privacy has been clicked", Toast.LENGTH_SHORT).show()
            }

            "Notifications"->{
                val intent =
                    Intent(this@SettingsActivity, NotificationsSettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}