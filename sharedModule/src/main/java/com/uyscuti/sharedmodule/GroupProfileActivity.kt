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

    //  onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        dialog          = intent.getParcelableExtra("Dialog_Extra")
        groupAdminId    = intent.getStringExtra("adminId").toString()
        groupCreatedAt  = intent.getStringExtra("createdAt").toString()
        myGroupRole     = intent.getStringExtra("myRole") ?: "member"
        groupInviteLink = intent.getStringExtra("inviteLink")

        currentInviteLink  = groupInviteLink ?: ""
        currentDescription = intent.getStringExtra("description") ?: ""

        supportActionBar?.title = dialog?.dialogName

        val admin = dialog?.users?.find { it.id == groupAdminId }
        val roleLabel = when (myGroupRole) {
            "admin"     -> " · You are Admin"
            "moderator" -> " · You are Moderator"
            else        -> ""
        }
        binding.groupInfo.text       = "Created by ${admin?.name}, on $groupCreatedAt$roleLabel"
        val memberCount              = dialog?.users?.size ?: 0
        binding.memberCountText.text = "Group · $memberCount members"
        binding.membersCount.text    = memberCount.toString()

        if (currentDescription.isNotEmpty()) showDescription(currentDescription)
        else dialog?.id?.let { groupProfileViewModel.loadGroupDescription(it) }

        setupTabs()
        setupNavigationIcon()

        binding.callTextView.setOnClickListener { showCallTypeDialog() }
        binding.userAvatar.setOnClickListener {
            val photo = dialog?.dialogPhoto ?: ""
            if (photo.isNotEmpty()) viewImage(photo, dialog?.dialogName ?: "")
        }

        setupInviteLinkSection()
        setupDeleteSection()
        setupLeaveSection()        // ← NEW
        setupLockGroupSection()    // ← NEW
        observeViewModel()
        setupAddMembersSection()

        dialog?.id?.let { groupProfileViewModel.loadMembers(it) }

        if (currentInviteLink.isNotEmpty()) {
            binding.inviteLinkText.text = currentInviteLink
        } else if (myGroupRole == "admin" || myGroupRole == "moderator") {
            dialog?.id?.let { groupProfileViewModel.generateLink(it) }
        }

        initGroup()
    }

    //  Tabs

    private fun setupTabs() {
        val tabsAdapter = GroupTabsAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.viewPager
        viewPager.adapter = tabsAdapter

        dialog?.let {
            tabsAdapter.setDialog(it)
            tabsAdapter.setAdminId(groupAdminId)
            tabsAdapter.setMyRole(myGroupRole)
        }

        val tabs: TabLayout = binding.tabLayout
        tabs.setupWithViewPager(viewPager)
        for (i in 0 until tabsAdapter.count) {
            tabs.getTabAt(i)?.icon = tabsAdapter.getIcon(i)
        }
    }

    //  Navigation icon ─

    private fun setupNavigationIcon() {
        val navigationIcon = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24)
        navigationIcon?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val wrapped = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapped, ContextCompat.getColor(this, R.color.black))
            binding.toolbar.navigationIcon = InsetDrawable(wrapped, 0, 0, 0, 0)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    //  Add members ─

    private fun setupAddMembersSection() {
        // Admins AND moderators can add members (backend allows it for both)
        binding.addMembersCard.visibility =
            if (myGroupRole == "admin" || myGroupRole == "moderator") View.VISIBLE else View.GONE

        binding.addMembersBtn.setOnClickListener {
            val existingIds = ArrayList(currentMembers.map { it.user._id })
            val chatId      = dialog?.id ?: return@setOnClickListener
            addMembersLauncher.launch(
                Intent().apply {
                    setClassName(packageName, "com.uyscuti.social.circuit.ui.AddMembersActivity")
                    putExtra("chatId", chatId)
                    putStringArrayListExtra("existingMemberIds", existingIds)
                }
            )
        }
    }

    //  Leave group (NEW) ─
    /**
     * Visible to everyone EXCEPT the admin who is the sole admin.
     * The server will auto-promote the oldest member if the last admin leaves.
     * The server will also auto-delete the group if the last person leaves.
     */
    private fun setupLeaveSection() {
        // Admins see this too — they can leave if another admin exists (server enforces the rule)
        binding.leaveGroupBtn.visibility = View.VISIBLE

        binding.leaveGroupBtn.setOnClickListener {
            val message = if (myGroupRole == "admin") {
                "You are the admin. If you are the only admin, the oldest member will " +
                        "be automatically promoted. Are you sure you want to leave?"
            } else {
                "Are you sure you want to leave \"${dialog?.dialogName}\"?"
            }

            AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage(message)
                .setPositiveButton("Leave") { _, _ ->
                    dialog?.id?.let { groupProfileViewModel.leaveGroup(it) }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    //  Lock group toggle (NEW) ─
    /**
     * Visible to admin only.
     * When locked: all non-admin members are muted → nobody except admins can send messages.
     * When unlocked: all non-admin members are unmuted.
     */
    private fun setupLockGroupSection() {
        binding.lockGroupBtn.visibility =
            if (myGroupRole == "admin") View.VISIBLE else View.GONE

        updateLockButtonLabel()

        binding.lockGroupBtn.setOnClickListener {
            val newLockState = !isGroupLocked
            val title   = if (newLockState) "Lock Group" else "Unlock Group"
            val message = if (newLockState)
                "Locking the group will prevent all members from sending messages. Only admins will be able to write. Continue?"
            else
                "Unlocking the group will allow all members to send messages again. Continue?"

            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(if (newLockState) "Lock" else "Unlock") { _, _ ->
                    dialog?.id?.let {
                        groupProfileViewModel.setGroupLocked(it, newLockState, currentMembers)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun updateLockButtonLabel() {
        binding.lockGroupBtn.text = if (isGroupLocked) "🔓 Unlock Group" else "🔒 Lock Group"
    }

    //  Description ─

    private fun showDescription(desc: String) {
        if (desc.isNotEmpty()) {
            binding.userBioText.text       = desc
            binding.userBioText.visibility = View.VISIBLE
        } else {
            binding.userBioText.text       = ""
            binding.userBioText.visibility = View.GONE
        }
    }

    //  Invite link ─

    private fun setupInviteLinkSection() {
        val canManageLink = myGroupRole == "admin" || myGroupRole == "moderator"
        binding.inviteLinkCard.visibility = if (canManageLink) View.VISIBLE else View.GONE
        binding.revokeLinkBtn.visibility  = if (myGroupRole == "admin") View.VISIBLE else View.GONE

        binding.inviteLinkText.text =
            if (currentInviteLink.isNotEmpty()) currentInviteLink else "Generating link…"

        binding.copyLinkBtn.setOnClickListener {
            if (currentInviteLink.isNotEmpty()) copyToClipboard(currentInviteLink)
            else Toast.makeText(this, "Link not ready yet", Toast.LENGTH_SHORT).show()
        }
        binding.generateLinkBtn.setOnClickListener {
            dialog?.id?.let { groupProfileViewModel.generateLink(it) }
        }
        binding.revokeLinkBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Revoke Invite Link")
                .setMessage("Anyone with the old link will no longer be able to join. Continue?")
                .setPositiveButton("Revoke") { _, _ ->
                    dialog?.id?.let { groupProfileViewModel.revokeLink(it) }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.shareLinkBtn.setOnClickListener {
            if (currentInviteLink.isNotEmpty()) shareLink(currentInviteLink)
            else Toast.makeText(this, "Link not ready yet", Toast.LENGTH_SHORT).show()
        }
    }

}