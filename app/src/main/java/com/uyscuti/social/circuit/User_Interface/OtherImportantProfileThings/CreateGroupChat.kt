package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.adapter.SearchUserAdapter
import com.uyscuti.social.circuit.adapter.SelectedUsersAdapter
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.circuit.databinding.ActivityCreateGroupChatBinding
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.circuit.presentation.RecentUserViewModel
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.network.api.request.search.SearchUsersRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CreateGroupChat : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupChatBinding

    private lateinit var searchEditText: EditText
    private lateinit var userListView: RecyclerView
    private lateinit var selectedUserListView: RecyclerView
    private lateinit var userListAdapter: SearchUserAdapter
    private lateinit var originalUserList: List<User>
    private  var selectedUserList: ArrayList<User> = arrayListOf()

    private lateinit var selectedUsersAdapter: SelectedUsersAdapter

    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()

    private var participants: ArrayList<com.uyscuti.social.network.api.models.User> = arrayListOf()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private val recentUserViewModel: RecentUserViewModel by viewModels()

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var myId: String

    private val dialogViewModel: DialogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        searchEditText = binding.searchEditText

        myId = localStorage.getUserId()

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        setListeners()
        initUserList()
        initSelectedUsers()

        hideKeyboard()
        searchEditText.clearFocus()


        CoroutineScope(Dispatchers.IO).launch {
            val recentUsers = recentUserViewModel.getRecentUsers()

            userListAdapter.setRecentUsers(recentUsers.map { it.toUser() })


        }



        recentUserViewModel.initializeSelectedUserList()

        CoroutineScope(Dispatchers.Main).launch {
            recentUserViewModel.selectedUserList.observe(this@CreateGroupChat,
                Observer { userList ->


                    if (userList.isNotEmpty()) {
                        selectedUserListView.visibility = View.VISIBLE
                        binding.nextFab.visibility = View.VISIBLE
                    } else {
                        selectedUserListView.visibility = View.GONE
                        binding.nextFab.visibility = View.GONE
                    }
                })
        }

    }


    private fun RecentUser.toUser(): User{
        return User(
            id, name, avatar, online, lastSeen
        )
    }

    private fun setListeners(){
        // Handle the "Search" button on the keyboard
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                userListAdapter.setLoading(true)
                performSearch(searchEditText.text.toString())
                true
            } else {
                false
            }
        }


        searchEditText.addTextChangedListener(afterTextChanged = { editable ->
            // This will be called after the text in the EditText has changed
            // and the user has finished typing

            Log.d("FinishedTyping", "FinishedTyping: $editable")

            userListAdapter.setLoading(true)
            val searchText = editable.toString()
            performSearch(searchText)
        })


        binding.nextFab.setOnClickListener {
            val intent = Intent(this, ConfirmGroupActivity::class.java)

            intent.putParcelableArrayListExtra("participantList", ArrayList(selectedUserList))
            startActivity(intent)
        }
    }

    private fun initSelectedUsers(){
        selectedUserListView = binding.selectedList
        selectedUserListView.visibility = View.GONE
        selectedUsersAdapter = SelectedUsersAdapter(this) {
            selectedUsersAdapter.removeUser(it)
            selectedUserList.remove(it)

            recentUserViewModel.removeUser(it)
        }

        selectedUserListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        selectedUserListView.adapter = selectedUsersAdapter
    }

    private fun initUserList() {
        userListView = binding.userList
        userListAdapter = SearchUserAdapter(this) {


            addUserToRecent(it.toRecentUser())

            CoroutineScope(Dispatchers.Main).launch {
                if (selectedUserList.contains(it)) {
                    selectedUserList.remove(it)
                    selectedUsersAdapter.removeUser(it)
                    recentUserViewModel.removeUser(it)
                } else {
                    selectedUserList.add(it)
                    selectedUsersAdapter.addUser(it)
                    recentUserViewModel.addUser(it)
                }

                selectedUserListView.scrollToPosition(selectedUsersAdapter.itemCount - 1)
            }
        }
        userListAdapter.setGroupSearch(true)

        // Set LinearLayoutManager for a vertical list
        userListView.layoutManager = LinearLayoutManager(this)

        originalUserList = listOf()

        userListView.adapter = userListAdapter
    }

    private fun com.uyscuti.social.network.api.models.User.toUser(): User{
        return User(
            _id,
            username,
            avatar.url,
            false,
            Date()
        )
    }

    private fun User.toRecentUser(): RecentUser {
        return RecentUser(
            id,
            name,
            avatar,
            Date(),
            false,
            Date()
        )
    }

    private fun addUserToRecent(user: RecentUser){
        CoroutineScope(Dispatchers.IO).launch {
            recentUserViewModel.addRecentUser(user)
        }
    }

    private fun performSearch(query: String) {
        userListAdapter.setLoading(true)

        CoroutineScope(Dispatchers.Main).launch {
            val searchResults = searchUsers(query).sortedBy { it.username }

            hideKeyboard()
            searchEditText.clearFocus()

            Log.d("SearchResults", "$searchResults")
            Log.d("SearchResults", "${searchResults.size}")

            if (searchResults.isNotEmpty()){
                userListAdapter.setSearchUsers(searchResults.map { it.toUser() })
            } else {
                userListAdapter.setNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<com.uyscuti.social.network.api.models.User> {
        userListAdapter.setLoading(true)
        return withContext(Dispatchers.IO) {
            val users = mutableListOf<com.uyscuti.social.network.api.models.User>()

            try {
                val request = SearchUsersRequest("")
                val response = retrofitInterface.apiService.searchUsers(query)


                if (response.isSuccessful) {
                    Log.d("SearchUsers", "Success Message: ${response.body()?.message}")
                    Log.d("SearchUsers", "Success Data: ${response.body()?.data}")

                    users.addAll(response.body()?.data as List<com.uyscuti.social.network.api.models.User>)
                } else {
                    Log.e("SearchUsers", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("SearchUsers", "Exception: ${e.message}")
                e.printStackTrace()
            }

            users
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}