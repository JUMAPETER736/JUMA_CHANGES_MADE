package com.uyscuti.sharedmodule

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import com.uyscuti.sharedmodule.adapter.GroupTabsAdapter
import com.uyscuti.sharedmodule.databinding.ActivityGroupProfileBinding
import com.uyscuti.sharedmodule.media.ViewImagesActivity
import com.uyscuti.social.core.models.data.Dialog
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GroupProfileActivity : AppCompatActivity() {

    companion object {
        fun open(
            context:        Context,
            dialog:         Dialog,
            adminId:        String,
            groupCreatedAt: String,
            myRole:         String  = "member",
            inviteLink:     String? = null,
            description:    String  = ""
        ) {
            context.startActivity(
                Intent(context, GroupProfileActivity::class.java).apply {
                    putExtra("Dialog_Extra", dialog)
                    putExtra("adminId",      adminId)
                    putExtra("createdAt",    groupCreatedAt)
                    putExtra("myRole",       myRole)
                    putExtra("inviteLink",   inviteLink)
                    putExtra("description",  description)
                }
            )
        }
    }


    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var binding:        ActivityGroupProfileBinding
    private lateinit var groupAdminId:   String
    private lateinit var groupCreatedAt: String
    private lateinit var settings:       SharedPreferences
    private val PREFS_NAME = "LocalSettings"


    private var dialog:             Dialog? = null
    private var myGroupRole:        String  = "member"
    private var groupInviteLink:    String? = null
    private var currentInviteLink:  String  = ""
    private var currentDescription: String  = ""

    // Tracks whether the group is currently locked (all non-admins muted)
    private var isGroupLocked: Boolean = false

    private val groupProfileViewModel: GroupProfileViewModel by viewModels()
    private var currentMembers: List<com.uyscuti.social.network.api.models.GroupMember> = emptyList()

    //  Launchers ─

    private val addMembersLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                dialog?.id?.let { groupProfileViewModel.loadMembers(it) }
                Toast.makeText(this, "Members added successfully", Toast.LENGTH_SHORT).show()
            }
        }

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // GroupSettingsActivity sends RESULT_GROUP_DELETED when the group was deleted
            // or the user left from within settings — just finish this screen too.
            if (result.resultCode == GroupSettingsActivity.RESULT_GROUP_DELETED) {
                setResult(RESULT_OK)
                finish()
                return@registerForActivityResult
            }
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                val newPhoto = data?.getStringExtra(GroupSettingsActivity.EXTRA_UPDATED_PHOTO)
                if (!newPhoto.isNullOrBlank()) {
                    dialog = dialog?.let {
                        Dialog(it.id, it.dialogName, newPhoto, it.users, it.lastMessage, it.unreadCount)
                    }
                    Glide.with(this).asBitmap().load(newPhoto)
                        .placeholder(R.drawable.baseline_groups_24)
                        .error(R.drawable.baseline_groups_24)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                val drawable = RoundedBitmapDrawableFactory.create(resources, resource)
                                drawable.isCircular = true
                                binding.userAvatar.setImageDrawable(InsetDrawable(drawable, 0, 0, 0, 0))
                            }
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                binding.userAvatar.setImageResource(R.drawable.baseline_groups_24)
                            }
                        })
                }

                val newName = data?.getStringExtra(GroupSettingsActivity.EXTRA_UPDATED_NAME)
                if (!newName.isNullOrBlank()) {
                    supportActionBar?.title = newName
                    dialog = dialog?.let {
                        Dialog(it.id, newName, it.dialogPhoto, it.users, it.lastMessage, it.unreadCount)
                    }
                    dialog?.id?.let { groupProfileViewModel.updateGroupNameLocally(it, newName) }
                }

                val newDesc = data?.getStringExtra(GroupSettingsActivity.EXTRA_UPDATED_DESC)
                if (newDesc != null) {
                    currentDescription = newDesc
                    showDescription(newDesc)
                    dialog?.id?.let { groupProfileViewModel.updateGroupDescriptionLocally(it, newDesc) }
                }
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

}