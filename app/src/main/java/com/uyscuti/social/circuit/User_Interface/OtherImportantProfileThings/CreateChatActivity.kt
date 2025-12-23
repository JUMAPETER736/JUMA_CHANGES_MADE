package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.circuit.adapter.UserListAdapter
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityCreateChatBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.UsersViewModel
//import com.uyscuti.social.flashdesign.utils.ChatOperationResult
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.Date
import javax.inject.Inject


@AndroidEntryPoint
class CreateChatActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityCreateChatBinding
    private lateinit var userListView: RecyclerView
    private lateinit var userListAdapter: UserListAdapter
    private lateinit var originalUserList: List<User>


    private lateinit var usersList: List<UserEntity>
    private var participants: ArrayList<User> = arrayListOf()
    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()
    private var dialog: android.app.Dialog? = null

    private val dialogViewModel: DialogViewModel by viewModels()


    @Inject
    lateinit var retrofitInterface: RetrofitInstance


    @Inject
    lateinit var localStorage: LocalStorage

    private val usersViewModel: UsersViewModel by viewModels()

    private lateinit var myId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }



        myId = localStorage.getUserId()

        supportActionBar?.title = "New Chat"


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
            usersViewModel.users.observe(this@CreateChatActivity) { users ->
                usersList = users
                runOnUiThread {
//                    Log.d("UsersList", "$usersList")
                    initUserList()
                }
            }
        }


