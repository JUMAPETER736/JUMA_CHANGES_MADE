package com.uyscuti.social.business.adapter.business.postViewHolder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonSyntaxException
import com.uyscuti.sharedmodule.OtherUserProfile
import com.uyscuti.sharedmodule.RepostBusinessPost
import com.uyscuti.sharedmodule.User_Interfaces.OtherUserProfile.OtherUserProfileAccount
import com.uyscuti.sharedmodule.data.model.User
import com.uyscuti.sharedmodule.data.model.shortsmodels.OtherUsersProfile
import com.uyscuti.sharedmodule.model.GoToUserProfileFragment
import com.uyscuti.sharedmodule.utils.formatCount
import com.uyscuti.sharedmodule.utils.formattedMongoDateTime
import com.uyscuti.social.business.CatalogueDetailsActivity
import com.uyscuti.social.business.R
import com.uyscuti.sharedmodule.adapter.BusinessMediaViewPager
import com.uyscuti.sharedmodule.bottomSheet.SendOfferBottomSheet
import com.uyscuti.sharedmodule.databinding.BottomDialogForShareBinding
import com.uyscuti.social.business.adapter.business.OnBusinessClickedListener
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Date

class BusinessPostViewHolder(
    itemView: View,
    private val context: Activity,
    private var businessClickedListener: OnBusinessClickedListener,
    private val retrofitInterface: RetrofitInstance,
    private val localStorage: LocalStorage,
    private val onItemClick: (Post) -> Unit = {},
    private val onBookmarkClick: (Post) -> Unit = { _ -> },
    private val onFollowClick: (Post) -> Unit = { _ -> },
    private val onMessageClick: (User, Post) -> Unit = { _ , _-> },
    private val fragmentManager: FragmentManager
) : RecyclerView.ViewHolder(itemView) {
    // User header elements
    val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
    val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
    val tvPostTime: TextView = itemView.findViewById(R.id.tv_post_time)
    val btnFollow: AppCompatButton = itemView.findViewById(R.id.btn_follow)

    // Item info elements
    val tvItemTitle: TextView = itemView.findViewById(R.id.tv_item_title)
    val tvItemPrice: TextView = itemView.findViewById(R.id.tv_item_price)
    val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
    val tvShowMore: TextView = itemView.findViewById(R.id.tv_show_more)

    // Media elements
    val recyclerView: RecyclerView = itemView.findViewById(R.id.business_recycler)
    val tvMediaCounter: TextView = itemView.findViewById(R.id.tv_media_counter)

    // Interaction buttons
    val buLike: ImageView = itemView.findViewById(R.id.bu_like)
    val buLikeCount: TextView = itemView.findViewById(R.id.bu_like_count)

    //comment button
    val buComment: ImageView = itemView.findViewById(R.id.bu_comment)
    val buCommentCount: TextView = itemView.findViewById(R.id.bu_comment_count)


    val buBookMark: ImageView = itemView.findViewById(R.id.bu_book_mark)
    val buBookMarkCount: TextView = itemView.findViewById(R.id.bu_book_mark_count)

    val sendOffer: LinearLayout = itemView.findViewById(R.id.send_offer)

    val buShare: ImageView = itemView.findViewById(R.id.bu_share)
    val buShareCount: TextView = itemView.findViewById(R.id.share_count)

    val message: RelativeLayout = itemView.findViewById(R.id.message_seller)
    val messageText: TextView = itemView.findViewById(R.id.message_user)

    val catalogueInfo: LinearLayout = itemView.findViewById(R.id.catalogueInfo)

    val userHeader: LinearLayout = itemView.findViewById(R.id.user_header)

    private var isExpanded = false
    private var originalText = ""

    @OptIn(UnstableApi::class)
    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun bind(item: Post, position: Int) {

        originalText = item.description
        setupExpandableDescription()

        // Bind user info
        tvUsername.text = "@${item.userDetails.username}"

        tvPostTime.text = formattedMongoDateTime(item.createdAt)

        buBookMarkCount.text = item.bookmarkCount.toString()
        buCommentCount.text = item.comments.toString()
        buLikeCount.text = item.likes.toString()
        buShareCount.text = "0"

        // Load user avatar
        Glide.with(context)
            .load(item.userDetails.avatar)
            .circleCrop()
            .into(ivUserAvatar)

        // Bind item info
        tvItemTitle.text = item.itemName
        tvItemPrice.text = "MWK ${item.price}"
        tvDescription.text = item.description
        messageText.text = "Message @${item.userDetails.username}"

        // Setup media ViewPager
        if (item.images.isNotEmpty()) {
            val businessMediaViewPager = BusinessMediaViewPager(
                context,
                item.images,
                onItemClicked = { position ->
                    navigateToItemDetail(item, position)
                }
            )

            // Setup RecyclerView layout based on file count
            recyclerView.layoutManager = when (item.images.size) {
                1 -> GridLayoutManager(context, 1)
                2 -> {
                    GridLayoutManager(context, 2)
                }
                3, 4, 5 -> {
                    StaggeredGridLayoutManager(
                        2,
                        StaggeredGridLayoutManager.VERTICAL
                    ) }

                else -> GridLayoutManager(context, 2)
            }
            recyclerView.hasFixedSize()
            recyclerView.adapter = businessMediaViewPager
        }

        if(item.images.size > 4) {

            val fileSize = item.images.size

            // Set the "+N" text
            tvMediaCounter.text = "+${fileSize - 4}"
            tvMediaCounter.textSize = 32f
            tvMediaCounter.setTextColor(Color.WHITE)
            tvMediaCounter.setTypeface(null, Typeface.NORMAL)

            // Create rounded dimmed background
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f // Rounded corners
                setColor(Color.parseColor("#80000000")) // Semi-transparent black
            }

            tvMediaCounter.background = background
            tvMediaCounter.visibility = View.VISIBLE
        } else {
            tvMediaCounter.visibility = View.GONE
        }


        updateBookmarkButton(item.isBookmarked)

        // Set click listeners
        catalogueInfo.setOnClickListener { onItemClick(item) }

        userHeader.setOnClickListener { onItemClick(item) }

        ivUserAvatar.setOnClickListener {
            if (item.owner == localStorage.getUserId()){
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                val otherUsersProfile = OtherUsersProfile(
                    item.userDetails.username, item.userDetails.username,
                    item.userDetails.avatar, item.owner
                )

                OtherUserProfileAccount.open(
                    ivUserAvatar.context,
                    otherUsersProfile,
                    item.userDetails.avatar,
                    item.owner
                )
            }
        }

        tvUsername.setOnClickListener {
            if (item.owner == localStorage.getUserId()){
                EventBus.getDefault().post(GoToUserProfileFragment())
            } else {
                val otherUsersProfile = OtherUsersProfile(
                    item.userDetails.username, item.userDetails.username,
                    item.userDetails.avatar, item.owner
                )

                OtherUserProfileAccount.open(
                    tvUsername.context,
                    otherUsersProfile,
                    item.userDetails.avatar,
                    item.owner
                )
            }
        }

        message.setOnClickListener {
            val user = User(
                item.owner,
                item.userDetails.username,
                item.userDetails.avatar,
                false,
                Date()
            )
            onMessageClick(user, item)
        }

        btnFollow.setOnClickListener {
            onFollowClick(item)
            setUpFollowButton(item)
        }

        buBookMark.setOnClickListener {
            setUpBookmarkedButton(item)
            onBookmarkClick(item)
            buBookMark.alpha = 1f
            buBookMark.isEnabled = true
        }

        sendOffer.setOnClickListener {
            showSendOfferBottomSheet(item)
        }

        buLike.setOnClickListener {
            setUpLikeButton(item)
        }

        setCommentButton(position, item)
        setUpShareButton(item)
        updateLikeButton(item.isLiked)

        if(localStorage.getUsername() == item.userDetails.username)
            btnFollow.visibility = View.GONE
        else
            updateFollowButton(item.isFollowing)

    }

    private fun animateButton(button: View) {
        YoYo.with(Techniques.Tada)
            .duration(700)
            .repeat(1)
            .playOn(button)
    }



    @OptIn(UnstableApi::class)
    private fun navigateToItemDetail(item: Post, position: Int) {
        // Navigate to item detail screen
        val intent = Intent(context, CatalogueDetailsActivity::class.java)
        intent.putExtra("catalogue", item)
        intent.putExtra("position", position)
        context.startActivity(intent)
    }

    private fun showSendOfferBottomSheet(data: Post) {
        val bottomSheet = SendOfferBottomSheet.newInstance(
            productName = data.itemName.toString(),
            listingPrice = data.price.toDouble(),
            itemImage = data.images.first()
        )

        // Set callback for when offer is submitted
        bottomSheet.onOfferSubmitted = { amount, message ->
            // Handle the submitted offer
            Log.d("Offer", "Amount: MWK$amount, Message: $message")

        }

        bottomSheet.show(fragmentManager, "SendOfferBottomSheet")
    }


    private fun setupExpandableDescription() {
        isExpanded = false
        tvDescription.maxLines = Integer.MAX_VALUE
        tvDescription.text = originalText

        // Post to ensure the TextView is laid out
        tvDescription.post {
            val layout = tvDescription.layout
            if (layout != null && layout.lineCount > 3) {
                // Text needs truncation
                tvShowMore.visibility = View.VISIBLE
                collapseText()
            } else {
                // Text fits in the allowed lines
                tvShowMore.visibility = View.GONE
            }
        }

        tvShowMore.setOnClickListener {
            if (isExpanded) {
                collapseText()
            } else {
                expandText()
            }
        }
    }

    private fun collapseText() {
        tvDescription.maxLines = 3
        tvDescription.ellipsize = TextUtils.TruncateAt.END
        tvShowMore.text = "Show more"
        isExpanded = false
    }

    private fun expandText() {
        tvDescription.maxLines = Integer.MAX_VALUE
        tvDescription.ellipsize = null
        tvShowMore.text = "Show less"
        isExpanded = true
    }

    private fun setCommentButton(position: Int, post: Post) {
        buComment.setOnClickListener {
            animateButton(buComment)
            businessClickedListener.businessCommentClickedListener(
                position,
                post
            )
        }
    }

    private fun setUpShareButton(data: Post) {

        val bottomSheetDialog = BottomSheetDialog(context)
        val binding = BottomDialogForShareBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(binding.root)

        // Prepare share content
        val shareText = "Check out this product on Flash!\n" +
                "By: ${data.userDetails.username}\n" +
                (data.itemName)
        val postUrl = data.images.firstOrNull() ?: data.images.size
        val fullShareText = if (true) "$shareText\n$postUrl" else shareText


        // Setup share buttons
        binding.btnWhatsApp.setOnClickListener {
            shareToWhatsApp(context, fullShareText)
           // incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnSMS.setOnClickListener {
            shareViaSMS(context, fullShareText)
           // incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnInstagram.setOnClickListener {
            shareToInstagram(context, fullShareText)
          //  incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnMessenger.setOnClickListener {
            shareToMessenger(context, fullShareText)
         //   incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnFacebook.setOnClickListener {
            shareToFacebook(context, fullShareText)
          //  incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        binding.btnTelegram.setOnClickListener {
            shareToTelegram(context, fullShareText)
          //  incrementShareCount(data)
            bottomSheetDialog.dismiss()
        }

        // Setup action buttons
        binding.btnReport.setOnClickListener {
            Toast.makeText(context, "Report functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        binding.btnNotInterested.setOnClickListener {
            Toast.makeText(context, "Not interested", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        binding.btnSaveVideo.setOnClickListener {
            Toast.makeText(context, "Save post functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        binding.btnDuet.setOnClickListener {
            Toast.makeText(context, "Duet functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        binding.btnReact.setOnClickListener {
            Toast.makeText(context, "React functionality", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        binding.btnAddToFavorites.setOnClickListener {
            Toast.makeText(context, "Add to favorites", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // Setup cancel button
        binding.btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        buShare.setOnClickListener { bottomSheetDialog.show() }




    }

    // Share helper functions with multiple package name variants
    private fun shareToWhatsApp(context: Context, text: String) {
        val packages = listOf(
            "com.whatsapp",
            "com.whatsapp.w4b"
        )
        shareToApp(context, text, packages, "WhatsApp")
    }

    private fun shareViaSMS(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "smsto:".toUri()
                putExtra("sms_body", text)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS app not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareToInstagram(context: Context, text: String) {
        val packages = listOf(
            "com.instagram.android"
        )
        shareToApp(context, text, packages, "Instagram")
    }

    private fun shareToMessenger(context: Context, text: String) {
        val packages = listOf(
            "com.facebook.orca",
            "com.facebook.mlite"
        )
        shareToApp(context, text, packages, "Messenger")
    }

    private fun shareToFacebook(context: Context, text: String) {
        val packages = listOf(
            "com.facebook.katana",
            "com.facebook.lite"
        )
        shareToApp(context, text, packages, "Facebook")
    }

    private fun shareToTelegram(context: Context, text: String) {
        val packages = listOf(
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "org.thunderdog.challegram"
        )
        shareToApp(context, text, packages, "Telegram")
    }


    // Generic function to try multiple package names
    private fun shareToApp(context: Context, text: String, packages: List<String>, appName: String) {
        try {
            for (packageName in packages) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(packageName)
                    putExtra(Intent.EXTRA_TEXT, text)
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            }

            Toast.makeText(context, "$appName not installed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "$appName not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFollowButton(isFollowing: Boolean) {
        if(isFollowing)
            btnFollow.visibility = View.GONE
    }

    private fun updateBookmarkButton(isBookmarked: Boolean) {
        buBookMark.setImageResource(
            if (isBookmarked) R.drawable.filled_favorite else R.drawable.favorite_svgrepo_com__1_
        )
    }



    private fun updateLikeButton(isLiked: Boolean) {
        buLike.setImageResource(
            if (isLiked) R.drawable.filled_favorite_like
            else R.drawable.heart_svgrepo_com
        )
    }

    private fun setUpFollowButton(post: Post) {
        val newFollowStatus = !post.isFollowing

        YoYo.with(if (newFollowStatus) Techniques.Tada else Techniques.Pulse)
            .duration(300)
            .playOn(btnFollow)

        updateFollowButton(newFollowStatus)
    }

    private fun setUpBookmarkedButton(post: Post) {
        val newBookmarkStatus = !post.isBookmarked
        post.isBookmarked = newBookmarkStatus

        post.bookmarkCount = if(newBookmarkStatus) post.bookmarkCount + 1 else maxOf(
            0,
            post.bookmarkCount - 1
        )

        buBookMarkCount.text = formatCount(post.bookmarkCount)
        updateBookmarkButton(newBookmarkStatus)

        YoYo.with(if (newBookmarkStatus) Techniques.Tada else Techniques.Pulse)
            .duration(300)
            .playOn(buBookMark)

        buBookMark.isEnabled = false

    }

    private fun setUpLikeButton(post: Post) {
        val newLikedStatus = !post.isLiked
        post.isLiked = newLikedStatus

        post.likes = if(newLikedStatus) post.likes + 1 else maxOf(
            0,
            post.likes - 1
        )

        buLikeCount.text = formatCount(post.likes)
        updateLikeButton(newLikedStatus)

        // Add animation for feedback
        YoYo.with(if (newLikedStatus) Techniques.Tada else Techniques.Pulse)
            .duration(300)
            .playOn(buLike)

        buLike.isEnabled = false

        val businessPostId = post._id

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val likeResponse = retrofitInterface.apiService.likeAndUnlikeBusinessPost(businessPostId.toString())

                if (likeResponse.isSuccessful && likeResponse.body() != null) {
                    withContext(Dispatchers.Main) {
                        buLike.alpha = 1f
                        buLike.isEnabled = true
                    }
                }
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}