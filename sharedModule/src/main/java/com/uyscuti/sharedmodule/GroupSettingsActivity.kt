package com.uyscuti.sharedmodule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.uyscuti.sharedmodule.databinding.ActivityGroupSettingsBinding
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
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











































}