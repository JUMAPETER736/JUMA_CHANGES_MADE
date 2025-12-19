package com.uyscuti.sharedmodule


import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.uyscuti.sharedmodule.adapter.BusinessMediaViewPager
import com.uyscuti.sharedmodule.databinding.ActivityRepostBusinessPostBinding
import com.uyscuti.sharedmodule.utils.formattedMongoDateTime
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.graphics.toColorInt
import com.uyscuti.sharedmodule.adapter.MediaPagerAdapter
import com.uyscuti.sharedmodule.media.ImagePickerManager
import com.uyscuti.sharedmodule.media.VideoPickerManager

@AndroidEntryPoint
class RepostBusinessPost : AppCompatActivity() {

    @Inject
    lateinit var localStorage: LocalStorage
    private lateinit var viewPagerAdapter: MediaPagerAdapter
    private val mediaUrls = ArrayList<String>()

    private lateinit var binding: ActivityRepostBusinessPostBinding

    private lateinit var imagePicker: ImagePickerManager
    private lateinit var videoPicker: VideoPickerManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepostBusinessPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        handleEventListener()

        val post = intent.getSerializableExtra("itemToRepost") as Post

        setupProductOwnerDetails(post)

    }

    private fun initViews() {
        setupCurrentUserDetails()
        setupViewPager()
        imagePicker = ImagePickerManager(this)
        videoPicker = VideoPickerManager(this)
    }

    private fun setupCurrentUserDetails() {
        val userAvatarUrl = getSharedPreferences("LocalSettings", MODE_PRIVATE).getString("profile_pic","").toString()
        Glide.with(this)
            .load(userAvatarUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.repostUserAvatar)

        binding.reposterName.text = localStorage.getUsername().toString()
        binding.reposterUsername.text = "@${localStorage.getUsername()}"

    }

    private fun setupProductOwnerDetails(item: Post) {
        Glide.with(this)
            .load(item.userDetails.avatar)
            .circleCrop()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .into(binding.ivUserAvatar)

        binding.tvUsername.text = "@${item.userDetails.username}"
        binding.tvPostTime.text = formattedMongoDateTime(item.createdAt)
        binding.tvDescription.text = item.description
        binding.tvItemTitle.text = item.itemName
        binding.tvItemPrice.text = "MWK ${item.price}"

        binding.buCommentCount.text = item.comments.toString()
        binding.buLikeCount.text = item.likes.toString()
        binding.buBookMarkCount.text = item.bookmarkCount.toString()

        setupRecyclerView(item)

    }

    private fun setupRecyclerView(item: Post) {

        if (item.images.isNotEmpty()) {
            val businessMediaViewPager = BusinessMediaViewPager(
                this,
                item.images,
                onItemClicked = { position ->

                }
            )

            // Setup RecyclerView layout based on file count
            binding.businessRecycler.layoutManager = when (item.images.size) {
                1 -> GridLayoutManager(this, 1)
                2 -> {
                    GridLayoutManager(this, 2)
                }
                3, 4, 5 -> {
                    StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    ) }

                else -> GridLayoutManager(this, 2)
            }

            binding.businessRecycler.hasFixedSize()
            binding.businessRecycler.adapter = businessMediaViewPager
        }

        if(item.images.size > 4) {

            val fileSize = item.images.size

            // Set the "+N" text
            binding.tvMediaCounter.text = "+${fileSize - 4}"
            binding.tvMediaCounter.textSize = 32f
            binding.tvMediaCounter.setTextColor(Color.WHITE)
            binding.tvMediaCounter.setTypeface(null, Typeface.NORMAL)

            // Create rounded dimmed background
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f // Rounded corners
                setColor("#80000000".toColorInt()) // Semi-transparent black
            }

            binding.tvMediaCounter.background = background
            binding.tvMediaCounter.visibility = View.VISIBLE
        } else {
            binding.tvMediaCounter.visibility = View.GONE
        }

    }

    private fun handleEventListener() {
        onGoBack()
        handleImagesSelected()
        handleVideoSelected()
        binding.addMore.setOnClickListener { showBottomSheet() }
    }

    private fun setupViewPager() {
        viewPagerAdapter = MediaPagerAdapter(
            context = this,
            mediaUrls = mediaUrls
        )

        binding.mediaViewPager.adapter = viewPagerAdapter
        binding.wormDotsIndicator.attachTo(binding.mediaViewPager)
    }

    @SuppressLint("InflateParams")
    private fun showBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.add_more_bottom_sheet,null)


        val addImage: CardView = view.findViewById(R.id.add_image)
        val addVideo: CardView = view.findViewById(R.id.add_video)

        addVideo.setOnClickListener {
            videoPicker.launchVideoPicker(allowMultiple = true)
            dialog.dismiss()
        }

        addImage.setOnClickListener {
            imagePicker.launchImagePicker(allowMultiple = true)
            dialog.dismiss()
        }


        dialog.setContentView(view)
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleImagesSelected() {

        imagePicker.initialize { images ->
            if(images.isNotEmpty()) {
                // Handle selected images
                images.forEach { (uri, fileName) ->
                    Log.d("ImagePicker", "Image: $fileName - $uri")
                    mediaUrls.add(uri.toString())
                    viewPagerAdapter.notifyDataSetChanged()
                }

                if (mediaUrls.isNotEmpty()) {
                    binding.viewPagerLayout.visibility = View.VISIBLE
                }

            } else {
                // User cancelled or no image selected
                Log.d("ImagePicker", "Image picker cancelled")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleVideoSelected() {
        videoPicker.initialize { videosList ->
            if (videosList.isNotEmpty()) {
                videosList.forEach { (uri, fileName) ->
                    Log.d("VideoPicker", "Video: $fileName - $uri")
                    val resolvedUrl = getRealPathFromUri(uri)
                    Log.d("VideoUrl", "Resolved Url: $resolvedUrl")
                    viewPagerAdapter.addMediaUrl(Uri.parse(resolvedUrl))
                }

                if (mediaUrls.isNotEmpty()){
                    binding.viewPagerLayout.visibility = View.VISIBLE
                }

            } else {
                Log.d("VideoPicker", "Video picker cancelled")
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }


    private fun onGoBack(){
        binding.closeRepost.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}