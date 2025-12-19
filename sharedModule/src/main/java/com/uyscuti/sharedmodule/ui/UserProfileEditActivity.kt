package com.uyscuti.sharedmodule.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.provider.FontRequest
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.uyscuti.sharedmodule.R
import com.uyscuti.sharedmodule.databinding.EditProfileRedesignBinding

import com.uyscuti.sharedmodule.media.ViewImagesActivity
import com.uyscuti.sharedmodule.utils.AndroidUtil
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.core.common.data.room.repository.calls.CallLogRepository
import com.uyscuti.social.core.local.utils.LocalProfile
import com.uyscuti.social.core.local.utils.SharedStorage
import com.uyscuti.social.network.api.request.profile.UpdateSocialProfileRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileEditActivity : AppCompatActivity() {

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @Inject
    lateinit var localStorage: SharedStorage

    private lateinit var callLogRepository: CallLogRepository
    private lateinit var dialogRepository: DialogRepository
    private lateinit var messageRepository: MessageRepository

    private var imagePickLauncher: ActivityResultLauncher<Intent>? = null
    private var imagePickLauncherBackground : ActivityResultLauncher<Intent>? = null
    private lateinit var thumbnail: Bitmap

//    val customActivityIntent = Intent(this, ChoosePhoto::class.java)


    private var selectedImageUri: Uri? = null

    val requestCode = 123 // Replace with any unique integer code


    private var dialog: Dialog? = null

    @Inject
    lateinit var localProfile: LocalProfile

    //    private var localStorage: SharedStorage = SharedStorage.getInstance(this)
    private var imageURl: String? = null

    private var loading = false

    private lateinit var bio: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var userName: String

    private lateinit var settings: SharedPreferences
    private val PREFS_NAME = "LocalSettings"

    private lateinit var binding: EditProfileRedesignBinding
//    private lateinit var binding: ActivityUserProfileEditBinding
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EmojiManager.install(
            GoogleCompatEmojiProvider(
                EmojiCompat.init(
                    FontRequestEmojiCompatConfig(
                        this,
                        FontRequest(
                            "com.google.android.gms.fonts",
                            "com.google.android.gms",
                            "Noto Color Emoji Compat",
                            com.uyscuti.social.chatsuit.R.array.com_google_android_gms_fonts_certs,
                        )
                    ).setReplaceAll(true)
                )
            )
        )

      binding = EditProfileRedesignBinding.inflate(layoutInflater)
//        binding = ActivityUserProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

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
        binding.editHeaderImageButton.setOnClickListener {

            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent { intent: Intent? ->
                if (intent != null) {
                    imagePickLauncherBackground?.launch(intent)
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        settings = getSharedPreferences(PREFS_NAME, 0)
        val accessToken = settings.getString("token", "").toString()

        val avatar = settings.getString("avatar", "avatar")


        Glide.with(this)
            .load(avatar)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .apply(RequestOptions.placeholderOf(R.drawable.google))
            .into(binding.avatar)




        bio = settings.getString("bio", "").toString()
        firstName = settings.getString("firstname", "").toString()
        lastName = settings.getString("lastname", "").toString()
        userName = settings.getString("username", "").toString()


        val db = ChatDatabase.getInstance(this)
        initializeLocalAttributes()

        binding.avatar.setOnClickListener {
            if (avatar != null) {
                viewImage(avatar, "Profile Picture")
            }
        }

    imagePickLauncherBackground =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    val selectedImageUri: Uri = data.data!!
                    // Load the bitmap from the URI
                    val bitmap = loadBitmapFromUri(selectedImageUri)
                    binding.headerImage.colorFilter = null
                    binding.headerImage.setPadding(0)
                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(binding.headerImage)
                    thumbnail=bitmap
                }
            }
        }
    imagePickLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    val selectedImageUri: Uri = data.data!!
                    // Load the bitmap from the URI
                    val bitmap = loadBitmapFromUri(selectedImageUri)
                    binding.avatar.colorFilter = null
                    binding.avatar.setPadding(0)
                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(binding.avatar)
                    thumbnail = bitmap

                }
            }
        }
        initializeViews()
        profileResults()
    }
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            // For versions before Android 9 (API level 28)
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            // For Android 9 (API level 28) and above
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }
    private fun profileResults() {
        imagePickLauncher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null && data.data != null) {
                    selectedImageUri = data.data
                    imageURl = selectedImageUri?.path
                    AndroidUtil.setProfilePic(
                        this,
                        selectedImageUri,
                        binding.avatar
                    )
                }
            }
        }
    }

    private fun startImagePicker(){
//        ImagePicker.with(this)
        ImagePicker.with(this)
            .galleryOnly()	//User can only select image from Gallery
            .start()	//Default Request Code is ImagePicker.REQUEST_CODE
    }

    private fun viewImage(url: String, name: String) {
        val intent = Intent(this, ViewImagesActivity::class.java)
        intent.putExtra("imageUrl", url)
        intent.putExtra("owner", name)
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val e1 = binding.bioET
        val e2 = binding.myNameET
        val e3 = binding.myLastNameET

        val t1 = binding.bioTV
        val t2 = binding.myNameTV
        val t3 = binding.myLastNameTV

        if (e1.isVisible || e2.isVisible || e3.isVisible) {
            e1.visibility = View.GONE
            e2.visibility = View.GONE
            e3.visibility = View.GONE

            t1.visibility = View.VISIBLE
            t2.visibility = View.VISIBLE
            t3.visibility = View.VISIBLE

        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLocalAttributes() {
        val avtUrl = localStorage.getUserAvatar()
        runOnUiThread {
            val image = Uri.parse(avtUrl)
//            binding.userAvatar.setImageURI(image)
            binding.myNameTV.text = firstName
            binding.myLastNameTV.text = lastName
            binding.bioTV.text = bio
//            binding.emoji.text = "@$userName"
            binding.myNameTV.text = "@$userName"
        }
    }



    private fun initializeViews() {
        binding.addPhotoWrapper.setOnClickListener {
//            val intent = Intent(this, ChoosePhoto::class.java)
//
//            startActivityForResult(intent, SELECT_PICTURE_REQUEST_CODE)
//            startImagePicker()

//            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent {
//                imagePickLauncher?.launch(intent)
//            }
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512, 512).createIntent { intent: Intent? ->
                if (intent != null) {
                    imagePickLauncher?.launch(intent)
                }

                }
        }

        binding.firstNameContainer.setOnClickListener {
            binding.myNameTV.visibility = View.GONE
            binding.myNameET.visibility = View.VISIBLE
            binding.myNameET.requestFocus()
            binding.myNameET.setText(firstName)
            binding.myNameET.setSelection(firstName.length)
            showKeyboard(binding.myNameET)

        }

        binding.lastNameContainer.setOnClickListener {
            binding.myLastNameTV.visibility = View.GONE
            binding.myLastNameET.visibility = View.VISIBLE
            binding.myLastNameET.requestFocus()
            binding.myLastNameET.setText(lastName)
            showKeyboard(binding.myLastNameET)
            // Set the cursor position to the end
            binding.myLastNameET.setSelection(lastName.length)
        }

        binding.bioContainer.setOnClickListener {
            binding.bioTV.visibility = View.GONE
            binding.bioET.visibility = View.VISIBLE
            binding.bioET.requestFocus()
            binding.bioET.setText(bio)
            binding.bioET.setSelection(bio.length)
            showKeyboard(binding.bioET)
        }

        binding.saveChanges.setOnClickListener {
            val typedBio = binding.bioET.text.toString()
            val typedFirstName = binding.myNameET.text.toString()
            val typedLastName = binding.myLastNameET.text.toString()

            var changed = true
            val editor = settings.edit()

            editor.putString("firstname", typedFirstName)
            editor.putString("lastname", typedLastName)
            editor.putString("bio", typedBio)
            editor.apply()


//            lastName = binding.myLastNameET.text.toString()

            if (typedFirstName.length > 3) {

                binding.myNameTV.text = typedFirstName
                binding.myNameET.visibility = View.GONE
                binding.myNameTV.visibility = View.VISIBLE

                firstName = typedFirstName
                changed = true
            }


            if (typedBio.length > 3) {

                binding.bioTV.text = typedBio
                binding.bioET.visibility = View.GONE
                binding.bioTV.visibility = View.VISIBLE

                bio = typedBio
                changed = true
            }

            if (typedLastName.length > 3) {

                binding.myLastNameTV.text = typedLastName
                binding.myLastNameET.visibility = View.GONE
                binding.myLastNameTV.visibility = View.VISIBLE

                lastName = typedLastName
                changed = true
            }

            imageURl?.let { it1 -> localStorage.setUserAvatar(it1) }


            val data = UpdateSocialProfileRequest(
                bio = bio,
                countryCode = "265",
                dob = "2000-12-20T17:25:24.365Z",
                firstName = firstName,
                lastName = lastName,
                location = "Lilongwe, Malawi",
                phoneNumber = "8273827837"
            )

            Log.d("UserProfile", "Update Profile Date: $data")

            if (changed && !loading) {
                showLoadingDialog()
                loading = true

                CoroutineScope(Dispatchers.IO).launch {
//                    delay(500)
                    Log.d("UserProfile", "profile update: $data")

                    updateMyProfile(data)
                }
            } else {
                dismissLoadingDialog()
                Log.d("UserProfile", "failed profile update")

            }


            imageURl?.let { it1 ->
                Log.d("Profile Pic", "initializeViews: Update avatar 1 and loading value is $loading")

                if (!loading) {
                    Log.d("Profile Pic", "initializeViews: Update avatar 2 loading value $loading ")

                    showLoadingDialog()
                }
                Log.d("Profile Pic", "initializeViews: Update avatar ")
                updateAvatar(it1)


                val contentResolver = applicationContext.contentResolver

//                val filePath = getRealPathFromUri(contentResolver, Uri.parse(it1))
                val filePath = selectedImageUri?.let { it2 -> getRealPathFromUri(contentResolver, it2) }

//                if (filePath != null) {
//                    Log.d("Profile", "Profile File Path Resolved: $filePath")
//                    updateAvatar(imageURl!!)
//                }
            }

            val handler = Handler()

//            handler.postDelayed(
//                {
//                    val userAttr = LocalUserAttr(
//                        userId = "local",
//                        avatarUrl = imageURl,
//                        fullName = "Dennis Reuben",
//                        userName = "dennis"
//                    )
//
//                    dbViewModel.insertLocalUserAttr(userAttr)
//
//
//
//                    val resultIntent = Intent()
//                    resultIntent.putExtra("image_url", imageURl)
//                    setResult(Activity.RESULT_OK, resultIntent)
//                    finish()
//                }, 5000
//            )
        }
//
//        binding.logout.setOnClickListener {
//            showLogoutConfirmationDialog()
//        }
    }

    private fun showKeyboard(editText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, which ->
            // Handle logout here
            performLogout()
        }
        builder.setNegativeButton("No") { dialog, which ->
            // Dismiss the dialog
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            callLogRepository.clearAll()
            messageRepository.clearAll()
            dialogRepository.clearAll()

            delay(500)
            gotoLauncher()
        }
    }

    private fun gotoLauncher() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        startActivity(intent)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Log.d("UserProfile", "onActivityResult: $requestCode")

        if (requestCode == SELECT_PICTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUrl = data?.getStringExtra("image_url")
            if (imageUrl != null) {
                // Handle the received image URL from the SelectPictureActivity
                // imageUrl contains the URL of the selected image
                val imagePath = Uri.parse(imageUrl)

                imageURl = imageUrl

                Log.d("UserProfile", "Image Path : $imagePath")

                val contentResolver = applicationContext.contentResolver

                val filePath = getRealPathFromUri(contentResolver, imagePath)

                Glide.with(this).load(filePath).apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.avatar)

//                binding.avatar.setImageURI(Uri.parse(imageUrl))
                if (filePath != null) {

                    Log.d("UserProfile", "Image Path Resolved : $filePath")
//                    updateAvatar(filePath)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAvatar(file: String) {
//        Toast.makeText(
//            this@UserProfileEditActivity,
//            "Upload Started",
//            Toast.LENGTH_SHORT
//        ).show()
        // Create a File object with the path to the user's new avatar
        val imageFile = File(file)
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestBody = imageFile.asRequestBody(mediaType)

        Log.d("Profile Pic", "initializeViews: inside Update avatar ")

        // Create a RequestBody from the file
//        val requestFile = RequestBody.create(mediaType, avatarFile)

        // Create a MultipartBody.Part from the RequestBody
        val avatarPart = MultipartBody.Part.createFormData("avatar", imageFile.name, requestBody)

        GlobalScope.launch {
            try {
                Log.d("Profile Pic", "initializeViews: inside Update avatar try block ")

                val response = retrofitInterface.apiService.updateUserAvatar(avatarPart)
                Log.d("Profile Pic", "initializeViews: inside Update avatar response1 $response ")

//                response.body().data.avatar.url

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Log.d("UserProfile", "Profile Picture updated successfully")
                        Toast.makeText(
                            this@UserProfileEditActivity,
                            "Profile Picture updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismissLoadingDialog()
                        val editor = settings.edit()
                        editor.putString("avatar", file)


                        editor.apply()
                        // Call this function from your activity
//                        loadProfileImage(profilePic, width, height)
                    }else {
                        Log.d("Profile Pic", "updateAvatar: failed to update avatar")
                        Log.d("Profile Pic", "updateAvatar: response = ${response.message()}")
                        Log.d("Profile Pic", "updateAvatar: response = ${response.body()}")
                        Log.d("Profile Pic", "updateAvatar: response = ${response.errorBody()}")

                    }
                }

            } catch (e: HttpException) {
                dismissLoadingDialog()
                Log.d("UserProfile", "Http Exception In UserProfileEdit Activity is : ${e.message}")
                Toast.makeText(
                    this@UserProfileEditActivity,
                    "Failed to update profile picture, check your internet connection and try again",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: IOException) {
                dismissLoadingDialog()
                Log.d("UserProfile", "IOException In UserProfileEdit Activity is : ${e.message}")
                Toast.makeText(
                    this@UserProfileEditActivity,
                    "Failed to update profile picture, try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getRealPathFromUri(contentResolver: ContentResolver, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                filePath = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            Log.e("UserProfile", "Error resolving content URI to file path: $e")
        } finally {
            cursor?.close()
        }

        return filePath
    }

    private fun updateMyProfile(data: UpdateSocialProfileRequest) {
//        Log.d("UserProfile", "gi dem update")

        GlobalScope.launch {

            try {
//                Log.d("UserProfile", "try gi dem update")

                val response = retrofitInterface.apiService.updateMyProfile(data)
//                Log.d("UserProfile", "gi dem update data - $response")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
//                        Log.d("UserProfile", "Updated Profile Successfully ")
                        Toast.makeText(
                            this@UserProfileEditActivity,
                            "Updated Profile Successfully ",
                            Toast.LENGTH_SHORT
                        ).show()
                        val editor = settings.edit()
                        editor.putString("firstname", data.firstName)
                        editor.putString("lastname", data.lastName)
                        editor.putString("bio", data.bio)


                        editor.apply()

                        dismissLoadingDialog()
//                        finish()
                    }
                }

            } catch (e: HttpException) {
                dismissLoadingDialog()
//                Log.d("UserProfile", "Http Exception In UserProfile is : ${e.message}")
                e.printStackTrace()
                Toast.makeText(
                    this@UserProfileEditActivity,
                    "Failed to Updated Profile, check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                dismissLoadingDialog()
//                Log.d("UserProfile", "IOException In UserProfile is : ${e.message}")
                e.printStackTrace()
                Toast.makeText(
                    this@UserProfileEditActivity,
                    "Failed to Updated Profile, try again later",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLoadingDialog() {
        loading = true
        // Create a dialog with the loading layout
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside

        // Show the dialog
        dialog?.show()
    }
    private fun dismissLoadingDialog() {
        loading = false
        dialog?.dismiss()
    }
    companion object {
        private const val SELECT_PICTURE_REQUEST_CODE = 1
        const val IMAGE_URL_KEY = "image_url"
    }
}