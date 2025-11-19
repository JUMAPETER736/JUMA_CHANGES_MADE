package com.uyscuti.social.call.ui//package com.uyscut.flashcall.ui
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import com.uyscuti.social.call.repository.MainRepository
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//
//
//@AndroidEntryPoint
//class LoginActivity : AppCompatActivity() {
//
//    private lateinit var  views: ActivityLoginBinding
//    @Inject lateinit var mainRepository: MainRepository
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        views = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(views.root)
//        init()
//    }
//
//
//    private fun init(){
//        views.apply {
//            btn.setOnClickListener {
////                mainRepository.login(
////                    usernameEt.text.toString(),passwordEt.text.toString()
////                ){ isDone, reason ->
////                    if (!isDone){
////                        Toast.makeText(this@LoginActivity, reason, Toast.LENGTH_SHORT).show()
////                    }else{
////                        //start moving to our main activity
////                        startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
////                            putExtra("username",usernameEt.text.toString())
////                        })
////                    }
////                }
//            }
//        }
//    }
//}