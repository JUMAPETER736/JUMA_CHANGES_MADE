package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.User_Interface.Log_In_And_Register.RegisterActivity
import com.uyscuti.social.circuit.User_Interface.fragments.feed.feedviewfragments.editRepost.Fragment_Edit_Post_To_Repost.Companion.TAG
import com.uyscuti.social.circuit.model.FollowListItemViewModel
import com.uyscuti.social.circuit.model.LoadMoreShorts
import com.uyscuti.social.circuit.model.ShortsCacheEvent
import com.uyscuti.social.circuit.model.ShortsViewModel
import com.uyscuti.social.circuit.service.VideoPreLoadingService
import com.uyscuti.social.circuit.utils.Constants
import com.uyscuti.social.circuit.utils.removeDuplicateFollowers
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import retrofit2.HttpException
import java.io.IOException
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


    private var dialogsInserted = false

    private var logged by Delegates.notNull<Boolean>()


    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var retrofitInstance: RetrofitInstance

    private var chatIdList = ArrayList<String>()
    private var groupIdList = ArrayList<String>()
    private var shortsList = ArrayList<String>()

//    private lateinit var shortsViewModel: ShortsViewModel

    private val shortsViewModel: ShortsViewModel by viewModels()
    private val followShortsViewModel: FollowListItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val splashScreenAlreadyShown = settings.getBoolean(SPLASH_SCREEN_ALREADY_SHOWN, false)
        logged = settings.getBoolean("logged", false)
        EventBus.getDefault().register(this)


        dialogRepository = DialogRepository(
            ChatDatabase.Companion.getInstance(this).dialogDao(),
            retrofitInstance,
            localStorage
        )
        messageRepository =
            MessageRepository(
                this,
                ChatDatabase.Companion.getInstance(this).messageDao(),
                retrofitInstance
            )
        groupDialogRepository = GroupDialogRepository(
            ChatDatabase.Companion.getInstance(this).groupDialogDao(),
            retrofitInstance,
            localStorage
        )


        val database = ChatDatabase.Companion.getInstance(applicationContext)
        val personDao = database.shortsDao()
//        val repository = ShortsRepository(personDao)
//        shortsViewModel = ViewModelProvider(this)[ShortsViewModel::class.java]

