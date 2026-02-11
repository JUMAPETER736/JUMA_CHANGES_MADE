package com.uyscuti.social.circuit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.uyscuti.sharedmodule.model.FollowListItemViewModel
import com.uyscuti.sharedmodule.model.LoadMoreShorts
import com.uyscuti.sharedmodule.model.ShortsCacheEvent
import com.uyscuti.sharedmodule.model.ShortsViewModel
import com.uyscuti.sharedmodule.viewmodels.feed.UserRelationshipsViewModel
import com.uyscuti.social.circuit.log_in_and_register.RegisterActivity
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.ShortsEntity
import com.uyscuti.social.core.common.data.room.entity.ShortsEntityFollowList
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.core.common.data.room.repository.GroupDialogRepository
import com.uyscuti.social.core.common.data.room.repository.MessageRepository
import com.uyscuti.social.network.api.response.getallshorts.FollowListItem
import com.uyscuti.social.network.api.response.getallshorts.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    private val PREFS_NAME = "LocalSettings" // Change this to a unique name for your app

    private val SPLASH_SCREEN_ALREADY_SHOWN = "splashScreenAlreadyShown"

    private var appInBackground = true // Default to true

    private var firstLaunch = true // Default to true

    private lateinit var dialogRepository: DialogRepository
    private lateinit var groupDialogRepository: GroupDialogRepository
    private lateinit var messageRepository: MessageRepository
    private val relationshipsViewModel: UserRelationshipsViewModel by viewModels()

    private val TAG = "OnSplashScreen"


    private var dialogsInserted = false

    private var logged by Delegates.notNull<Boolean>()


    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private var chatIdList = ArrayList<String>()
    private var groupIdList = ArrayList<String>()
    private var shortsList = ArrayList<String>()

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val followShortsViewModel: FollowListItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val splashScreenAlreadyShown = settings.getBoolean(SPLASH_SCREEN_ALREADY_SHOWN, false)

        logged = settings.getBoolean("logged", false)
        EventBus.getDefault().register(this)

        if (isUserAuthenticated()) {
            loadUserRelationships()
        }

        dialogRepository = DialogRepository(
            ChatDatabase.Companion.getInstance(this).dialogDao(),
            retrofitInstance,
            localStorage
        )
        messageRepository =
            MessageRepository(
                ChatDatabase.Companion.getInstance(this).messageDao(),
                retrofitInstance
            )
        groupDialogRepository = GroupDialogRepository(
            ChatDatabase.Companion.getInstance(this).groupDialogDao(),
            retrofitInstance
        )


        val database = ChatDatabase.Companion.getInstance(applicationContext)
        val personDao = database.shortsDao()


        Log.d(TAG, "onCreate token: ${LocalStorage.Companion.getInstance(this).getToken()}")
        lifecycleScope.launch {


        }
    }

    // ADD THIS NEW METHOD - Load all user relationships
    private fun loadUserRelationships() {
        Log.d(TAG, "Loading user relationships in SplashScreen...")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                relationshipsViewModel.loadAllRelationships()

                withContext(Dispatchers.Main) {
                    // Observe loading completion
                    relationshipsViewModel.isLoading.collect { isLoading ->
                        if (!isLoading) {
                            Log.d(TAG, "Relationships loaded successfully")
                            Log.d(TAG, "Close Friends: ${relationshipsViewModel.closeFriendIds.value.size}")
                            Log.d(TAG, "Muted Posts: ${relationshipsViewModel.mutedPostsIds.value.size}")
                            Log.d(TAG, "Muted Stories: ${relationshipsViewModel.mutedStoriesIds.value.size}")
                            Log.d(TAG, "Favorites: ${relationshipsViewModel.favoriteIds.value.size}")
                            Log.d(TAG, "Restricted: ${relationshipsViewModel.restrictedIds.value.size}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading relationships: ${e.message}", e)
            }
        }
    }

    private fun serverResponseToEntity(serverResponse: List<Post>): List<ShortsEntity> {
        return serverResponse.map { serverResponseItem ->
            ShortsEntity(
                __v = serverResponseItem.__v,
                _id = serverResponseItem._id,
                content = serverResponseItem.content,
                author = serverResponseItem.author,
                comments = serverResponseItem.comments,
                createdAt = serverResponseItem.createdAt,
                images = serverResponseItem.images,
                isBookmarked = serverResponseItem.isBookmarked,
                isLiked = serverResponseItem.isLiked,
                likes = serverResponseItem.likes,
                tags = serverResponseItem.tags,
                updatedAt = serverResponseItem.updatedAt,
                thumbnail = serverResponseItem.thumbnail
                // map other properties...
            )
        }
    }

    private fun serverResponseToFollowEntity(serverResponse: List<FollowListItem>) : List<ShortsEntityFollowList> {
        return serverResponse.map{serverResponse ->
            ShortsEntityFollowList(
                followersId = serverResponse.followersId,
                isFollowing = serverResponse.isFollowing
            )
        }
    }


    @OptIn(UnstableApi::class)
    private fun launchMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onStop() {
        super.onStop()
        appInBackground = true
    }

    override fun onResume() {
        super.onResume()
        if (firstLaunch) {
            fetchDialogs(
                onSuccess = {
                    // Code to execute on success
                    firstLaunch = false
                   startMainActivity()

                    if (chatIdList.isNotEmpty()) {
                        chatIdList.forEach { dialogId ->
                            fetchMessages(dialogId)
                        }
                    }

                },
                onFailure = { errorMessage ->
                    // Code to execute on failure
                    Log.e("FetchedDialogs", errorMessage)
                    // Handle failure as needed
                    firstLaunch = false

                    startMainActivity()
                }
            )

            fetchGroups(
                onSuccess = {
                    // Code to execute on success


                    firstLaunch = false
                    // Uncomment the line below if you want to delay starting the main activity

                    if (groupIdList.isNotEmpty()) {
                        groupIdList.forEach { dialogId ->
                            fetchGroupMessages(dialogId)
                        }
                    }
                },
                onFailure = { errorMessage ->
                    Log.e("FetchedGroups", errorMessage)
                    // Handle failure as needed
                    firstLaunch = false


                }
            )

        } else {
            startMainActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

    }

    private fun fetchDialogs(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (isUserAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    dialogRepository.fetchAndInsertPersonalDialogs() // Initiate fetching and inserting dialogs


                    val list = dialogRepository.dialogIds()
                    chatIdList += list as ArrayList<String>


                    // Call the success callback
                    onSuccess.invoke()

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Call the failure callback with the error message
                    onFailure.invoke("Failed to fetch dialogs: ${e.message}")
                }
            }
        } else {
            onFailure.invoke("Not authenticated")
        }
    }


    private fun fetchGroups(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (isUserAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    groupDialogRepository.fetchAndInsertGroupDialogs() // Initiate fetching and inserting groups


                    val list = groupDialogRepository.dialogIds()
                    groupIdList = list as ArrayList<String>


                    // Call the success callback
                    onSuccess.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Call the failure callback with the error message
                    onFailure.invoke("Failed to fetch groups: ${e.message}")
                }
            }
        } else {
            onFailure.invoke("Not authenticated")
        }
    }

    private fun fetchGroupMessages(groupId: String) {
        if (isUserAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    messageRepository.getMessagesWithMediaType(groupId)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun fetchMessages(chatId: String) {
        if (isUserAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                     messageRepository.getMessagesWithMediaType(chatId)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getToken(): String {
        return localStorage.getToken()
    }

    private fun isUserAuthenticated(): Boolean {
        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getBoolean("logged", false) && getToken().isNotEmpty()
    }


    private fun startMainActivity() {
        val set = getSharedPreferences(PREFS_NAME, 0)
        set.getBoolean(SPLASH_SCREEN_ALREADY_SHOWN, false)
        set.getBoolean("logged", false)


        if (!isUserAuthenticated()) {
            val intent = Intent(this@SplashScreen, RegisterActivity::class.java)

            // Apply custom transitions
            AnimationUtils.loadAnimation(this@SplashScreen, R.anim.slide_in_right)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            overridePendingTransition(0, 0) // Disable the default transition
            this@SplashScreen.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )


            // Mark the splash screen as already shown in shared preferences
            val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val editor = settings.edit()
            editor.putBoolean(SPLASH_SCREEN_ALREADY_SHOWN, true)
            editor.apply()
            finish() // Finish the splash screen activity
        } else {
            launchMain()
        }
    }

    @Subscribe
    fun onEvent(event: ShortsCacheEvent) {
        // Handle the event here
        Log.d("EventBus", "Received event: ${event.videoPath}")
    }
    @Subscribe
    fun onLoadMoreShortsEvent(event: LoadMoreShorts) {
        // Handle the event here
        Log.d("EventBus", "Received event: ${event.loadMore}")
        if(event.loadMore) {
            lifecycleScope.launch(Dispatchers.IO) {

            }
        }
    }
}