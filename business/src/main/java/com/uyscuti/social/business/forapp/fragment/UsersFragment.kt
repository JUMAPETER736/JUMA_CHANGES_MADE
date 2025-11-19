package com.uyscuti.social.business.forapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.business.ProfileViewActivity
import com.uyscuti.social.business.adapter.UsersAdapter
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import retrofit2.HttpException
import com.uyscuti.social.business.model.User
import com.uyscuti.social.business.R


@AndroidEntryPoint
class UsersFragment : Fragment() {

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var usersAdapter: UsersAdapter

//    private val apiService = RetrofitClient.instance
//    private val apiService = RetrofitInstance

    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewUsers = view.findViewById(R.id.recyclerViewUsers)

        val userList = arrayListOf<User>()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = retrofitInterface.apiService.getOtherUsers()

                if (response.isSuccessful) {
                    val users = response.body()?.data
                    Log.d("ApiService", "Other users: $users")

                    if (!users.isNullOrEmpty()) {
                        for (user in users) {
                            Log.d("ApiService", "User: $user")
                            val userItem = User(
                                _id = user._id,
                                avatar = user.avatar.url,
                                email = user.email,
                                isEmailVerified = user.isEmailVerified,
                                role = user.role,
                                username = user.username,
                                lastseen = TODO()
                            )
                            userList.add(userItem)
                        }
                    }

                } else {
                    Log.e("ApiService", "Failed to get other users: ${response.code()}")
                }
            } catch (e: HttpException) {
                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
            } catch (e: Throwable) {
                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    // Setting up the RecyclerView
                    usersAdapter = UsersAdapter(userList) { user ->
                        val intent = Intent(requireContext(), ProfileViewActivity::class.java)
                        intent.putExtra("userId", user._id)
                        intent.putExtra("userName", user.username)
                        intent.putExtra("userAvatar", user.avatar)
                        startActivity(intent)
                    }
                    recyclerViewUsers.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    recyclerViewUsers.adapter = usersAdapter
                }
            }
        }
    }
}
