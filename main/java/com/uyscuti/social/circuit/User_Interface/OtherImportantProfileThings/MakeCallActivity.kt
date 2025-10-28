package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.call.models.DataModel
import com.uyscuti.social.call.models.DataModelType
import com.uyscuti.social.call.repository.MainRepository
import com.uyscuti.social.call.ui.CallActivity
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.CallUsersListAdapter
import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityMakeCallBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.UsersViewModel
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MakeCallActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMakeCallBinding
    private lateinit var userListView: RecyclerView
    private lateinit var userListAdapter: CallUsersListAdapter
    private lateinit var originalUserList: List<User>

    private lateinit var usersList: List<UserEntity>

    private val usersViewModel: UsersViewModel by viewModels()
    private val dialogViewModel: DialogViewModel by viewModels()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    @Inject
    lateinit var mainRepository: MainRepository

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var callViewModel: CallViewModel


    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]

        username = localStorage.getUsername()

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {
            // No internet connection, get data from room
            return
        } else {
//            fetchUsers()
        }

        dialogViewModel.allDialogs.observe(this) { chats ->
            usersList = chats.map { it.toUser() }
            runOnUiThread {
//                Log.d("UsersList", "$usersList")
                initUserList()
            }
        }

//        CoroutineScope(Dispatchers.Main).launch {
//            usersViewModel.users.observe(this@MakeCallActivity) { users ->
//                usersList = users
//                runOnUiThread {
//                    Log.d("UsersList", "$usersList")
//                    initUserList()
//                }
//            }
//        }

        supportActionBar?.title = "Call"
//        initUserList()
    }



    private fun fetchUsers() {
        GlobalScope.launch {
            val response = try {

                retrofitInterface.apiService.getUsers()
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MakeCallActivity,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@MakeCallActivity,
                        "Network error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            } finally {
                // Ensure the progress bar is hidden in case of an error
//                withContext(Dispatchers.Main) {
//                    dismissLoadingDialog()
//                }
            }

            if (response.isSuccessful) {
                val responseBody = response.body()
//                Log.d("AllUsers", "Users List ${responseBody?.data}")

                if (responseBody?.data != null) {

                    val allUsers = mutableListOf<UserEntity>()

                    for (user in responseBody.data) {
                        val userEntity = UserEntity(
                            id = user._id,
                            name = user.username,
                            avatar = user.avatar.url,
                            online = false,
                            lastSeen = Date()
                        )

                        allUsers.add(userEntity)

                    }
                    allUsers.forEach {
                        insertUser(it)
//                        Log.d("Add users", "All users added successfully to room")
                    }


                } else {
                    Log.d("RetrofitActivity", "Response body or data is null")
                }
            }

        }
    }

    private fun DialogEntity.toUser(): UserEntity {
        return UserEntity(
            id = this.id,
            name = dialogName,
            avatar = dialogPhoto,
            lastSeen = Date(),
            online = true
        )
    }

    private fun insertUser(user: UserEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            usersViewModel.addUsers(user)
        }
    }

    private fun initUserList() {
        userListView = binding.userList
        userListAdapter = CallUsersListAdapter(this) { user, video ->
//            showToast(video, user.name)
            if (video) {
                startVideoCall(user)
            } else {
                startVoiceCall(user)
            }
        }

        // Set LinearLayoutManager for a vertical list
        userListView.layoutManager = LinearLayoutManager(this)

        originalUserList = usersList.map { it.toUser() }.sortedBy { it.name }

        val converted = usersList.map { it.toUser() }

        val sorted = converted.sortedBy { it.name }

        userListAdapter.setUserList(originalUserList)
        userListView.adapter = userListAdapter
    }

    private fun showToast(video: Boolean, friend: String) {
        val type = if (video) "Making Video Call to $friend" else "Making Audio Call to $friend"

        Toast.makeText(this, type, Toast.LENGTH_SHORT).show()
    }


    private fun UserEntity.toUser(): User {
        return User(
            id,
            name,
            avatar,
            online,
            lastSeen
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)

        // Get the search item
        val searchItem = menu.findItem(R.id.menu_search)

        // Get the SearchView from the search item
        val searchView = searchItem.actionView as SearchView

        // Set the query text listener to this activity
        searchView.setOnQueryTextListener(this)

        // Tint the search icon with white color
//        tintMenuIcon(searchItem, Color.WHITE)
        return true
    }

//    private fun setupSearchView() {
//        val searchView = findViewById<SearchView>(R.id.searchView)
//
//        // Customize the SearchView
//        searchView.queryHint = "Search Users"
//        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_button)
//        searchIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
//        val closeButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
//        closeButton.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                // Filter the user list based on the search query
//                val filteredList = originalUserList.filter { user ->
//                    user.name.contains(newText.orEmpty(), true)
//                }
//                userListAdapter.setUserList(filteredList)
//                return true
//            }
//        })
//    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                val searchView = item.actionView as SearchView
                searchView.isIconified = false // Expand the SearchView when the icon is clicked
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        // Handle the submission of the search query if needed
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // Filter the user list based on the search query
        val filteredList = originalUserList.filter { user ->
            user.name.contains(newText.toString(), true)
        }
        userListAdapter.setUserList(filteredList)
        return true
    }


    private fun startVoiceCall(user: User) {
        mainRepository.sendConnectionRequest(
            DataModel(
                DataModelType.StartVoiceCall, username, user.name, null
            )
        ) {
            if (it) {
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra("target", user.name)
                    putExtra("isVideoCall", false)
                    putExtra("isCaller", true)
                    putExtra("avatar", user.avatar)

                })
            }
        }
        val newCallLog = CallLogEntity(
            id = Random.Default.nextLong(),
            callerName = user.name,
            System.currentTimeMillis(),
            callDuration = 0,
            "Outgoing",
            "Not Answered",
            user.avatar,
            user.id,
            false,
            false
        )
        insertCallLog(newCallLog)
    }

    private fun startVideoCall(user: User) {
        mainRepository.sendConnectionRequest(
            DataModel(
                DataModelType.StartVideoCall, username, user.name, null
            )
        ) {
            if (it) {
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra("target", user.name)
                    putExtra("isVideoCall", true)
                    putExtra("isCaller", true)
                    putExtra("avatar", user.avatar)

                })
            }
        }
        val newCallLog = CallLogEntity(
            id = Random.Default.nextLong(),
            callerName = user.name,
            System.currentTimeMillis(),
            callDuration = 0,
            "Outgoing",
            "Not Answered",
            user.avatar,
            user.id,
            true,
            false
        )
        insertCallLog(newCallLog)
    }

    private fun insertCallLog(callLog: CallLogEntity) {
        callViewModel.insertCallLog(callLog)
    }
}