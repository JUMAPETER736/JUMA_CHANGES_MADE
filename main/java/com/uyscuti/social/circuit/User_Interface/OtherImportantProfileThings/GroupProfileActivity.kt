package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.tabs.TabLayout
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.GroupTabsAdapter
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.databinding.ActivityGroupProfileBinding
import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupProfileBinding
    private var dialog: Dialog? = null
    private lateinit var groupAdminId: String
    private lateinit var groupCreatedAt: String

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        dialog = intent.getParcelableExtra("Dialog_Extra")
        groupAdminId = intent.getStringExtra("adminId").toString()
        groupCreatedAt = intent.getStringExtra("createdAt").toString()

        supportActionBar?.title = dialog?.dialogName

        val admin = dialog?.users?.find { it.id == groupAdminId }

        val info = "Created by ${admin?.name}, on $groupCreatedAt"

        binding.groupInfo.text = info

//        dialog?.users?.map { user ->
//            if (user.id == groupAdminId) {
//                binding.groupInfo.text = "Created by ${user.name} On $groupCreatedAt"
//            }
//        }

        val tabsAdapter = GroupTabsAdapter(this, supportFragmentManager)

        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = tabsAdapter

        tabsAdapter.setDialog(dialog!!)
        tabsAdapter.setAdminId(groupAdminId)

        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)
        for (i in 0 until tabsAdapter.count) {
            tabs.getTabAt(i)?.icon = tabsAdapter.getIcon(i)
        }

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

//        // Set listener for tab selection
//        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                // Change the icon tint color for the selected tab
//                tab.icon = getTintedDrawable(R.drawable.ic_tab_selected, Color.RED)
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//                // Change the icon tint color for the unselected tab
//                tab.icon = getTintedDrawable(R.drawable.ic_tab_unselected, Color.BLACK)
//            }
//
//            override fun onTabReselected(tab: TabLayout.Tab) {
//                // Handle tab reselection if needed
//            }
//        })
//        tabs.setOnTabSelectedListener(
//            object : TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
//                override fun onTabSelected(tab: TabLayout.Tab) {
//                    super.onTabSelected(tab)
//                    val tabIconColor = ContextCompat.getColor(this@GroupProfileActivity, R.color.tabSelectedIconColor)
//                    tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
//                }
//
//                override fun onTabUnselected(tab: TabLayout.Tab) {
//                    super.onTabUnselected(tab)
//                    val tabIconColor =
//                        ContextCompat.getColor(context, R.color.tabUnselectedIconColor)
//                    tab.icon!!.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
//                }
//
//                override fun onTabReselected(tab: TabLayout.Tab) {
//                    super.onTabReselected(tab)
//                }
//            }
//        )

        binding.callTextView.setOnClickListener {
            showCallTypeDialog()
        }

        initGroup()

        binding.userAvatar.setOnClickListener {
            viewImage(dialog!!.dialogPhoto, dialog!!.dialogName)
        }
    }

    private fun viewImage(url: String, name:String){
        val intent = Intent(this, ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }


    private fun initGroup(){
        if (dialog != null){
            Glide.with(this)
                .asBitmap()
                .load(dialog?.dialogPhoto)
                .into(object : SimpleTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val drawable = RoundedBitmapDrawableFactory.create(resources, resource)

//                        drawable.cornerRadius = resources.getDimension(R.dimen.icon_radius)
                        drawable.isCircular = true

                        val marginDrawable = InsetDrawable(drawable, 0, 0, 0, 0)
                        binding.userAvatar.setImageDrawable(marginDrawable)
                    }
                })

//            binding.groupNameET.text = dialog?.dialogName
        }
    }

    private fun getTintedDrawable(drawableResId: Int, @ColorInt tintColor: Int): Drawable {
        val drawable = ContextCompat.getDrawable(this, drawableResId)
        drawable?.setTint(tintColor)
        return drawable ?: throw IllegalArgumentException("Drawable not found")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.group_profile_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_setting -> {
                // Handle the click on this menu item
                // For example, you can open a new activity or perform an action
//                Toast.makeText(this, "Menu item clicked", Toast.LENGTH_SHORT).show()
//                showAccessDeniedDialog("You cannot access this content because you are not an admin.")

                settings = getSharedPreferences(PREFS_NAME, 0)
                val userId = settings.getString("_id", "").toString()


                if (groupAdminId == userId) {
                    val intent =
                        Intent(this@GroupProfileActivity, GroupSettingsActivity::class.java)
                    startActivity(intent)
                    return true
                } else {
                    showAccessDeniedDialog("You are not group admin")
                }


            }

            R.id.exit -> {
                // Handle the click on this menu item
                // For example, you can open a new activity or perform an action
                showAccessDeniedDialog("You will be notified when this feature is ready.")

                return true
            }

            R.id.block -> {
                // Handle the click on this menu item
                // For example, you can open a new activity or perform an action
                showAccessDeniedDialog("You will be notified when this feature is ready.")
                return true
            }

            R.id.report -> {
                // Handle the click on this menu item
                // For example, you can open a new activity or perform an action
                showAccessDeniedDialog("You will be notified when this feature is ready.")

                return true
            }

            // Add other cases for different menu items if needed
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showAccessDeniedDialog(message:String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Access Denied")
            setMessage(message)
            setPositiveButton("OK") { dialog, which ->
                // Handle the OK button click if needed
                dialog.dismiss()
            }
            // Optionally, add a cancel button or other actions
            // setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    private fun menuBlocker(message:String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Group Menu")
        builder.setMessage(message)

        val dialog = builder.create()
        dialog.show()
    }

    private fun showCallTypeDialog() {
        val callTypes = arrayOf("Video Call", "Voice Call")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Group Call Type")
        builder.setItems(callTypes) { dialog, which ->
            when (which) {
                0 -> initiateVideoCall()
                1 -> initiateVoiceCall()
            }
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun initiateVideoCall() {
        // Code to start a video call
    }

    private fun initiateVoiceCall() {
        // Code to start a voice call
    }

    companion object {
        fun open(context: Context, dialog: Dialog, adminId: String, groupCreatedAt: String) {
            val intent = Intent(context, GroupProfileActivity::class.java)
            intent.putExtra("Dialog_Extra", dialog)
            intent.putExtra("adminId", adminId)
            intent.putExtra("createdAt", groupCreatedAt)
            context.startActivity(intent)
        }
    }
}