//        shortsViewModel.allShorts.observe(this, Observer { persons ->
//            // Handle the updated list of persons
//
//        })

        Log.d(TAG, "onCreate token: ${LocalStorage.Companion.getInstance(this).getToken()}")
        lifecycleScope.launch {
//            getAllShort()
//            getAllShort2()
//            getAllShort3()
//            loadMoreShorts()

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

    private suspend fun getAllShort3(){
        Log.d("AllShorts3", "getAllShort2: In Get shorts 2")

        try {
            Log.d(TAG, "getAllShort3: In Get shorts 2")

            val response = retrofitInstance.apiService.getAllPosts3()

            Log.d("allShorts3", "getAllShort3: $response")
            Log.d("allShorts3", "getAllShort3: ${response.body()}")
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d("AllShorts3", "Shorts List ${responseBody?.data?.posts?.posts}")
                Log.d("AllShorts3", "Shorts List ${responseBody?.data?.followList}")


//                responseBody.data.posts.
//                val hasMoreShorts = responseBody!!.data.data.hasNextPage
//                Log.d("AllShorts2", "Has more shorts: $hasMoreShorts")

//                if(hasMoreShorts) {
////                    loadMoreShorts()
//                    EventBus.getDefault().post(LoadMoreShorts(true))
//                }
                val shortsEntity = responseBody!!.data.posts.posts.let { serverResponseToEntity(it) }
                val followListItem = responseBody.data.followList.let { serverResponseToFollowEntity(it) }
//                val followListItem = responseBody.data.followList

// Now, insert yourEntity into the Room database
                lifecycleScope.launch(Dispatchers.IO) {
                    shortsViewModel.addAllShorts(shortsEntity)
                    val uniqueFollowList = removeDuplicateFollowers(followListItem)
                    Log.d(TAG, "getAllShort3: Inserted uniqueFollowList $uniqueFollowList")
                    followShortsViewModel.insertFollowListItems(uniqueFollowList)

//                    followShortsViewModel.insertFollowListItems(followListItem)
//                    shortsViewModel.addAllFollowListShorts(followListItem)

                    for (entity in shortsEntity) {
                        // Access the list of images for each entity
                        val images = entity.images

                        // Iterate through the list of images
                        for (image in images) {
                            // Access individual image properties or perform any desired actions
                            val imageUrl = image.url
//                                Log.d(TAG, "imageUrl - $imageUrl")
                            shortsList.add(imageUrl)
//                                EventBus.getDefault().post(ShortsCacheEvent(shortsList))

                            // Do something with the imageUrl...
                        }
                    }
                    startPreLoadingService()
                }
//                Log.d(TAG, "Handle the updated list of persons")
//                shortsViewModel.addAllShorts(personList)

            } else {
                Log.d("AllShorts3", "Error: ${response.message()}")
                showToast(response.message())
            }

        } catch (e: HttpException) {
            Log.d("AllShorts3", "Http Exception ${e.message}")
            Toast.makeText(this, "Failed to connect try again...", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.d("AllShorts3", "IOException ${e.message}")
                Toast.makeText(
                    this@SplashScreen,
                    "Failed to connect try again...",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

//    private suspend fun loadMoreShorts(){
//        Log.d("AllShorts", "Loading more shorts")
//        val nextPage = 2
//        try {
//            Log.d("AllShorts", "Loading more shorts in try block")
//
//            val response = retrofitInstance.apiService.getShorts(nextPage.toString())
//
//            Log.d("All shorts", "response page 2 message: ${response.message()}")
//            if (response.isSuccessful) {
//                val responseBody = response.body()
//                Log.d("AllShorts", "Shorts List in page 2 ${responseBody?.data}")
//
//
//                val shortsEntity = responseBody?.data?.posts?.let { serverResponseToEntity(it) }
//
//// Now, insert yourEntity into the Room database
//                if (shortsEntity != null) {
//
//
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        shortsViewModel.addAllShorts(shortsEntity)
//
//                        for (entity in shortsEntity) {
//                            // Access the list of images for each entity
//                            val images = entity.images
//
//                            // Iterate through the list of images
//                            for (image in images) {
//                                // Access individual image properties or perform any desired actions
//                                val imageUrl = image.url
////                                Log.d(SHORTS, "imageUrl - $imageUrl")
//                                shortsList.add(imageUrl)
////                                EventBus.getDefault().post(ShortsCacheEvent(shortsList))
//
//                                // Do something with the imageUrl...
//                            }
//                        }
////                        viewPager.offscreenPageLimit = 21
//                        startPreLoadingService()
//                        withContext(Dispatchers.Main) {
////                            shortsAdapter.addData(shortsEntity)
//                        }
//
//                    }
////
//                    Log.d(SHORTS, "Data from page 2 added to local database - $shortsEntity")
//                }else {
//                    Log.d(SHORTS, "failed to add shorts to local database")
//                }
////                Log.d(SHORTS, "Handle the updated list of persons")
////                shortsViewModel.addAllShorts(personList)
//
//            } else {
//                Log.d("AllShorts", "Error: ${response.message()}")
//                showToast(response.message())
//            }
//
//        } catch (e: HttpException) {
//            Log.d("AllShorts", "Http Exception ${e.message}")
//            showToast("Failed to connect try again...")
//        } catch (e: IOException) {
//            Log.d("AllShorts", "IOException ${e.message}")
//            showToast("Failed to connect try again...")
//        }
//    }


    @OptIn(UnstableApi::class)
    private fun startPreLoadingService() {
        val preloadingServiceIntent = Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(Constants.VIDEO_LIST, shortsList)
        startService(preloadingServiceIntent)
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
//                    Log.d("FetchedDialogs", "FetchedDialogs : ${chatIdList.size}")

                    firstLaunch = false
                    // Uncomment the line below if you want to delay starting the main activity
//                    val handler = Handler()
//                    handler.postDelayed({ startMainActivity() }, 2000)

                    startMainActivity()


                    if (chatIdList.isNotEmpty()) {
                        chatIdList.forEach { dialogId ->
                            fetchMessages(dialogId)
                        }
                    }


                    // Comment out the line below if you want to delay starting the main activity
//                    startMainActivity()

                },
                onFailure = { errorMessage ->
                    // Code to execute on failure
                    Log.e("FetchedDialogs", errorMessage)
                    // Handle failure as needed
                    firstLaunch = false

                    startMainActivity()

//                    val handler = Handler()
//                    handler.postDelayed({ startMainActivity() }, 2000)
                }
            )

            fetchGroups(
                onSuccess = {
                    // Code to execute on success
//                    Log.d("FetchedGroups", "FetchedGroups : ${groupIdList.size}")

                    firstLaunch = false
                    // Uncomment the line below if you want to delay starting the main activity
//                    val handler = Handler()
//                    handler.postDelayed({ startMainActivity() }, 2000)

//                    startMainActivity()


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

//                    startMainActivity()

                }
            )

        } else {
            startMainActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

//        shortsViewModel.allShorts.observe(this) { entities ->
//
//            if(entities != null) {
//                for (url in entities) {
////                Log.d("Shorts", "Shorts in adapter: ${url.url}")
////                // Add the URL to the list
////                urlList.add(url.url)
////                startPreLoadingService()
////                initializePlayer()
//            }
//            }
//        }
    }

    private fun fetchDialogs(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (isUserAuthenticated()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
//                    Log.d("FetchedDialogs", "Fetching dialogs from the internet")
                    dialogRepository.fetchAndInsertPersonalDialogs() // Initiate fetching and inserting dialogs
//                    Log.d("FetchedDialogs", "Fetched dialogs from the internet successfully")

                    val list = dialogRepository.dialogIds()
                    chatIdList += list as ArrayList<String>
//                    Log.d("FetchedDialogs", "The number of chats fetched : ${chatIdList.size}")

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
//                    Log.d("FetchedGroups", "Fetching groups from the internet")
                    groupDialogRepository.fetchAndInsertGroupDialogs() // Initiate fetching and inserting groups
//                    Log.d("FetchedGroups", "Fetched groups from the internet successfully")

                    val list = groupDialogRepository.dialogIds()
                    groupIdList = list as ArrayList<String>
//                    Log.d("FetchedGroups", "The number of groups fetched : ${chatIdList.size}")

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
//                    Log.d("FetchedMessages", "Fetching group messages from the internet")
                    messageRepository.getMessagesWithMediaType(groupId)
//                    Log.d("FetchedMessages", "Fetched group messages from the internet successfully")
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
//                    Log.d("FetchedMessages", "Fetching messages from the internet")
                    messageRepository.getMessagesWithMediaType(chatId)
//                    Log.d("FetchedMessages", "Fetched messages from the internet successfully")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getToken(): String {
        return localStorage.getToken()
    }

    private fun launchMainActivityIfNeeded() {
        if (dialogsInserted) {
            if (firstLaunch) {
                val handler = Handler()
                handler.postDelayed({
                    if (isUserAuthenticated()) {
                        launchMain()
                    } else {
                        startMainActivity()
                    }
                    firstLaunch = false
                }, 200)
            } else {
                if (isUserAuthenticated()) {
                    launchMain()
                } else {
                    startMainActivity()
                }
            }
        }
    }

    private fun isUserAuthenticated(): Boolean {
        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return settings.getBoolean("logged", false) && getToken().isNotEmpty()
    }


    private fun startMainActivity() {
        val set = getSharedPreferences(PREFS_NAME, 0)
        val splashScreenAlreadyShown = set.getBoolean(SPLASH_SCREEN_ALREADY_SHOWN, false)
        val logged = set.getBoolean("logged", false)


        if (!isUserAuthenticated()) {
            val intent = Intent(this@SplashScreen, RegisterActivity::class.java)

            // Apply custom transitions
            val slideInRight =
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
//                loadMoreShorts()
            }
        }
    }
}