package com.uyscuti.social.circuit.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.core.common.data.room.entity.RecentUser
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.uyscuti.social.circuit.adapter.SearchUserAdapter
import com.uyscuti.social.circuit.adapter.SelectedUsersAdapter
import com.uyscuti.social.circuit.databinding.ActivityCreateGroupChatBinding
import com.uyscuti.sharedmodule.presentation.DialogViewModel
import com.uyscuti.sharedmodule.presentation.RecentUserViewModel
import com.uyscuti.social.network.api.models.User
import com.uyscuti.social.network.api.request.search.SearchUsersRequest
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.AndroidEntryPoint
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
    private lateinit var originalUserList: List<com.uyscuti.social.core.models.data.User>
    private var selectedUserList: ArrayList<com.uyscuti.social.core.models.data.User> = arrayListOf()

    private lateinit var selectedUsersAdapter: SelectedUsersAdapter

    private var chatParticipant: ArrayList<UserEntity> = arrayListOf()
    private var participants: ArrayList<User> = arrayListOf()

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    // FIX: reference captured once on the Main thread — never accessed from IO
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

        // FIX: Force the lazy `by viewModels()` delegate to resolve RIGHT NOW on the
        // Main thread before any coroutine is launched. Hilt's ViewModelStore.put()
        // calls ensureMainThread() on first access — if the first access happens inside
        // withContext(Dispatchers.IO) the app crashes even though lifecycleScope itself
        // starts on Main, because withContext switches the thread for the lambda body.
        val vm = recentUserViewModel

        binding.toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_ios_24)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setListeners()
        initUserList()
        initSelectedUsers()

        hideKeyboard()
        searchEditText.clearFocus()

        // FIX: was CoroutineScope(Dispatchers.IO).launch { recentUserViewModel.getRecentUsers() }
        // Accessing a `by viewModels()` delegate off the Main thread triggers Hilt's
        // ensureMainThread() check and crashes with IllegalStateException.
        //
        // Correct pattern: use lifecycleScope (always Main), do the heavy Room query
        // inside withContext(Dispatchers.IO), then update the adapter back on Main.
        lifecycleScope.launch {
            // Repository now owns IO dispatch internally — no withContext needed here.
            // vm is pre-resolved on Main (see comment above), so this is safe.
            val recentUsers = vm.getRecentUsers()
            userListAdapter.setRecentUsers(recentUsers.map { it.toUser() })
        }

        vm.initializeSelectedUserList()

        vm.selectedUserList.observe(this) { userList ->
            if (userList.isNotEmpty()) {
                selectedUserListView.visibility = View.VISIBLE
                binding.nextFab.visibility = View.VISIBLE
            } else {
                selectedUserListView.visibility = View.GONE
                binding.nextFab.visibility = View.GONE
            }
        }
    }

    //  Extension helpers

    private fun RecentUser.toUser(): com.uyscuti.social.core.models.data.User {
        return com.uyscuti.social.core.models.data.User(id, name, avatar, online, lastSeen)
    }

    private fun User.toUser(): com.uyscuti.social.core.models.data.User {
        return com.uyscuti.social.core.models.data.User(_id, username, avatar.url, false, Date())
    }

    private fun com.uyscuti.social.core.models.data.User.toRecentUser(): RecentUser {
        return RecentUser(id, name, avatar, Date(), false, Date())
    }

    //  Listeners

    private fun setListeners() {
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
            Log.d("FinishedTyping", "FinishedTyping: $editable")
            userListAdapter.setLoading(true)
            performSearch(editable.toString())
        })

        binding.nextFab.setOnClickListener {
            val intent = Intent(this, ConfirmGroupActivity::class.java)
            intent.putParcelableArrayListExtra("participantList", ArrayList(selectedUserList))
            startActivity(intent)
        }
    }

    //  List setup

    private fun initSelectedUsers() {
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
        userListAdapter = SearchUserAdapter(this) { user ->
            addUserToRecent(user.toRecentUser())

            // FIX: was CoroutineScope(Dispatchers.Main).launch — unnecessary wrapper.
            // All of this is synchronous UI work; lifecycleScope is cleaner and
            // automatically cancelled when the Activity is destroyed.
            lifecycleScope.launch {
                if (selectedUserList.contains(user)) {
                    selectedUserList.remove(user)
                    selectedUsersAdapter.removeUser(user)
                    recentUserViewModel.removeUser(user)
                } else {
                    selectedUserList.add(user)
                    selectedUsersAdapter.addUser(user)
                    recentUserViewModel.addUser(user)
                }
                selectedUserListView.scrollToPosition(selectedUsersAdapter.itemCount - 1)
            }
        }
        userListAdapter.setGroupSearch(true)
        userListView.layoutManager = LinearLayoutManager(this)
        originalUserList = listOf()
        userListView.adapter = userListAdapter
    }

    //  Search

    private fun performSearch(query: String) {
        userListAdapter.setLoading(true)
        lifecycleScope.launch {
            val searchResults = searchUsers(query).sortedBy { it.username }

            hideKeyboard()
            searchEditText.clearFocus()

            Log.d("SearchResults", "$searchResults")
            Log.d("SearchResults", "${searchResults.size}")

            if (searchResults.isNotEmpty()) {
                userListAdapter.setSearchUsers(searchResults.map { it.toUser() })
            } else {
                userListAdapter.setNoResults()
            }
        }
    }

    private suspend fun searchUsers(query: String): List<User> {
        userListAdapter.setLoading(true)
        return withContext(Dispatchers.IO) {
            val users = mutableListOf<User>()
            try {
                retrofitInterface.apiService.searchUsers(query).let { response ->
                    if (response.isSuccessful) {
                        Log.d("SearchUsers", "Success Message: ${response.body()?.message}")
                        Log.d("SearchUsers", "Success Data: ${response.body()?.data}")
                        users.addAll(response.body()?.data as List<User>)
                    } else {
                        Log.e("SearchUsers", "Error: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchUsers", "Exception: ${e.message}")
                e.printStackTrace()
            }
            users
        }
    }

    //  Recent users

    private fun addUserToRecent(user: RecentUser) {
        // addRecentUser is now a plain fun (fire-and-forget via viewModelScope inside
        // the ViewModel) — no coroutine needed at the call site.
        recentUserViewModel.addRecentUser(user)
    }

    //  Keyboard

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}