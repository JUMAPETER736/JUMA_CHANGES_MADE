package com.uyscuti.social.circuit.User_Interface.MyUserProfile

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.MyUserProfileAccountBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable
import kotlin.math.abs
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getAccessToken
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getAvatarUrl
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getEmail
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUserId
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.LoginActivity.UserStorageHelper.getUsername
import com.uyscuti.social.network.api.request.profile.UpdateSocialProfileRequest
import com.uyscuti.social.network.api.response.otherusersprofile.Data
import com.uyscuti.social.network.api.retrofit.interfaces.IFlashapi
import com.uyscuti.social.network.utils.LocalStorage


private const val TAG = "MyUserProfileAccount"
private const val CAMERA_PERMISSION_CODE = 100
private const val STORAGE_PERMISSION_CODE = 101

@UnstableApi
@AndroidEntryPoint
class MyUserProfileAccount : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER = "extra_user"
        private const val EXTRA_USER_ID = "extra_user_id"
        private const val EXTRA_USER_NAME = "extra_user_name"
        private const val EXTRA_USERNAME = "extra_username"
        private const val EXTRA_AVATAR_URL = "extra_avatar_url"
        private const val EXTRA_DIALOG_PHOTO = "extra_dialog_photo"
        private const val EXTRA_DIALOG_ID = "extra_dialog_id"
        private const val EXTRA_FULL_NAME = "user_full_name"

        fun open(context: Context, user: Any, dialogPhoto: String?, dialogId: String) {
            val intent = Intent(context, MyUserProfileAccount::class.java).apply {
                putExtra(EXTRA_USER, user as? Serializable)
                putExtra(EXTRA_DIALOG_PHOTO, dialogPhoto)
                putExtra(EXTRA_DIALOG_ID, dialogId)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var apiService: IFlashapi
    private lateinit var binding: MyUserProfileAccountBinding
    private var isFollowing = false
    private lateinit var retrofitInstance: RetrofitInstance
    val userProfileLiveData = MutableLiveData<Any>()
    val onErrorFeedBack = MutableLiveData<String>()

    private var currentUsername: String = ""
    private var currentUserId: String = ""
    private var currentPage = 1
    private var currentPhotoUri: Uri? = null

    // User data variables
    internal var userId: String = ""
    private var userName: String = ""
    internal var username: String = ""
    private var fullName: String = ""
    private var avatarUrl: String? = null
    private var followerCount: Int = 0
    private var followingCount: Int = 0
    private var likesCount: Int = 0
    private var joinDate: String = ""
    private var userLocation: String = ""
    private var userBio: String = ""


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoUri?.let { uri ->
                handleImageCapture(uri)
            }
        } else {
            Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        }
    }



    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri = photoUri
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            Toast.makeText(this, "Error opening camera", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initializeApiService() {
        if (!::retrofitInstance.isInitialized) {
            val localStorage = LocalStorage(this)
            retrofitInstance = RetrofitInstance(localStorage, this)
        }
        apiService = retrofitInstance.apiService
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MyUserProfileAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeApiService()

        // Check if user data was passed via intent
        extractUserData()

        // If no user data in intent, load logged in user
        if (userId.isEmpty() || username.isEmpty()) {
            Log.d(TAG, "No user data in intent, loading logged in user")
            loadLoggedInUserDetails()
        }

        setupUserInterface()
        setupToolbar()
        setupClickListeners()
        setupScrollBehavior()
        setupStoryRingAnimation()
        observeUserProfile()
    }

    @SuppressLint("UseKtx")
    private fun setupClickListeners() {


        binding.backButton.setOnClickListener {
            // Instead of just finish(), navigate back with intent
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("fragment", "profile")  // Tell MainActivity to show profile
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        binding.followIcon.setOnClickListener {
            isFollowing = !isFollowing
            binding.followIcon.text = if (isFollowing) "Following" else "Follow"
        }

        binding.actionMessage.setOnClickListener {
            Log.d(TAG, "Message button clicked for user: $userId")

        }

        binding.callButton.setOnClickListener {
            Log.d(TAG, "Call button clicked for user: $userId")
            showCallOptions()
        }

        binding.shareProfileButton.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Check out $fullName's profile (@$username)!")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Profile"))
        }

        binding.qrCodeButton.setOnClickListener {
            showQRCodeDialog()
            Log.d(TAG, "More QRCODE clicked for user: $userId")
        }

        binding.moreOptionsButton.setOnClickListener { view ->
            showOptionsMenu(view)
            Log.d(TAG, "More options clicked for user: $userId")
        }

        binding.followingSection.setOnClickListener {
            Log.d(TAG, "Following section clicked for user: $userId")
            openFollowingScreen()
        }

        binding.followersSection.setOnClickListener {
            Log.d(TAG, "Followers section clicked for user: $userId")
            openFollowerScreen()
        }

        binding.likesSection.setOnClickListener {
            Log.d(TAG, "Likes section clicked for user: $userId")
        }

        binding.linkInBio.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://linktr.ee/$username".toUri())
            startActivity(intent)
        }

        binding.copyLinkButton.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "Profile Link",
                "https://app.com/profile/$userId"
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Profile Avatar and Frame - Show camera/gallery options
        binding.userProfileAvatar.setOnClickListener {
            Log.d(TAG, "Profile avatar clicked for user: $userId")
            showImageSourceDialog()
        }

        binding.avatarFrame.setOnClickListener {
            Log.d(TAG, "Avatar frame clicked for user: $userId")
            showImageSourceDialog()
        }

        binding.addFriendIcon.setOnClickListener {
            Log.d(TAG, "Add friend clicked for user: $userId")
            handleAddFriend()
        }

        // Edit profile button - Show edit dialog
        binding.editProfileButton.setOnClickListener {
            Log.d(TAG, "Edit profile clicked for user: $userId")
            showEditProfileDialog()
        }

        binding.accountTypeBadge.setOnClickListener {
            Log.d(TAG, "Account type badge clicked for user: $userId")
            showAccountTypeInfo()
        }

        binding.trendingStatus.setOnClickListener {
            Log.d(TAG, "Trending status clicked for user: $userId")
            showTrendingInfo()
        }

        binding.youtubeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://youtube.com/@$username".toUri())
            startActivity(intent)
        }

        binding.instagramButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, "https://instagram.com/$username".toUri())
            startActivity(intent)
        }


        binding.mutualConnectionsSection.setOnClickListener {
            Log.d(TAG, "Mutual connections clicked for user: $userId")
            showMutualConnections()
        }

        binding.userBioText.setOnClickListener {
            Log.d(TAG, "Bio text clicked for user: $userId")
        }

        binding.storyRing.setOnClickListener {
            Log.d(TAG, "Story ring clicked for user: $userId")
            openUserStories()
        }


        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(TAG, "Tab selected: ${tab?.position}")
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Log.d(TAG, "Message sent: ${binding.messageEditText.text}")
                binding.messageEditText.text.clear()
                true
            } else {
                false
            }
        }
    }

    private fun loadLoggedInUserDetails() {

        try {
            // ✅ Use UserStorageHelper from LoginActivity instead of LocalStorage
            val storedUserId = getUserId(this)
            val storedUsername = getUsername(this)
            val storedEmail = getEmail(this)
            val storedAvatarUrl = getAvatarUrl(this)
            val storedAccessToken = getAccessToken(this)

            if (storedUserId.isNotEmpty() && storedUsername.isNotEmpty()) {
                // Set the user details
                userId = storedUserId
                username = storedUsername
                avatarUrl = storedAvatarUrl

                Log.d(TAG, "Logged in user loaded from UserStorageHelper - ID: $userId, Username: $username")

                // Update UI with basic info
                binding.userName.text = "@$username"
                binding.toolbarUserName.text = "@$username"

                if (avatarUrl?.isNotEmpty() == true) {
                    loadProfileImage()
                }

                // Now load full profile from API
                loadUserProfile(username)
            } else {
                Log.w(TAG, "No logged in user found in UserStorageHelper")
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                finish()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading logged in user from UserStorageHelper: ${e.message}", e)
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initiateVoiceCall() {
        Toast.makeText(this, "Initiating voice call to $fullName...", Toast.LENGTH_SHORT).show()

    }

    private fun initiateVideoCall() {
        Toast.makeText(this, "Initiating video call to $fullName...", Toast.LENGTH_SHORT).show()

    }

    // NEW: Show image source dialog (Camera/Gallery)
    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "View Full Picture", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> showFullProfilePicture()
                }
            }
            .show()
    }

    // NEW: Check camera permission
    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> {
                showPermissionRationale()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Needed")
            .setMessage("This app needs camera access to take profile pictures.")
            .setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        showSettingsDialog()
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Camera permission is required. Please enable it in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PROFILE_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }


    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun handleImageCapture(uri: Uri) {
        Toast.makeText(this, "Photo captured! Uploading...", Toast.LENGTH_SHORT).show()
        uploadProfileImage(uri)
    }

    private fun handleImageSelection(uri: Uri) {
        Toast.makeText(this, "Image selected! Uploading...", Toast.LENGTH_SHORT).show()
        uploadProfileImage(uri)
    }

    private fun showFullProfilePicture() {

        if (!avatarUrl.isNullOrEmpty()) {
            val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.dialog_profile_full_image)

            val imageView = dialog.findViewById<ImageView>(R.id.fullImageView)
            val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)

            Glide.with(this)
                .load(avatarUrl)
                .into(imageView)

            closeButton?.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } else {
            Toast.makeText(this, "No profile picture available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCallOptions() {

        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_call_options, null)

        view.findViewById<TextView>(R.id.voiceCallOption)?.setOnClickListener {
            initiateVoiceCall()
            bottomSheet.dismiss()
        }

        view.findViewById<TextView>(R.id.videoCallOption)?.setOnClickListener {
            initiateVideoCall()
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }


    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)

        val firstNameInput = dialogView.findViewById<EditText>(R.id.firstNameInput)
        val lastNameInput = dialogView.findViewById<EditText>(R.id.lastNameInput)
        val bioInput = dialogView.findViewById<EditText>(R.id.bioInput)
        val locationInput = dialogView.findViewById<EditText>(R.id.locationInput)
        val dobInput = dialogView.findViewById<EditText>(R.id.dobInput)
        val phoneNumberInput = dialogView.findViewById<EditText>(R.id.phoneNumberInput)
        val countryCodeInput = dialogView.findViewById<EditText>(R.id.countryCodeInput)

        // Pre-fill current data from the profile response
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getMyProfile()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data

                    val firstName = data.firstName ?: ""
                    val lastName = data.lastName ?: ""
                    val bio = data.bio ?: ""
                    val location = data.location ?: ""
                    val dob = data.dob ?: ""
                    val phoneNumber = data.phoneNumber ?: ""
                    val countryCode = data.countryCode ?: ""

                    withContext(Dispatchers.Main) {
                        firstNameInput.setText(firstName)
                        lastNameInput.setText(lastName)
                        bioInput.setText(bio)
                        locationInput.setText(location)
                        dobInput.setText(dob)
                        phoneNumberInput.setText(phoneNumber)
                        countryCodeInput.setText(countryCode)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "One or More Input is InCorrect: ${e.message}", e)
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newFirstName = firstNameInput.text.toString().trim()
                val newLastName = lastNameInput.text.toString().trim()
                val newBio = bioInput.text.toString().trim()
                val newLocation = locationInput.text.toString().trim()
                val newDob = dobInput.text.toString().trim()
                val newPhoneNumber = phoneNumberInput.text.toString().trim()
                val newCountryCode = countryCodeInput.text.toString().trim()

                updateProfileOnServer(newFirstName, newLastName, newBio, newLocation, newDob, newPhoneNumber, newCountryCode)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProfileOnServer(
        firstName: String,
        lastName: String,
        bio: String,
        location: String,
        dob: String,
        phoneNumber: String,
        countryCode: String
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Create the request object using your existing data class
                val updateRequest = UpdateSocialProfileRequest(
                    firstName = firstName.ifEmpty { null },
                    lastName = lastName.ifEmpty { null },
                    bio = bio.ifEmpty { null },
                    location = location.ifEmpty { null },
                    dob = dob.ifEmpty { null },
                    phoneNumber = phoneNumber.ifEmpty { null },
                    countryCode = countryCode.ifEmpty { null }
                )

                // Call the API to update profile on server
                val response = apiService.updateMyProfile(updateRequest)

                if (response.isSuccessful && response.body() != null) {
                    val updatedData = response.body()!!.data

                    // Update local variables with server response
                    fullName = "${updatedData.firstName} ${updatedData.lastName}".trim()
                    userBio = updatedData.bio
                    userLocation = updatedData.location

                    withContext(Dispatchers.Main) {
                        // Update UI
                        binding.fullName.text = fullName.ifEmpty { username }
                        binding.userBioText.text = userBio
                        binding.userActualLocation.text = userLocation

                        // Save to local storage
                        val localStorage = LocalStorage(this@MyUserProfileAccount)
                        localStorage.saveUserData(
                            userId = userId,
                            username = username,
                            email = getEmail(this@MyUserProfileAccount),
                            avatarUrl = avatarUrl,
                            fullName = fullName,
                            accessToken = getAccessToken(this@MyUserProfileAccount)
                        )

                        Toast.makeText(
                            this@MyUserProfileAccount,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Profile update failed: ${response.code()} - $errorBody")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MyUserProfileAccount,
                            "Failed to update profile: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun uploadProfileImage(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Uploading image: $uri")

                // Convert URI to temporary file
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }

                // Prepare Multipart
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)

                // Call your exact endpoint
                val response = apiService.updateUserAvatar(avatarPart)

                if (response.isSuccessful && response.body() != null) {
                    val avatarUrl = response.body()!!.data.avatar.url
                    Log.d(TAG, "Avatar updated: $avatarUrl")

                    // Update locally
                    this@MyUserProfileAccount.avatarUrl = avatarUrl
                    val localStorage = LocalStorage(this@MyUserProfileAccount)
                    localStorage.saveUserData(
                        userId = userId,
                        username = username,
                        email = getEmail(this@MyUserProfileAccount),
                        avatarUrl = avatarUrl,
                        fullName = fullName,
                        accessToken = getAccessToken(this@MyUserProfileAccount)
                    )

                    // Refresh UI
                    withContext(Dispatchers.Main) {
                        loadProfileImage()
                        Toast.makeText(
                            this@MyUserProfileAccount,
                            "Profile picture updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Upload failed: ${response.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MyUserProfileAccount,
                            "Failed to upload: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading avatar", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    private fun loadUserProfile(username: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.getMyProfile()

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    Log.d(TAG, "Fetched profile: ${data.firstName} ${data.lastName}")

                    // Map data to local vars
                    fullName = "${data.firstName} ${data.lastName}".trim()
                    userBio = data.bio
                    userLocation = data.location
                    avatarUrl = data.account.avatar.url

                    // Save locally
                    val localStorage = LocalStorage(this@MyUserProfileAccount)
                    localStorage.saveUserData(
                        userId = data._id,
                        username = data.account.username,
                        email = data.account.email,
                        avatarUrl = avatarUrl,
                        fullName = fullName,
                        accessToken = getAccessToken(this@MyUserProfileAccount)
                    )

                    // Update UI on main thread
                    withContext(Dispatchers.Main) {
                        binding.fullName.text = fullName.ifEmpty { username }
                        binding.userBioText.text = userBio
                        binding.userActualLocation.text = userLocation
                        loadProfileImage()
                    }
                } else {
                    Log.e(TAG, "Failed to load profile: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Failed to load profile: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        getOtherUsersProfile(username)
    }

    private fun getOtherUsersProfile(username: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofitInstance.apiService.getOtherUsersProfileByUsername(username)
                val responseBody = response.body()

                if (responseBody != null) {
                    extractAndProcessProfile(responseBody.data)
                    withContext(Dispatchers.Main) {
                        userProfileLiveData.postValue(responseBody.data)
                    }
                    Log.d(TAG, "Profile loaded successfully for: $username")
                } else {
                    withContext(Dispatchers.Main) {
                        onErrorFeedBack.postValue("User data is empty")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading profile: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onErrorFeedBack.postValue("Error connecting to server. Check internet connection.")
                }
            }
        }
    }

    private fun extractAndProcessProfile(profileData: Any) {
        try {
            followerCount = extractFieldValueAsInt(profileData, "followersCount", "followers") ?: 0
            followingCount = extractFieldValueAsInt(profileData, "followingCount", "following") ?: 0
            val postsCount = extractFieldValueAsInt(profileData, "postsCount", "posts") ?: 0

            val bio = extractFieldValue(profileData as Data?, "bio") ?: ""
            val location = extractFieldValue(profileData, "location") ?: ""
            val joinedDate = extractFieldValue(profileData, "joinedDate", "createdAt")

            val extractedFirstName = extractFieldValue(profileData, "firstName") ?: ""
            val extractedLastName = extractFieldValue(profileData, "lastName") ?: ""

            val extractedOwnerId = extractFieldValue(profileData, "owner") ?: ""
            val extractedProfileId = extractFieldValue(profileData, "_id", "id") ?: ""
            val extractedUsername = extractFieldValue(profileData, "username") ?: username

            val avatarFromProfile = extractNestedFieldValue(profileData, "account", "avatar", "url")

            if (extractedOwnerId.isNotEmpty()) {
                userId = extractedOwnerId
                Log.d(TAG, "✓ Set userId to owner/account ID: $userId")
            } else if (extractedProfileId.isNotEmpty()) {
                val nestedOwnerId = extractNestedFieldValue(profileData, "account", "_id")
                if (!nestedOwnerId.isNullOrEmpty()) {
                    userId = nestedOwnerId
                    Log.d(TAG, "✓ Set userId from nested account._id: $userId")
                } else {
                    userId = extractedProfileId
                    Log.w(TAG, "⚠ Using profile _id as userId (may be wrong): $userId")
                }
            }

            if (extractedUsername.isNotEmpty()) username = extractedUsername

            if (extractedFirstName.isNotEmpty() || extractedLastName.isNotEmpty()) {
                fullName = "$extractedFirstName $extractedLastName".trim()
                if (fullName.isEmpty()) fullName = username
                userName = fullName
            }

            if (!avatarFromProfile.isNullOrEmpty()) {
                avatarUrl = avatarFromProfile
                Log.d(TAG, "Avatar URL: $avatarUrl")
            }

            userBio = bio.ifBlank {
                "✨ Content Creator | Dancer ✨\n🎵 Music Lover | Viral Videos\n#Dance #Comedy #Viral"
            }
            userLocation = location.ifBlank { "Lilongwe, Malawi" }
            joinDate = formatJoinDate(joinedDate)

            val initialPostsCount = postsCount

            Log.d(TAG, "Profile processed - userId: $userId, username: $username, name: $fullName")

            lifecycleScope.launch(Dispatchers.Main) {
                setupUserInterface()
                binding.postsCount.text = formatCount(initialPostsCount)
            }

            fetchUserTotalPostsAndLikes(username)

        } catch (e: Exception) {
            Log.e(TAG, "Error processing profile data: ${e.message}", e)
        }
    }

    private fun extractFromUserObject(userObject: Any) {
        try {
            val extractedOwnerId = extractFieldValue(userObject as Data?, "owner")
            val extractedProfileId = extractFieldValue(userObject, "userId", "_id", "id")
            val extractedFirstName = extractFieldValue(userObject, "firstName", "first_name") ?: ""
            val extractedLastName = extractFieldValue(userObject, "lastName", "last_name") ?: ""
            val extractedUsername = extractFieldValue(userObject, "username")
            val extractedDisplayName = extractFieldValue(userObject, "displayName", "name")

            if (!extractedOwnerId.isNullOrBlank()) {
                userId = extractedOwnerId
                Log.d(TAG, "✓ Using owner as userId: $userId")
            } else {
                val nestedAccountId = extractNestedFieldValue(userObject, "account", "_id")
                if (!nestedAccountId.isNullOrBlank()) {
                    userId = nestedAccountId
                    Log.d(TAG, "✓ Using account._id as userId: $userId")
                } else if (!extractedProfileId.isNullOrBlank()) {
                    userId = extractedProfileId
                    Log.w(TAG, "⚠ Using profile _id as userId (may be incorrect): $userId")
                }
            }

            if (!extractedUsername.isNullOrBlank()) username = extractedUsername

            fullName = when {
                !extractedDisplayName.isNullOrBlank() -> extractedDisplayName
                extractedFirstName.isNotBlank() && extractedLastName.isNotBlank() -> "$extractedFirstName $extractedLastName"
                extractedFirstName.isNotBlank() -> extractedFirstName
                extractedLastName.isNotBlank() -> extractedLastName
                !extractedUsername.isNullOrBlank() -> extractedUsername
                else -> "Unknown User"
            }

            avatarUrl = extractNestedFieldValue(userObject, "avatar", "url") ?:
                    extractNestedFieldValue(userObject, "account", "avatar", "url")

            Log.d(TAG, "Extracted from user object - ID: $userId, Name: $fullName, Username: $username")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting from user object: ${e.message}", e)
        }
    }

    private fun extractUserData() {
        try {
            Log.d(TAG, "Starting to extract user data from Intent")

            userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""
            userName = intent.getStringExtra(EXTRA_USER_NAME) ?: ""
            username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
            avatarUrl = intent.getStringExtra(EXTRA_AVATAR_URL)
            fullName = intent.getStringExtra(EXTRA_FULL_NAME) ?: ""

            Log.d(TAG, "Direct extras - ID: $userId, Full Name: $fullName, Username: $username")

            if (userId.isEmpty() || username.isEmpty()) {
                Log.d(TAG, "Direct extras incomplete, trying user object")
                val userObject = intent.getSerializableExtra(EXTRA_USER)
                if (userObject != null) {
                    extractFromUserObject(userObject)
                }
            }

            if (fullName.isEmpty() && userName.isNotEmpty()) {
                fullName = userName
            } else if (fullName.isEmpty() && username.isNotEmpty()) {
                fullName = username
            }

            Log.d(TAG, "Final data - ID: $userId, Name: $fullName, Username: $username")

        } catch (e: Exception) {
            Log.e(TAG, "Error extracting user data: ${e.message}", e)
            fullName = "Unknown User"
            userName = fullName
            username = "unknown"
            userId = "unknown_id"
        }
    }

    private fun fetchUserTotalPostsAndLikes(username: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching posts for username: $username")

                var totalPosts = 0
                var totalLikes = 0
                var page = 1
                var hasMorePages = true

                while (hasMorePages && page <= 10) {
                    val response = retrofitInstance.apiService.getShortsByUsername(username)

                    if (response.isSuccessful) {
                        val body = response.body()?.data

                        body?.posts?.let { posts ->
                            val userPosts = posts.filter { post ->
                                post.author?.account?.username == username
                            }

                            totalPosts += userPosts.size

                            userPosts.forEach { post ->
                                totalLikes += post.likes
                            }
                        }

                        hasMorePages = body?.hasNextPage == true
                        page++
                    } else {
                        Log.e(TAG, "API error: ${response.code()}")
                        break
                    }
                }

                Log.d(TAG, "Total posts: $totalPosts, Total likes: $totalLikes")

                withContext(Dispatchers.Main) {
                    binding.postsCount.text = formatCount(totalPosts)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.postsCount.text = "0"
                }
            }
        }
    }

    private fun setupUserInterface() {
        binding.fullName.text = fullName.ifEmpty { "Unknown User" }
        binding.userName.text = if (username.isNotEmpty()) "@$username" else "@unknown"
        binding.toolbarUserName.text = if (username.isNotEmpty()) "@$username" else "@unknown"

        binding.followersCount.text = formatCount(followerCount)
        binding.followingCount.text = formatCount(followingCount)

        binding.dateJoined.text = joinDate.ifEmpty { "Join date not available" }
        binding.userActualLocation.text = userLocation
        binding.userBioText.text = userBio

        loadProfileImage()
    }

    private fun handleAddFriend() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                Log.d(TAG, "Adding friend: $userId")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Friend request sent to $fullName",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding friend", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Failed to send friend request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAccountTypeInfo() {
        val accountType = if (extractFieldValueAsBoolean(userProfileLiveData.value, "isBusinessAccount") == true) {
            "Business Account"
        } else if (extractFieldValueAsBoolean(userProfileLiveData.value, "isCreator") == true) {
            "Creator Account"
        } else {
            "Personal Account"
        }

        AlertDialog.Builder(this)
            .setTitle(accountType)
            .setMessage("This user has a verified $accountType with access to special features.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showTrendingInfo() {
        AlertDialog.Builder(this)
            .setTitle("🔥 Trending")
            .setMessage("$fullName is currently trending! Their content is getting lots of engagement.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun openUserStories() {
        Toast.makeText(this, "Opening $fullName's stories...", Toast.LENGTH_SHORT).show()

    }


    private fun showMutualConnections() {
        Toast.makeText(this, "Showing mutual connections with $fullName", Toast.LENGTH_SHORT).show()

    }

    private fun openFollowingScreen() {
        val intent = Intent(this, UserFollowingFragment::class.java).apply {
            putExtra("user_id", userId)
            putExtra("username", username)
            putExtra("full_name", fullName)
            putExtra("following_count", followingCount)
            putExtra("tab_index", 0)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun openFollowerScreen() {
        val intent = Intent(this, UserFollowersFragment::class.java).apply {
            putExtra("user_id", userId)
            putExtra("username", username)
            putExtra("full_name", fullName)
            putExtra("followers_count", followerCount)
            putExtra("tab_index", 0)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    @SuppressLint("SetTextI18n")
    private fun showQRCodeDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.qr_code_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val qrImageView = dialog.findViewById<ImageView>(R.id.qrImageView)
        val usernameText = dialog.findViewById<TextView>(R.id.usernameText)
        val profileImageView = dialog.findViewById<ImageView>(R.id.profileImageView)

        usernameText.text = "@$username"

        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this).load(avatarUrl).circleCrop().into(profileImageView)
        } else {
            profileImageView.setImageResource(R.drawable.person_button_svgrepo_com)
        }

        val profileUrl = "https://app.com/profile/$userId"
        val qrCodeBitmap = generateQRCode(profileUrl, 512, 512)
        qrImageView.setImageBitmap(qrCodeBitmap)

        qrImageView.setOnLongClickListener {
            saveQRCodeToGallery(qrCodeBitmap)
            Toast.makeText(this, "QR code saved", Toast.LENGTH_SHORT).show()
            true
        }

        qrImageView.setOnClickListener {
            shareQRCode(qrCodeBitmap)
        }

        dialog.show()
    }

    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating QR code", e)
            null
        }
    }

    private fun saveQRCodeToGallery(bitmap: Bitmap?) {
        bitmap?.let {
            try {
                val filename = "QR_${username}_${System.currentTimeMillis()}.jpg"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    imageUri?.let { uri ->
                        resolver.openOutputStream(uri)?.use { fos ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            Toast.makeText(this, "QR code saved to gallery", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val path = MediaStore.Images.Media.insertImage(
                        contentResolver,
                        bitmap,
                        filename,
                        "Profile QR Code for $username"
                    )
                    if (path != null) {
                        Toast.makeText(this, "QR code saved", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving QR code", e)
                Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareQRCode(bitmap: Bitmap?) {
        bitmap?.let {
            try {
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "QR_$username", null)
                val uri = Uri.parse(path)

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_TEXT, "Scan this QR code to view $fullName's profile (@$username)")
                }
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing QR code", e)
                Toast.makeText(this, "Failed to share QR code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOptionsMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)  // Use 'this' instead of requireContext()

        // Apply custom style and force show icons (optional)
        try {
            val fieldPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopup.isAccessible = true
            val menuPopupWindow = fieldPopup.get(popup)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Rest of your code remains the same...
        popup.menuInflater.inflate(R.menu.post_options_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_report -> {
                    handleReportPost()
                    true
                }
                R.id.menu_block_user -> {
                    handleBlockUser()
                    true
                }
                R.id.menu_mute_user -> {
                    handleMuteUser()
                    true
                }
                R.id.menu_copy_link -> {
                    handleCopyLink()
                    true
                }
                R.id.menu_save_post -> {
                    handleSavePost()
                    true
                }
                R.id.menu_not_interested -> {
                    handleNotInterested()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun handleReportPost() {
        Toast.makeText(this, "Report post", Toast.LENGTH_SHORT).show()
    }

    private fun handleBlockUser() {
        Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show()
    }


    private fun handleSavePost() {
        Toast.makeText(this, "Post saved", Toast.LENGTH_SHORT).show()
    }

    private fun handleNotInterested() {
        Toast.makeText(this, "We'll show you fewer posts like this", Toast.LENGTH_SHORT).show()
    }

    private fun handleMuteUser() {
        AlertDialog.Builder(this)
            .setTitle("Mute $fullName?")
            .setMessage("You won't see their posts in your feed, but you can still view their profile.")
            .setPositiveButton("Mute") { _, _ ->

                Toast.makeText(this, "$fullName has been muted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleCopyLink() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Profile Link", "https://app.com/profile/$userId")
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Profile link copied to clipboard", Toast.LENGTH_SHORT).show()
    }


    private fun submitReport(reason: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                Log.d(TAG, "Reporting user $userId for: $reason")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Thank you for your report. We'll review it shortly.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting report", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyUserProfileAccount,
                        "Failed to submit report",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarUserName.alpha = 0f
    }

    private fun setupScrollBehavior() {
        binding.appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val percentage = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
                binding.toolbarUserName.alpha = percentage
            }
        )
    }

    private fun setupStoryRingAnimation() {
        val animator = ObjectAnimator.ofFloat(binding.storyRing, "rotation", 0f, 360f)
        animator.duration = 3000
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.start()
    }


    private fun loadProfileImage() {
        val imageView = binding.userProfileAvatar

        if (!avatarUrl.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(avatarUrl)
                    .apply(
                        RequestOptions()
                            .circleCrop()
                            .placeholder(R.drawable.flash21)
                            .error(R.drawable.flash21)
                    )
                    .into(imageView)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile image: ${e.message}", e)
                imageView.setImageResource(R.drawable.flash21)
            }
        } else {
            imageView.setImageResource(R.drawable.flash21)
        }
    }

    private fun observeUserProfile() {
        userProfileLiveData.observe(this) { profileData ->
            Log.d(TAG, "Profile data received: $profileData")

            // Setup TabLayout AFTER userId is correctly extracted
            if (binding.viewPager2.adapter == null) {
                Log.d(TAG, "Setting up TabLayout with correct userId: $userId, username: $username")
                setupTabLayout()
            }
        }

        onErrorFeedBack.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTabLayout() {
        val adapter = MyUserProfilePagerAdapter(this, userId, username)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 4

        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.scroll_text_line_svgrepo_com)
                1 -> tab.setIcon(R.drawable.play_svgrepo_com_white)
                2 -> tab.setIcon(R.drawable.favorite_black)
                3 -> tab.setIcon(R.drawable.business_bag_svgrepo_com)
                4 -> tab.setIcon(R.drawable.analytics_svgrepo_com)
            }
        }.attach()
    }


    // Helper extraction methods
    private fun extractFieldValue(obj: Data?, vararg fieldNames: String): String? {
        for (fieldName in fieldNames) {
            try {
                val field = obj?.javaClass?.getDeclaredField(fieldName)
                field?.isAccessible = true
                val value = field?.get(obj)
                return when (value) {
                    null -> null
                    is String -> value
                    else -> value.toString()
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun extractFieldValueAsInt(obj: Any, vararg fieldNames: String): Int? {
        for (fieldName in fieldNames) {
            try {
                val field = obj.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    is Int -> value
                    is Long -> value.toInt()
                    is String -> value.toIntOrNull()
                    null -> null
                    else -> value.toString().toIntOrNull()
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun extractFieldValueAsBoolean(obj: Any?, vararg fieldNames: String): Boolean? {
        if (obj == null) return null
        for (fieldName in fieldNames) {
            try {
                val field = obj.javaClass.getDeclaredField(fieldName)
                field.isAccessible = true
                val value = field.get(obj)
                return when (value) {
                    is Boolean -> value
                    is String -> value.toBoolean()
                    null -> null
                    else -> value.toString().toBoolean()
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun extractNestedFieldValue(obj: Any, vararg fieldPath: String): String? {
        try {
            var currentObj: Any? = obj
            for (i in 0 until fieldPath.size - 1) {
                val field = currentObj?.javaClass?.getDeclaredField(fieldPath[i])
                field?.isAccessible = true
                currentObj = field?.get(currentObj)
                if (currentObj == null) return null
            }

            val finalField = currentObj?.javaClass?.getDeclaredField(fieldPath.last())
            finalField?.isAccessible = true
            val value = finalField?.get(currentObj)?.toString()
            return if (value != "null") value else null
        } catch (e: Exception) {
            return null
        }
    }

    private fun formatJoinDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Join date not available"

        return try {
            val formats = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )

            var parsedDate: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    if (format.contains("'Z'")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    parsedDate = sdf.parse(dateString)
                    break
                } catch (e: Exception) {
                    continue
                }
            }

            if (parsedDate != null) {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                "Joined ${formatter.format(parsedDate)}"
            } else {
                "Joined $dateString"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateString", e)
            "Joined $dateString"
        }
    }

    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> {
                val millions = count / 1_000_000.0
                String.format("%.1fM", millions).replace(".0M", "M")
            }
            count >= 1_000 -> {
                val thousands = count / 1_000.0
                String.format("%.1fK", thousands).replace(".0K", "K")
            }
            else -> count.toString()
        }
    }

    inner class MyUserProfilePagerAdapter(
        private val activity: FragmentActivity,
        private val userId: String,
        private val username: String
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> {

                    // Posts Fragment
                    MyUserPostsFragment.newInstance(userId, username)
                }

                1 -> {

                    // Videos Fragment
                    MyUserVideosOnlyFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }
                }

                2 -> {

                    // Favorites Fragment
                    MyUserFavoritesFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }
                }

                3 -> {

                    // Business Fragment
                    MyUserBusinessFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }

                }

                4 -> {

                    // Business Fragment
                    MyUserAnalyticsFragment().apply {
                        arguments = Bundle().apply {
                            putString("userId", userId)
                            putString("username", username)
                        }
                    }

                }



                else -> throw IllegalStateException("Invalid position: $position")

            }
        }

    }

}