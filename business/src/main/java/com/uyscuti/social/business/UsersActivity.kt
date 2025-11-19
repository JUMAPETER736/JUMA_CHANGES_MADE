package com.uyscuti.social.business

//import com.example.mylibrary.retro.RetrofitClient

//class UsersActivity : AppCompatActivity() {
//
//    private lateinit var recyclerViewUsers: RecyclerView
//    private lateinit var usersAdapter: UsersAdapter
//
//    private val apiService = RetrofitClient.instance
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_users)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
//
//        val userList = arrayListOf<User>()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = apiService.getOtherUsers()
//
//                if (response.isSuccessful) {
//                    val users = response.body()?.data
//                    Log.d("ApiService", "Other users: $users")
//
//                    if (!users.isNullOrEmpty()) {
//                        for (user in users) {
//                            Log.d("ApiService", "User: $user")
//                            val userItem = User(user._id, user.username, user.avatar.url)
//                            userList.add(userItem)
//                        }
//                    }
//
//                } else {
//                    Log.e("ApiService", "Failed to get other users: ${response.code()}")
//                }
//            } catch (e: HttpException) {
//                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
//            } catch (e: Throwable) {
//                Log.e("ApiService", "Failed to get other users: ${e.message}", e)
//            } finally {
//                withContext(Dispatchers.Main) {
//                    // Setting up the RecyclerView
//                    usersAdapter = UsersAdapter(userList){ user ->
//                        val intent = Intent(this@UsersActivity, ProfileViewActivity::class.java)
//                        intent.putExtra("userId", user.id)
//                        intent.putExtra("userName", user.name)
//                        intent.putExtra("userAvatar", user.avatar)
//                        startActivity(intent)
//                    }
//                    recyclerViewUsers.layoutManager =
//                        LinearLayoutManager(this@UsersActivity, LinearLayoutManager.VERTICAL, false)
//                    recyclerViewUsers.adapter = usersAdapter
//                }
//            }
//        }
//
//        // Sample user data
////        val userList = listOf(
////            User("1", "Alice", "https://example.com/avatar1.png"),
////            User("2", "Bob", "https://example.com/avatar2.png"),
////            User("3", "Charlie", "https://example.com/avatar3.png"),
////            User("4", "Dave", "https://example.com/avatar4.png"),
////            User("5", "Eve", "https://example.com/avatar5.png")
////        )
//
//
//    }
//}