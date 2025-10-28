package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.AddParticipantsAdapter
import com.uyscuti.social.circuit.adapter.UserListAdapter
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityCreateGroupBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.UsersViewModel
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity(), SearchView.OnQueryTextListener  {
    private lateinit var binding: ActivityCreateGroupBinding

    private lateinit var userListView: RecyclerView
    private lateinit var userListAdapter: UserListAdapter
    private lateinit var originalUserList: List<User>
    private lateinit var participantsList: RecyclerView
    private lateinit var participantsAdapter: AddParticipantsAdapter

    private lateinit var usersList: List<UserEntity>
    private var participants: ArrayList<User> = arrayListOf()
    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()
    private var dialog: Dialog? = null

    private val dialogViewModel: DialogViewModel by viewModels()

    private val groupDialogViewModel: GroupDialogViewModel by viewModels()



    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private val usersViewModel: UsersViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        supportActionBar?.title = "New Group Chat"

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected

        if (!isConnected) {
            // No internet connection, get data from room
            //return
        } else {
            fetchUsers()
        }

        CoroutineScope(Dispatchers.Main).launch {
            usersViewModel.users.observe(this@CreateGroupActivity){ users ->
                usersList = users
                runOnUiThread{
                    Log.d("UsersList", "$usersList")
                    initUserList()
                }
            }
        }

//        initUserList()
    }
    private fun initUserList() {
        userListView = binding.userList
        participantsList = binding.participantsView

        userListAdapter = UserListAdapter(this) { user ->
            val isUserInParticipants = participants.contains(user)

            if (participants.contains(user)) {
                // User exists in the participants list, remove it
                participants.remove(user)
                participantsAdapter.removeUser(user)

            } else {
                // User doesn't exist in the participants list, add it
                participants.add(user)
                participantsAdapter.addUser(user)
            }

            val participantsVisibility = if (participants.isNotEmpty()) View.VISIBLE else View.GONE
            animateVisibilityChange(binding.participantsView, participantsVisibility)

            // Update the visibility of the participants layout
//            binding.participants.visibility = if (participants.isNotEmpty()) {
//                View.VISIBLE
//            } else {
//                View.GONE
//            }
            binding.nextFab.visibility = if (participants.size > 0) {
                View.VISIBLE
            } else {
                View.GONE
            }
            // Update the adapter and scroll to the last item
//            participantsAdapter.addUser(user)
            participantsList.scrollToPosition(participantsAdapter.itemCount - 1)
            participantsAdapter.notifyDataSetChanged()
        }

        participantsAdapter = AddParticipantsAdapter(this) {
            participants.remove(it)

            val participantsVisibility = if (participants.isNotEmpty()) View.VISIBLE else View.GONE
            animateVisibilityChange(binding.participantsView, participantsVisibility)
//            binding.participants.visibility = if (participants.size > 0) {
//                View.VISIBLE
//            } else {
//                View.GONE
//            }
            binding.nextFab.visibility = if (participants.size > 1) {
                View.VISIBLE
            } else {
                View.GONE
            }
            participantsAdapter.removeUser(it)
            participantsAdapter.notifyDataSetChanged()
        }


        participantsList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Set LinearLayoutManager for a vertical list
        userListView.layoutManager = LinearLayoutManager(this)

        // Get the random users and sort them alphabetically
//        val randomUsers = DialogsFixtures.getRandomUsers(15)
//        val sortedUsers = randomUsers.sortedBy { it.name }

        originalUserList = usersList.map { it.toUser() }.sortedBy { it.name }

        val converted = usersList.map { it.toUser() }

        val sorted = converted.sortedBy { it.name }

        participantsAdapter.setUserList(participants)

        userListAdapter.setUserList(sorted)
        userListView.adapter = userListAdapter

        participantsList.adapter = participantsAdapter

        binding.nextFab.setOnClickListener {

            // Convert DBUserList to Participant list
//            val participantList = participants.map { it.toParticipant() }
////
            val intent = Intent(this, ConfirmGroupActivity::class.java)

            intent.putParcelableArrayListExtra("participantList", ArrayList(participants))
            startActivity(intent)
        }
    }


    private fun animateVisibilityChange(view: View, newVisibility: Int) {
        if (view.isVisible != (newVisibility == View.VISIBLE)) {
            val alpha = if (newVisibility == View.VISIBLE) 1f else 0f
            val animator = ObjectAnimator.ofFloat(view, View.ALPHA, alpha)
            animator.duration = 800 // Adjust the duration as needed
            animator.start()

            view.visibility = newVisibility
        }
    }
    private fun fetchUsers() {
        GlobalScope.launch {
            val response = try {

                retrofitInterface.apiService.getUsers()
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@CreateGroupActivity,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@CreateGroupActivity,
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

    private fun insertUser(user: UserEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            usersViewModel.addUsers(user)
        }
    }

    private fun createUserChat(receiverId: String, receiver: User) {
        showLoadingDialog()

        GlobalScope.launch {
            try {
                val response = retrofitInterface.apiService.createUserChat(receiverId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // User chat created successfully, you can handle this as needed
                        Log.d(
                            "UserDialog",
                            "User chat created successfully: id ${response.body()!!.data._id}"
                        )
//                        callBack?.onChatCreated()
                        val chatId = response.body()!!.data._id
                        val chatName = response.body()!!.data.name

                        val admin =

                            participants.add(receiver)

                        val dialog = com.uyscuti.social.circuit.data.model.Dialog(
                            chatId,
                            receiver.name,
                            receiver.avatar,
                            participants,
                            null,
                            0
                        )
//                        val allUsers = mutableListOf<DialogEntity>()
                        val chatResponse = response.body()!!.data


                        chatParticipant.add(receiver.toUserEntity())

                        val lastMessage = createDefaultMessageEntity(chatResponse.createdAt)
                        val createdAt = convertIso8601ToUnixTimestamp(chatResponse.createdAt)
                        val updatedAt = convertIso8601ToUnixTimestamp(chatResponse.updatedAt)

                        val dialogEntity = GroupDialogEntity(
                            id = chatResponse._id,
                            adminId = chatResponse.admin,
                            adminName = chatName,
                            dialogPhoto = chatResponse.participants[0].avatar.url,
                            dialogName = chatResponse.participants[0].username,
                            users = chatParticipant,
                            lastMessage = lastMessage,
                            unreadCount = 0,
                            updatedAt = updatedAt,
                            createdAt = createdAt
                        )

//                        allUsers.add(dialogEntity)
//                        insertDialog(dialogEntity)

                        dismissLoadingDialog()

                        MessagesActivity.Companion.open(
                            this@CreateGroupActivity, "",
                            dialog, false
                        )
//                        openChatActivity(chatId,chatName)
                        finish()
                    } else {
                        // Handle the error or show a message to the user
                        val errorBody = response.errorBody()?.string()
                        Log.d("UserDialog", "Error creating user chat: $errorBody")
                    }
                }
            } catch (e: HttpException) {
                Log.d("UserDialog", "Http Exception In User Dialog is : ${e.message}")
            } catch (e: IOException) {
                Log.d("UserDialog", "IOException In User Dialog is : ${e.message}")
            }
        }
    }


    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")


        val date = sdf.parse(iso8601Date)
        return date?.time ?: 0
    }


    private fun createDefaultMessageEntity(date: String): MessageEntity {
        val createdAt = convertIso8601ToUnixTimestamp(date)
        val lastseen = Date(createdAt)

        val user = UserEntity(
            id = "Flash",
            name = "Flash",
            avatar = "Flash",
            online = true,
            lastSeen = lastseen
        )

        return MessageEntity(
            id = "FirstMessageId",
            chatId = "InitialMessage",
            text = "No messages yet for this group chat, be the first to send a message.",
            userId = "Flash",
            user = user,
            createdAt = createdAt,
            imageUrl = null,
            voiceUrl = null,
            voiceDuration = 0,
            userName = "Flash",
            status = "Received",
            videoUrl = null,
            audioUrl = null,
            docUrl = null,
            fileSize = 0
        )
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
//        Log.d("OneToChat", "Added single chat to local -  $dialog")

    }

    private fun DialogEntity.toDialog(): com.uyscuti.social.circuit.data.model.Dialog {
        return com.uyscuti.social.circuit.data.model.Dialog(
            id,
            dialogName,
            dialogPhoto,
            participants,
            null,
            unreadCount
        )
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

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            Date(),
            true
        )
    }

    private fun showLoadingDialog() {
        // Create a dialog with the loading layout
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.loading_dialog)
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside

        // Show the dialog
        dialog?.show()
    }

    private fun dismissLoadingDialog() {
        dialog?.dismiss()
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
}