//        observeTempDialogs()
//        initUserList()
    }

    private fun observeTempDialogs() {
        CoroutineScope(Dispatchers.Main).launch {
            dialogViewModel.getTempDialogs().observe(this@CreateChatActivity, Observer { temps ->

                Log.d("ChatOperations", "Temp Chats Found : $temps")
                CoroutineScope(Dispatchers.IO).launch {
                    handleTempDialogs(temps, 0)
//                    deleteTempDialogs(temps)
                }
            })
        }
    }


    private suspend fun deleteTempDialogs(temps: List<DialogEntity>) {
        // Your logic to delete all temporary dialogs
        // You can use dialogViewModel.deleteDialog(dialog) for each dialog
        val ids = temps.filter { it.id == it.dialogName }.map { it.id }
        dialogViewModel.deleteDialogs(ids)
//        temps.forEach { dialog ->
//            dialogViewModel.deleteDialog(dialog)
//        }
    }


    private suspend fun handleTempDialogs(tempDialogs: List<DialogEntity>, index: Int) {
        if (index < tempDialogs.size) {
            val currentDialog = tempDialogs[index]

            // Perform your specific operation for the current dialog
            val operationResult = performOperationForDialog(currentDialog)

            if (operationResult == ChatOperationResult.SUCCESS) {
                // Proceed to the next dialog after the operation is successful
                handleTempDialogs(tempDialogs, index + 1)
            } else {
                // Handle the failure if needed
//                deleteTempDialogs(tempDialogs)
            }
        } else {
            // All dialogs are successfully processed
            // You can perform any final actions here if needed
//            deleteTempDialogs(tempDialogs)
        }
    }

    private suspend fun performOperationForDialog(dialog: DialogEntity): ChatOperationResult {
        // Your specific operation for the dialog goes here
        var chatId = ""
        var isCodeExecuted = false
        // Return ChatOperationResult.SUCCESS if the operation is successful, ChatOperationResult.FAILURE otherwise
        return try {
            // Your operation logic here
            // Assuming you have access to receiverId and receiver data in your dialog
            val receiverId = dialog.users.first().id
            val receiver = dialog.users.first()
//            val receiver = dialog.receiver
            // Invoke createUserChat and check the result
//            val result = createChat(receiverId, receiver)

            val result = createChat(receiverId, receiver)


            if (result is ChatOperationResult.Return) {
                chatId = result.chatId


                // Do something with the chat ID
                Log.d("ChatOperation", "Chat Created Successfully,  Chat ID: $chatId , ")

                CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                    // Your code here

                    if (!isCodeExecuted){
                        isCodeExecuted = true
                        withContext(NonCancellable) {
                            // Your code inside withContext
                            Log.d("ChatOperation", "Before try block : $dialog")
                            try {
//                            dialog.id = chatId
//                            dialogViewModel.updateDialog(dialog)
//                            updateDialog(dialog, chatId)
//                            deleteTempDialogs(listOf(dialog))
                                Log.d("ChatOperation", "After try block , : $dialog")
//                            val updated = dialogViewModel.getDialog(chatId)

                                val newDialog = dialog
                                newDialog.id = chatId
//
//                                dialogViewModel.replaceDialog(dialog,newDialog)
//
//
                                val updated = dialogViewModel.getDialog(chatId)

                                Log.d("ChatOperation", "First Updated Dialog : $updated")
//
//                                cancel()
//
//                                return@withContext

                                // Check for cancellation and respond appropriately
                                if (!isActive) {
                                    // Perform cleanup or abort the operation as needed
                                    Log.d(
                                        "ChatOperation",
                                        "Coroutine cancelled. Performing cleanup."
                                    )
                                    return@withContext
                                }

                            } catch (e: Exception) {
                                Log.e("MessageUpdate", "Failure: ${e.message}")

                                e.printStackTrace()
                                return@withContext
                            }
                        }
                    }

                }




                ChatOperationResult.SUCCESS
            } else {
                // Handle the failure case
                Log.d("ChatOperation", "Chat Creation Failed")

                ChatOperationResult.FAILURE

            }

        } catch (e: Exception) {
            // Handle the failure and return ChatOperationResult.FAILURE
            Log.d("ChatOperation", "Chat Creation Failed : ${e.message}")

            ChatOperationResult.FAILURE
        }
    }

    private suspend fun updateDialog(dialog: DialogEntity, id:String){
        Log.d("ChatOperation", "Dialog To Update : $dialog")
        dialog.id = id
        dialogViewModel.updateDialog(dialog)

        Log.d("ChatOperation", "After try block , : $dialog")
        val updated = dialogViewModel.getDialog(id)

        Log.d("ChatOperation", "Updated Dialog : $updated")
    }


    private suspend fun createChat(receiverId: String, receiver: UserEntity): ChatOperationResult {
        return try {
            val response = retrofitInterface.apiService.createUserChat(receiverId)

            if (response.isSuccessful) {
                val chatId = response.body()!!.data._id
                val chatName = response.body()!!.data.name



//                participants.add(receiver)

                val dialog = Dialog(
                    chatId,
                    receiver.name,
                    receiver.avatar,
                    participants,
                    null,
                    0
                )

                chatParticipant.add(receiver)

                val chatResponse = response.body()!!.data
                val dialogEntity = DialogEntity(
                    id = chatResponse._id,
                    dialogPhoto = chatResponse.participants[0].avatar.url,
                    dialogName = chatResponse.participants[0].username,
                    users = chatParticipant,
                    lastMessage = null,
                    unreadCount = 0
                )

                val dialogs = dialogViewModel.getDialogsList()

                val available = dialogs.filter { it.id == it.dialogName }.find { it.dialogName == receiver.name }

                Log.d("Available", "Available Chat : $available")

                if (available != null) {
//                    dialogViewModel.replaceDialog(available,dialogEntity)


                    deleteTempDialogs(listOf(available))
                }

                insertDialog(dialogEntity)

//                MessagesActivity.open(
//                    this@CreateChatActivity, "",
//                    dialog
//                )
//
//                finish()
                ChatOperationResult.Return(chatId)
            } else {
                // Handle the error or show a message to the user
                val errorBody = response.errorBody()?.string()
                Log.d("UserDialog", "Error creating user chat: $errorBody")
                ChatOperationResult.FAILURE
            }
        } catch (e: HttpException) {
            Log.d("UserDialog", "Http Exception In User Dialog is : ${e.message}")
            ChatOperationResult.FAILURE
        } catch (e: IOException) {
            Log.d("UserDialog", "IOException In User Dialog is : ${e.message}")
            ChatOperationResult.FAILURE
        }
    }


    private suspend fun chatExist(name: String, resultHandler: (Boolean, DialogEntity?) -> Unit) {
        val dialogEntityLiveData = dialogViewModel.getDialogByName(name)

        val dialogs = dialogViewModel.getDialogsList()

        val available = dialogs.filter { it.id != it.dialogName }.find { it.dialogName == name }

        val dialogEntity = dialogEntityLiveData.value

        Log.d("Dialog", "Dialog Entity : $dialogEntity")
        Log.d("Dialog", "Available Dialog Entity : $available")

        // Invoke the resultHandler with the information about dialog existence and the entity
        resultHandler(available != null, available)
    }


    private fun initUserList() {
        userListView = binding.userList
        userListAdapter = UserListAdapter(this) {

            CoroutineScope(Dispatchers.Main).launch {
                chatExist(it.name) { exists, dialogEntity ->
                    if (exists) {

                        Log.d("UserDialog", "Dialog exists, and you can use dialogEntity")
                        // Dialog exists, and you can use dialogEntity
                        participants.add(it)

                        val lastMessage = dialogEntity?.lastMessage?.toMessage()

                        val dialog = Dialog(
                            dialogEntity?.id,
                            dialogEntity?.dialogName,
                            dialogEntity?.dialogPhoto,
                            participants,
                            lastMessage,
                            0
                        )
                        MessagesActivity.open(
                            this@CreateChatActivity, "",
                            dialog, false
                        )
//                        openChatActivity(chatId,chatName)
                        finish()
                    } else {
                        // Dialog does not exist
                        // ...

                        Log.d("UserDialog", "Dialog does not exist")
                        doInBackGround(it)
                    }
                }
            }

//            if (chatExist(it.name)){
//
//            } else {
//
//            }
        }

        // Set LinearLayoutManager for a vertical list
        userListView.layoutManager = LinearLayoutManager(this)

        // Get the random users and sort them alphabetically
//        val randomUsers = DialogsFixtures.getRandomUsers(15)
//        val sortedUsers = randomUsers.sortedBy { it.name }

        originalUserList = usersList.map { it.toUser() }.sortedBy { it.name }

        val converted = usersList.map { it.toUser() }

        val sorted = converted.sortedBy { it.name }

        userListAdapter.setUserList(sorted)
        userListView.adapter = userListAdapter
    }

    private fun doInBackGround(user: User) {
        val singleUserList = arrayListOf(user)
//        singleUserList.size

        Log.d("UserList", "Single User List Size : ${singleUserList.size}")

        val tempDialog = Dialog(
            user.name,
            user.name,
            user.avatar,
            singleUserList,
            null,
            0
        )

//        chatParticipant.add(receiver.toUserEntity())
//        chatParticipant.add(user.toUserEntity())

        val dialogEntity = DialogEntity(
            id = tempDialog.dialogName,
            dialogPhoto = tempDialog.dialogPhoto,
            dialogName = tempDialog.dialogName,
            users = listOf(user.toUserEntity()),
            lastMessage = null,
            unreadCount = 0
        )

        insertDialog(dialogEntity)

//        createUserChat(user.id, user)

        openTempChat(tempDialog)
    }

    @OptIn(UnstableApi::class)
    private fun openTempChat(dialog: Dialog) {
        MessagesActivity.open(
            this@CreateChatActivity, "",
            dialog, true
        )
        finish()
    }

    private fun MessageEntity.toMessage(): Message {

        val userId = if (userId == myId) "0" else "1"
        val status = status
        val user =
            User(userId, user.name, user.avatar, user.online, user.lastSeen)
        val date = Date(createdAt)

        // Check if the text is "None" and imageUrl is not null
        val messageContent = if (imageUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setImage(Message.Image(imageUrl!!))
                setStatus(status)
            }
        } else if (videoUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setVideo(Message.Video(videoUrl!!))
                setStatus(status)
            }
        } else if (audioUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setAudio(
                    Message.Audio(
                        audioUrl!!,
                        0,
                        getNameFromUrl(audioUrl!!)
                    )
                )
                setStatus(status)
            }
        } else if (voiceUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {
                setVoice(Message.Voice(voiceUrl!!, voiceDuration))
                setStatus(status)
            }
        } else if (docUrl != null) {
//                        user.id = "0"
            Message(
                id,
                user,
                null,
                date
            ).apply {


                setStatus(status)
            }
        } else {
            Message(
                id,
                user,
                text,
                date
            ).apply {
                setStatus(status)
            }
        }

        return messageContent
    }


    private fun getNameFromUrl(videoUrl: String): String {
        // Split the URL using '/' as a delimiter and get the last part, which is the video filename
        val parts = videoUrl.split("/")

        // You can further process the filename if needed, such as removing the file extension
        return parts.last()
    }


    private fun fetchUsers() {
        GlobalScope.launch {
            val response = try {

                retrofitInterface.apiService.getUsers()
            } catch (e: HttpException) {
                Log.d("RetrofitActivity", "Http Exception ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@CreateChatActivity,
                        "HTTP error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                return@launch
            } catch (e: IOException) {
                Log.d("RetrofitActivity", "IOException ${e.message}")
                runOnUiThread {
                    Toast.makeText(
                        this@CreateChatActivity,
                        "Network error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            } finally {

            }

            if (response.isSuccessful) {
                val responseBody = response.body()
//                Log.d("AllUsers", "Users List ${responseBody?.data}")

                if (responseBody?.data != null) {

//                    responseBody.data[0].

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
//        showLoadingDialog()

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

                        participants.add(receiver)

                        val dialog = Dialog(
                            chatId,
                            receiver.name,
                            receiver.avatar,
                            participants,
                            null,
                            0
                        )
//                        val allUsers = mutableListOf<DialogEntity>()

                        chatParticipant.add(receiver.toUserEntity())


                        val chatResponse = response.body()!!.data
                        val dialogEntity = DialogEntity(
                            id = chatResponse._id,
                            dialogPhoto = chatResponse.participants[0].avatar.url,
                            dialogName = chatResponse.participants[0].username,
                            users = chatParticipant,
                            lastMessage = null,
                            unreadCount = 0
                        )

//                        allUsers.add(dialogEntity)
                        insertDialog(dialogEntity)

//                        dismissLoadingDialog()

                        MessagesActivity.open(
                            this@CreateChatActivity, "",
                            dialog, true
                        )
//                        openChatActivity(chatId,chatName)
                        finish()
                    } else {
                        // Handle the error or show a message to the user
                        val errorBody = response.errorBody()?.string()
                        Log.d("UserDialog", "Error creating user chat: $errorBody")
//                        dismissLoadingDialog()

                    }
                }
            } catch (e: HttpException) {
                Log.d("UserDialog", "Http Exception In User Dialog is : ${e.message}")
//                dismissLoadingDialog()

            } catch (e: IOException) {
                Log.d("UserDialog", "IOException In User Dialog is : ${e.message}")
//                dismissLoadingDialog()
            }
        }
    }

    private fun insertDialog(dialog: DialogEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            dialogViewModel.insertDialog(dialog)
        }
//        Log.d("OneToChat", "Added single chat to local -  $dialog")

    }

    private fun DialogEntity.toDialog(): Dialog {
        return Dialog(
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
            lastSeen,
        )
    }

    private fun User.toUserEntity(): UserEntity {
        return UserEntity(
            id,
            name,
            avatar,
            lastSeen = Date(),
            true
        )
    }

    private fun showLoadingDialog() {
        // Create a dialog with the loading layout
        dialog = android.app.Dialog(this)
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

sealed class ChatOperationResult {
    object SUCCESS : ChatOperationResult()
    object FAILURE : ChatOperationResult()
    data class Return(val chatId: String) : ChatOperationResult()
}