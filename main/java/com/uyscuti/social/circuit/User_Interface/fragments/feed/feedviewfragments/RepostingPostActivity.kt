package com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.feed.multiple_files.FeedRepostViewFileAdapter
import com.uyscuti.social.circuit.databinding.ActivityRepostingPostBinding
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("StaticFieldLeak")
private const val TAG = "RepostingPostActivity"
@AndroidEntryPoint
class RepostingPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRepostingPostBinding
    private lateinit var data:  com.uyscuti.social.network.api.response.posts.Post

    private val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Inflate the layout using ViewBinding
        binding = ActivityRepostingPostBinding.inflate(layoutInflater)
        // Set the content view after binding
        setContentView(binding.root)

        // Now you can safely use ViewCompat to apply insets to the main view
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the toolbar as support action bar
        setSupportActionBar(binding.toolbar)
        if (data.author.account.username.isNotEmpty()) {
            binding.originalPostUsername.text = data.author.account.username
        } else {
            Log.e("NewRepostedPostFragment", "The author list is empty or null!")

            binding.originalPostUsername.text = "UnknownAuthor"
        }

        if (data.content == "" && data.content.isEmpty()) {
            binding.originalFeedTextContent.visibility = View.GONE
        }else{
            binding.originalFeedTextContent.visibility = View.VISIBLE
            binding.originalFeedTextContent.text = data.content
        }
        if (data.originalPost.isNotEmpty()) {

            Glide.with(this)
                .load(data.originalPost[0].author.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.originalPostProfileImage)

            binding.originalFeedTextContent.text = data.originalPost[0].content

            binding.originalFeedTextContent.visibility = View.VISIBLE
            binding.mixedFilesCardView.visibility = View.GONE
        }else{
            binding.originalFeedTextContent.visibility = View.GONE
            binding.mixedFilesCardView.visibility = View.VISIBLE
        }
        if (data.author!!.account.avatar.url.isNotEmpty() && data.author!!.account.avatar.url.isNotEmpty()) {
            Glide.with(this)
                .load(data.author!!.account.avatar.url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .placeholder(R.drawable.profilepic2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.originalPostProfileImage)
            val fileList: MutableList<String> = mutableListOf()

        } else {

            Glide.with(this)
                .load(R.drawable.profilepic2)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.originalPostProfileImage)

            Log.e("NewRepostedPostFragment", "Author list is empty or avatar URL is missing!")
        }


        val fileList: MutableList<String> = mutableListOf()
        if (data.files.isNotEmpty()) {
            for (file in data.files) {
                Log.d(TAG, "render: images ${file.url}")
                fileList.add(file.url)
            }
        } else {
            Log.d(TAG, "render: data files is empty")
        }

        try {

            when (data.contentType) {

                "text" -> {
                    if (data.content.isNotEmpty()){
                        Log.d("clicked", "render: original post text")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.content
                        binding.mixedFilesCardView.visibility = View.GONE
                    }else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                    }
                }

                "mixed_files" -> {

                    Log.d("clicked", "render: original post mixed files")
                    binding.mixedFilesCardView.visibility = View.VISIBLE
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: data files are empty")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                        binding.recyclerView2.visibility = View.VISIBLE
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding. mixedFilesCardView.visibility = View.GONE
                    }
                    // Display original post text if available
                    if (data.content.isNotEmpty()) {
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.content
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                    }


                    var adapter: FeedRepostViewFileAdapter? = null

                    if (data.originalPost.isNotEmpty()) {
                        val originalPost = data.originalPost[0]
                        val imageUrls = originalPost.files.map { it.url }
                        val adapter = FeedRepostViewFileAdapter(imageUrls, originalPost)
                        binding.recyclerView2.adapter = adapter
                    } else {
                        Log.e(TAG, "No OriginalPost available to display")
                        binding.recyclerView2.visibility = View.GONE
                    }

                    when (fileList.size) {
                        1 -> {
                            binding.recyclerView2.layoutManager = GridLayoutManager(
                                requireContext(), 1)
                            binding.recyclerView2.setHasFixedSize(true)
                            binding. recyclerView2.adapter = adapter
                        }

                        2 -> {
                            binding.recyclerView2.layoutManager =
                                GridLayoutManager(requireContext(), 2) // 2 columns in the grid
                            binding.recyclerView2.setHasFixedSize(true) // Ensures items won't change size
                            binding.recyclerView2.adapter = adapter // Replace with your adapter
                        }

                        3 -> {
                            // Use a GridLayoutManager with span size 2 for the first row and 1 for the second row
                            val layoutManager = GridLayoutManager(requireContext(), 2)
                            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return if (position < 2) 1 else 2
                                }
                            }
                            binding.recyclerView2.layoutManager = layoutManager
                            binding.recyclerView2.setHasFixedSize(true) // Ensures items won't change size
                            binding.recyclerView2.adapter = adapter // Replace with your adapter
                        }

                        else -> {
                            val layoutManager = GridLayoutManager(requireContext(), 2)

                            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                                override fun getSpanSize(position: Int): Int {
                                    return when (position) {
                                        0, 1 -> 1  // First and second items span 2 columns
                                        else -> 1   // All other items span 1 column
                                    }
                                }
                            }
                            binding.recyclerView2.layoutManager = layoutManager
                            binding.recyclerView2.adapter = adapter
                        }
                    }

                }

                "image" -> {
                    if (data.files.isNotEmpty()) {
                        Log.d("clicked", "render: image content")
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.originalFeedTextContent.text = data.originalPost[0].content
                        binding.originalFeedImage.visibility = View.VISIBLE

                        Glide.with(context)
                            .load(data.files[0].url) // Assuming the image URL is in `files[0].url`
                            .placeholder(R.drawable.flash21)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.originalFeedImage)

                    } else {
                        Log.d("clicked", "render: image content")
                        binding.originalFeedTextContent.visibility = View.GONE
                    }
                }

                "video" -> {
                    Log.d("clicked", "render: video content")
                    // Hide other views
                    if (data.files.isNotEmpty()) {
                        binding.originalFeedTextContent.visibility = View.VISIBLE
                        binding.mixedFilesCardView.visibility = View.VISIBLE
                        binding.multipleImagesContainer.visibility = View.VISIBLE
                    } else {
                        binding.originalFeedTextContent.visibility = View.GONE
                        binding.mixedFilesCardView.visibility = View.GONE
                        binding.multipleImagesContainer.visibility = View.GONE
                    }
                }
                else -> {
                }
            }
        }catch (e: Exception){
            Log.d("Exception", "onCreate: ${e.message}")
        }
    }

    private fun requireContext(): Context {
        TODO("Not yet implemented")
    }

}