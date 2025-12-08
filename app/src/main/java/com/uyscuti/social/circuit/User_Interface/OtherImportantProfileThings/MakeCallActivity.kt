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
    private lateinit var dialogsList: List<DialogEntity>

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

        }

        dialogViewModel.allDialogs.observe(this) { chats ->
            dialogsList = chats
            usersList = chats.map { it.toUser() }
            runOnUiThread {
                Log.d("UsersList", "Loaded ${usersList.size} users from dialogs")
                initUserList()
            }
        }

        supportActionBar?.title = "Call"
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

            }

            if (response.isSuccessful) {
                val responseBody = response.body()

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
            if (video) {
                startVideoCall(user)
            } else {
                startVoiceCall(user)
            }
        }

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

        val searchItem = menu.findItem(R.id.menu_search)
        val searchView = searchItem.actionView as SearchView

        // Configure SearchView to align left and take full width
        searchView.maxWidth = Integer.MAX_VALUE

        // Set the query hint
        searchView.queryHint = "Search contacts..."

        // Set the query text listener
        searchView.setOnQueryTextListener(this)

        // Handle search view expand/collapse
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // SearchView expanded
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // SearchView collapsed - reset the list
                userListAdapter.setUserList(originalUserList)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                // Let the default behavior handle it
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val filteredList = originalUserList.filter { user ->
            user.name.contains(newText.toString(), true)
        }
        userListAdapter.setUserList(filteredList)
        return true
    }

    private fun startVoiceCall(user: User) {
        val dialog = dialogsList.find { it.dialogName == user.name }
            ?: dialogsList.find { it.id == user.id }
            ?: dialogsList.find { it.users.any { u -> u.id == user.id } }

        Log.d("MakeCallActivity", "=== Starting Voice Call ===")
        Log.d("MakeCallActivity", "User name: ${user.name}")
        Log.d("MakeCallActivity", "User ID: ${user.id}")
        Log.d("MakeCallActivity", "Total dialogs: ${dialogsList.size}")
        Log.d("MakeCallActivity", "Dialog found: ${dialog != null}")

        if (dialog != null) {
            Log.d("MakeCallActivity", "Dialog ID: ${dialog.id}")
            Log.d("MakeCallActivity", "Dialog name: ${dialog.dialogName}")
            Log.d("MakeCallActivity", "Dialog users count: ${dialog.users.size}")
            dialog.users.forEach { u ->
                Log.d("MakeCallActivity", "Dialog user: ${u.name} (${u.id})")
            }
        } else {
            Log.w("MakeCallActivity", "No dialog found! Available dialogs:")
            dialogsList.forEach { d ->
                Log.w("MakeCallActivity", "  - ${d.dialogName} (${d.id})")
            }
        }

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

                    val finalChatId: String
                    val finalUserId: String

                    if (dialog != null) {
                        finalChatId = dialog.id
                        finalUserId = dialog.users.firstOrNull()?.id ?: user.id
                        Log.d("MakeCallActivity", "✓ Using dialog data - chatId: $finalChatId, userId: $finalUserId")
                    } else {
                        finalChatId = user.id
                        finalUserId = user.id
                        Log.w("MakeCallActivity", "⚠ Using fallback - chatId: $finalChatId, userId: $finalUserId")
                    }

                    putExtra("chatId", finalChatId)
                    putExtra("userId", finalUserId)

                    Log.d("MakeCallActivity", "Final intent extras - chatId: $finalChatId, userId: $finalUserId, target: ${user.name}")
                })
            } else {
                Log.e("MakeCallActivity", "Failed to send connection request")
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
        val dialog = dialogsList.find { it.dialogName == user.name }
            ?: dialogsList.find { it.id == user.id }
            ?: dialogsList.find { it.users.any { u -> u.id == user.id } }

        Log.d("MakeCallActivity", "=== Starting Video Call ===")
        Log.d("MakeCallActivity", "User name: ${user.name}")
        Log.d("MakeCallActivity", "User ID: ${user.id}")
        Log.d("MakeCallActivity", "Total dialogs: ${dialogsList.size}")
        Log.d("MakeCallActivity", "Dialog found: ${dialog != null}")

        if (dialog != null) {
            Log.d("MakeCallActivity", "Dialog ID: ${dialog.id}")
            Log.d("MakeCallActivity", "Dialog name: ${dialog.dialogName}")
            Log.d("MakeCallActivity", "Dialog users: ${dialog.users.map { it.name }}")
        } else {
            Log.w("MakeCallActivity", "No dialog found! Available dialogs:")
            dialogsList.forEach { d ->
                Log.w("MakeCallActivity", "  - ${d.dialogName} (${d.id})")
            }
        }

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

                    val finalChatId: String
                    val finalUserId: String

                    if (dialog != null) {
                        finalChatId = dialog.id
                        finalUserId = dialog.users.firstOrNull()?.id ?: user.id
                        Log.d("MakeCallActivity", "✓ Using dialog data - chatId: $finalChatId, userId: $finalUserId")
                    } else {
                        finalChatId = user.id
                        finalUserId = user.id
                        Log.w("MakeCallActivity", "⚠ Using fallback - chatId: $finalChatId, userId: $finalUserId")
                    }

                    putExtra("chatId", finalChatId)
                    putExtra("userId", finalUserId)

                    Log.d("MakeCallActivity", "Final intent extras - chatId: $finalChatId, userId: $finalUserId, target: ${user.name}")
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