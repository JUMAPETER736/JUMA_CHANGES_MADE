package com.uyscuti.social.circuit.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.InsetDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.uyscuti.social.business.model.User
import com.uyscuti.social.circuit.adapter.UserProfileTabsAdapter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityUserProfileBinding
import com.uyscuti.social.circuit.ui.media.ViewImagesActivity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.IOException
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app
    private lateinit var username: String
    private lateinit var avatar: String
    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        binding.toolbar.setNavigationIcon(R.drawable.back_svgrepo_com)
        supportActionBar?.title = "Profile"

        settings = getSharedPreferences(PREFS_NAME, 0)
        username = settings.getString("username", "").toString()

        val bio = settings.getString("bio", "bio")


        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24)

        navigationIcon?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)

            val wrappedDrawable = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.black))
            val drawableMargin = InsetDrawable(wrappedDrawable, 0, 0, 0, 0)
            binding.toolbar.navigationContentDescription = "Navigate up"
            binding.toolbar.navigationIcon = drawableMargin
            getUserProfile()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(this@UserProfileActivity, UserProfileEditActivity::class.java)
            startActivity(intent)
        }

        val userId = settings.getString("_id", "").toString()
        binding.username.text = username
        binding.userBioText.text = bio


        Log.d("PREFS_NAME", "PREFS_NAME: userid $userId")
        val user = User(
            _id = userId,
            avatar = avatar,
            email = "username@gmail.com",
            isEmailVerified = false,
            role = "user",
            username = username,
            lastseen = Date()
        )



        val tabsAdapter = UserProfileTabsAdapter(this, supportFragmentManager, user)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = tabsAdapter
        viewPager.offscreenPageLimit = 6

        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)
        for (i in 0 until tabsAdapter.count) {
            tabs.getTabAt(i)?.icon = tabsAdapter.getIcon(i)
        }

        binding.userAvatar.setOnClickListener {
            viewImage(avatar, "Profile Picture")
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun getUserProfile() {

        GlobalScope.launch {
            val response = try {

                retrofitInterface.apiService.getMyProfile()
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "HTTP error. Please try again.",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    MotionToast.createToast(
                        this@UserProfileActivity,
                        "Failed To Retrieve Data☹️",
                        "HTTP error. Please try again.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@UserProfileActivity, R.font.helvetica_regular)
                    )
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Network error. Please try again.",
//                        Toast.LENGTH_SHORT
//                    ).show()

                    MotionToast.createToast(
                        this@UserProfileActivity,
                        "Failed To Retrieve Data☹️",
                        "Network error. Please try again.",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this@UserProfileActivity, R.font.helvetica_regular)
                    )

                }
                return@launch
            } finally {
                // Ensure the progress bar is hidden in case of an error
//                withContext(Dispatchers.Main) {
//                    dismissLoadingDialog()
//                }
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
//                Log.d("UserProfile", "User profile ${responseBody?.data}")

                if (responseBody?.data != null) {

//                    binding.followersCount.text = responseBody.data.followersCount.toString()
//                    binding.followingCount.text = responseBody.data.followingCount.toString()
                    withContext(Dispatchers.Main) {
                        binding.followersCount.text = responseBody.data.followersCount.toString()
                        binding.followingCount.text = responseBody.data.followingCount.toString()
                    }
//                    val editor = settings.edit()
//                    editor.putString("firstname", responseBody.data.firstName)
//                    editor.putString("lastname", responseBody.data.lastName)
//                    editor.putString("avatar", responseBody.data.account.avatar.url)
//                    editor.putString("bio", responseBody.data.bio)
//                    editor.apply()

//                    val myProfile = ProfileEntity(
//                        __v = responseBody.data.__v,
//                        _id = responseBody.data._id,
//                        bio = responseBody.data.bio,
//                        firstName = responseBody.data.firstName,
//                        lastName = responseBody.data.lastName,
//                        account = responseBody.data.account,
//                        createdAt = responseBody.data.createdAt,
//                        dob = responseBody.data.dob,
//                        countryCode = responseBody.data.countryCode,
//                        coverImage = responseBody.data.coverImage,
//                        updatedAt = responseBody.data.updatedAt,
//                        followersCount = responseBody.data.followersCount,
//                        isFollowing = responseBody.data.isFollowing,
//                        location = responseBody.data.location,
//                        owner = responseBody.data.owner,
//                        phoneNumber = responseBody.data.phoneNumber,
//                        followingCount = responseBody.data.followingCount
//                    )

//                    insertProfile(myProfile)
//                    Log.d("ProfileLocal", "To localDb $myProfile")

                } else {
                    Log.d("RetrofitActivity", "Response body or data is null")
                }
            }

        }

    }

    override fun onResume() {
        super.onResume()
        avatar = settings.getString("avatar", "avatar").toString()

        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.google))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.userAvatar)
    }

    private fun viewImage(url: String, name:String){
        val intent = Intent(this, ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.user_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {
                // Handle the click on this menu item
                // For example, you can open a new activity or perform an action
//                Toast.makeText(this, "Menu item clicked", Toast.LENGTH_SHORT).show()
//                showAccessDeniedDialog("You cannot access this content because you are not an admin.")
                val intent = Intent(this@UserProfileActivity, UserProfileEditActivity::class.java)
                startActivity(intent)
                return true
            }


            // Add other cases for different menu items if needed
        }
        return super.onOptionsItemSelected(item)
    }

}