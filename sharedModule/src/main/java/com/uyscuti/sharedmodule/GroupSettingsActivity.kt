package com.uyscuti.sharedmodule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bumptech.glide.Glide
import com.uyscuti.sharedmodule.databinding.ActivityGroupSettingsBinding
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class GroupSettingsActivity : AppCompatActivity() {

    companion object {
        const val RESULT_GROUP_DELETED = 100
        const val EXTRA_UPDATED_NAME = "updated_name"
        const val EXTRA_UPDATED_DESC = "updated_description"
        const val EXTRA_UPDATED_PHOTO = "updated_photo"

        fun open(
            context: Context,
            chatId: String,
            myRole: String,
            inviteLink: String?,
            groupName: String,
            memberCount: Int,
            description: String = "",
            editInfoLocked: Boolean = false
        ) {
            context.startActivity(
                Intent(context, GroupSettingsActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("myRole", myRole)
                    putExtra("inviteLink", inviteLink ?: "")
                    putExtra("groupName", groupName)
                    putExtra("memberCount", memberCount)
                    putExtra("description", description)
                    putExtra("editInfoLocked", editInfoLocked)
                }
            )
        }
    }


    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var binding: ActivityGroupSettingsBinding

    private var chatId:          String  = ""
    private var myRole:          String  = "member"
    private var inviteLink:      String  = ""
    private var groupName:       String  = ""
    private var memberCount:     Int     = 0
    private var pendingName:     String  = ""
    private var pendingDesc:     String  = ""
    private var pendingPhotoUrl: String  = ""
    private var editInfoLocked:  Boolean = false


    private var renamePending:      Boolean = false
    private var descriptionPending: Boolean = false

    private val viewModel: GroupProfileViewModel by viewModels()

    private var isEditingName:        Boolean      = false
    private var isEditingDescription: Boolean      = false
    private var deleteDialog:         AlertDialog? = null
    private var isDeleting:           Boolean      = false

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) uploadGroupAvatar(uri)
            }
        }

    //  onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId         = intent.getStringExtra("chatId")         ?: ""
        myRole         = intent.getStringExtra("myRole")         ?: "member"
        inviteLink     = intent.getStringExtra("inviteLink")     ?: ""
        groupName      = intent.getStringExtra("groupName")      ?: ""
        memberCount    = intent.getIntExtra("memberCount", 0)
        editInfoLocked = intent.getBooleanExtra("editInfoLocked", false)

        val savedDescription = intent.getStringExtra("description") ?: ""
        if (savedDescription.isNotEmpty()) {
            binding.myLastNameTV.text = savedDescription
            binding.myLastNameTV.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        setupToolbar()
        setupHeader()
        setupNameField()
        setupDescriptionField()
        setupInviteLink()
        setupPermissions()
        setupDangerZone()
        setupSaveButton()
        observeViewModel()

        if (chatId.isNotEmpty()) {
            viewModel.loadGroupDetail(chatId)
            viewModel.loadMembers(chatId)
        }
    }

    //  Toolbar ─

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        val nav = ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_ios_24)
        nav?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            val wrapped = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapped, ContextCompat.getColor(this, R.color.black))
            binding.toolbar.navigationIcon = InsetDrawable(wrapped, 0, 0, 0, 0)
        }
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    //  Header

    private fun setupHeader() {
        binding.groupNameHeaderTV.text = groupName.ifBlank { "Group" }
        binding.memberCountTV.text     = "$memberCount members"

        // Avatar edit only available to admins regardless of editInfoLocked
        val isAdmin = myRole == "admin"
        binding.addPhotoWrapper.visibility = if (isAdmin) View.VISIBLE else View.GONE

        binding.addPhotoWrapper.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }
    }


    //  Upload avatar ─

    private fun uploadGroupAvatar(uri: Uri) {

        CoroutineScope(Dispatchers.IO).launch {
            try {

                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val tempFile    = File.createTempFile("group_avatar_", ".jpg", cacheDir)
                tempFile.outputStream().use { inputStream.copyTo(it) }

                val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("avatar", tempFile.name, requestBody)

                val response = retrofitInterface.apiService.updateGroupAvatar(chatId, part)

                withContext(Dispatchers.Main) {

                    if (response.isSuccessful) {
                        val newAvatarUrl = response.body()?.data?.avatar?.url ?: ""

                        Glide.with(this@GroupSettingsActivity).load(uri).circleCrop()
                            .placeholder(R.drawable.baseline_groups_24)
                            .error(R.drawable.baseline_groups_24)
                            .into(binding.userAvatar)
                        pendingPhotoUrl = newAvatarUrl.ifEmpty { uri.toString() }

                        if (newAvatarUrl.isNotEmpty())
                            viewModel.updateGroupAvatarLocally(chatId, newAvatarUrl)
                        Toast.makeText(this@GroupSettingsActivity, "Group photo updated", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(
                            this@GroupSettingsActivity,
                            "Failed to update photo: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                tempFile.delete()

            } catch (e: Exception) {
                Log.e("GroupSettings", "uploadGroupAvatar error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GroupSettingsActivity, "Error uploading photo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    //  Name field

    private fun setupNameField() {
        binding.myNameTV.text = groupName.ifBlank { "Group" }
        applyNameEditVisibility()

        binding.myNameEditBtn.setOnClickListener {
            isEditingName = !isEditingName
            if (isEditingName) {
                binding.myNameTV.visibility = View.GONE
                binding.myNameET.visibility = View.VISIBLE
                binding.myNameET.setText(binding.myNameTV.text)
                binding.myNameET.requestFocus()
                binding.myNameET.setSelection(binding.myNameET.text.length)
                binding.myNameEditBtn.setImageResource(R.drawable.baseline_check_24)
            } else {
                val newName = binding.myNameET.text.toString().trim()
                if (newName.isNotEmpty()) {
                    binding.myNameTV.text          = newName
                    binding.groupNameHeaderTV.text = newName
                }
                binding.myNameET.visibility = View.GONE
                binding.myNameTV.visibility = View.VISIBLE
                binding.myNameEditBtn.setImageResource(R.drawable.baseline_edit_24)
            }
        }
    }

    // Determines if the name edit button should show based on role + lock state
    private fun applyNameEditVisibility() {
        val canEdit = myRole == "admin" || (!editInfoLocked && myRole == "moderator")
        binding.myNameEditBtn.visibility = if (canEdit) View.VISIBLE else View.GONE
    }


    //  Description field ─

    private fun setupDescriptionField() {
        applyDescriptionEditVisibility()

        binding.descriptionEditBtn.setOnClickListener {
            isEditingDescription = !isEditingDescription
            if (isEditingDescription) {
                binding.myLastNameTV.visibility = View.GONE
                binding.myLastNameET.visibility = View.VISIBLE
                val current = binding.myLastNameTV.text.toString()
                binding.myLastNameET.setText(if (current == "Add a description...") "" else current)
                binding.myLastNameET.requestFocus()
                binding.descriptionEditBtn.setImageResource(R.drawable.baseline_check_24)
            } else {
                val newDesc = binding.myLastNameET.text.toString().trim()
                binding.myLastNameTV.text =
                    if (newDesc.isNotEmpty()) newDesc else "Add a description..."
                binding.myLastNameTV.setTextColor(
                    if (newDesc.isNotEmpty()) ContextCompat.getColor(this, R.color.black)
                    else ContextCompat.getColor(this, android.R.color.darker_gray)
                )
                binding.myLastNameET.visibility = View.GONE
                binding.myLastNameTV.visibility = View.VISIBLE
                binding.descriptionEditBtn.setImageResource(R.drawable.baseline_edit_24)
            }
        }
    }


    private fun applyDescriptionEditVisibility() {
        val canEdit = myRole == "admin" || (!editInfoLocked && myRole == "moderator")
        binding.descriptionEditBtn.visibility = if (canEdit) View.VISIBLE else View.GONE
    }

    //  Permissions (admin only)

    //  Invite link ─

    private fun setupInviteLink() {
        val canManage = myRole == "admin" || myRole == "moderator"

        binding.inviteSectionLabel.visibility = if (canManage) View.VISIBLE else View.GONE
        binding.inviteLinkSection.visibility  = if (canManage) View.VISIBLE else View.GONE
        binding.inviteLinkDivider.visibility  = if (canManage) View.VISIBLE else View.GONE
        binding.revokeLinkBtn.visibility      = if (myRole == "admin") View.VISIBLE else View.GONE

        if (inviteLink.isNotEmpty()) binding.inviteLinkText.text = inviteLink
        else if (canManage) viewModel.generateLink(chatId)
        binding.copyLinkBtn.setOnClickListener {
            val link = binding.inviteLinkText.text.toString()
            if (link.isNotEmpty() && link != "Generating..." && link != "No active link")
                copyToClipboard(link)
            else Toast.makeText(this, "Link not ready yet", Toast.LENGTH_SHORT).show()
        }
        binding.shareLinkBtn.setOnClickListener {
            val link = binding.inviteLinkText.text.toString()
            if (link.isNotEmpty() && link != "Generating..." && link != "No active link")
                shareLink(link)
            else Toast.makeText(this, "Link not ready yet", Toast.LENGTH_SHORT).show()
        }
        binding.revokeLinkBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Invite Link")
                .setMessage("The old link will stop working. Continue?")
                .setPositiveButton("Reset") { _, _ -> viewModel.revokeLink(chatId) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    //  Save button ─

    private fun setupSaveButton() {
        val canEdit = myRole == "admin" || (!editInfoLocked && myRole == "moderator")
        binding.saveChangesBtn.visibility = if (canEdit) View.VISIBLE else View.GONE

        binding.saveChangesBtn.setOnClickListener {
            if (isEditingName)        binding.myNameEditBtn.performClick()
            if (isEditingDescription) binding.descriptionEditBtn.performClick()

            val newName = binding.myNameTV.text.toString().trim()
            val newDesc = binding.myLastNameTV.text.toString().let {
                if (it == "Add a description...") "" else it.trim()
            }

            if (newName.isEmpty()) {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.saveChangesBtn.isEnabled = false
            binding.saveChangesBtn.text      = "Saving..."

            pendingName = newName
            pendingDesc = newDesc

            val originalDesc   = intent.getStringExtra("description") ?: ""
            val descChanged    = newDesc != originalDesc
            renamePending      = true
            descriptionPending = descChanged

            viewModel.renameGroup(chatId, newName)
            if (descChanged) viewModel.updateDescription(chatId, newDesc)
        }
    }


    //  Danger zone (delete + leave)

    private fun setupDangerZone() {
        binding.deleteGroupBtn.visibility = if (myRole == "admin") View.VISIBLE else View.GONE

        binding.deleteGroupBtn.setOnClickListener {
            if (isDeleting) return@setOnClickListener
            deleteDialog = AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage(
                    "This will permanently delete the group and remove all members. " +
                            "This cannot be undone."
                )
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteGroup(chatId) }
                .setNegativeButton("Cancel", null)
                .create()
            deleteDialog?.show()
        }

        binding.leaveGroupBtn.visibility = View.VISIBLE

        binding.leaveGroupBtn.setOnClickListener {
            val message = if (myRole == "admin") {
                "You are the admin. If you are the only admin, the oldest member will be " +
                        "automatically promoted to admin. Are you sure you want to leave?"
            } else {
                "Are you sure you want to leave this group?"
            }

            AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage(message)
                .setPositiveButton("Leave") { _, _ -> viewModel.leaveGroup(chatId) }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }



























}