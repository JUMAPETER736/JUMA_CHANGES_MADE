package com.uyscuti.social.circuit.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.SettingsAdapter
import com.uyscuti.sharedmodule.media.ViewImagesActivity
import com.uyscuti.sharedmodule.model.SettingsModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"
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

        // Create Bitmaps for icons
        val lockBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google)
        val userBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.round_user)
        val blockBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_block
        val muteBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_mute
        val friendsBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_friends
        val favoriteBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_favorite
        val restrictBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_restrict
        val hideBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.google) // Change to ic_hide

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = "Settings"
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val settingsList = listOf(
            // User Profile
            SettingsModel(avatarBitmap, "Username", "Some Text"),

            // Main Settings
            SettingsModel(lockBitmap, "Privacy", "Block contacts, Disappearing messages"),
            SettingsModel(lockBitmap, "Chats", "Theme, Wallpapers, Chat history"),
            SettingsModel(lockBitmap, "Notifications", "Message, group & call tones"),
            SettingsModel(lockBitmap, "Storage", "Network usage, auto-download"),
            SettingsModel(lockBitmap, "Help", "Help center, contact us, privacy policy"),
            SettingsModel(lockBitmap, "Invite", ""),

            // ========== RELATIONSHIP MANAGEMENT SECTION ==========

            // Blocked Users
            SettingsModel(blockBitmap, "Blocked Users", "Manage blocked accounts"),

            // Muted Posts
            SettingsModel(muteBitmap, "Muted Posts", "Accounts whose posts you've muted"),

            // Muted Stories
            SettingsModel(muteBitmap, "Muted Stories", "Accounts whose stories you've muted"),

            // Close Friends
            SettingsModel(friendsBitmap, "Close Friends", "Manage your close friends list"),

            // Favorites
            SettingsModel(favoriteBitmap, "Favorites", "Accounts you've added to favorites"),

            // Restricted Accounts
            SettingsModel(restrictBitmap, "Restricted Accounts", "Manage restricted accounts"),

            // Hidden Posts
            SettingsModel(hideBitmap, "Hidden Posts", "Posts you've hidden from your feed")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(this, settingsList) {
            listItemClicked(it)
        }
    }

    private fun listItemClicked(selectedItem: SettingsModel) {
        when (selectedItem.title) {
            "Username" -> {
                val intent = Intent(this@SettingsActivity, ViewImagesActivity::class.java)
                startActivity(intent)
            }

            "Privacy" -> {
                Toast.makeText(this, "Privacy has been clicked", Toast.LENGTH_SHORT).show()
            }

            "Notifications" -> {
                val intent = Intent(this@SettingsActivity, NotificationsSettingsActivity::class.java)
                startActivity(intent)
            }

            // ========== RELATIONSHIP MANAGEMENT CLICKS ==========

            "Blocked Users" -> {
                val intent = Intent(this@SettingsActivity, BlockedUsersActivity::class.java)
                startActivity(intent)
            }

            "Muted Posts" -> {
                val intent = Intent(this@SettingsActivity, MutedPostsActivity::class.java)
                startActivity(intent)
            }

            "Muted Stories" -> {
                val intent = Intent(this@SettingsActivity, MutedStoriesActivity::class.java)
                startActivity(intent)
            }

            "Close Friends" -> {
                val intent = Intent(this@SettingsActivity, CloseFriendsActivity::class.java)
                startActivity(intent)
            }

            "Favorites" -> {
                val intent = Intent(this@SettingsActivity, FavoritesActivity::class.java)
                startActivity(intent)
            }

            "Restricted Accounts" -> {
                val intent = Intent(this@SettingsActivity, RestrictedAccountsActivity::class.java)
                startActivity(intent)
            }

            "Hidden Posts" -> {
                val intent = Intent(this@SettingsActivity, HiddenPostsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}