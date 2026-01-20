package com.uyscuti.social.circuit.business

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uyscuti.sharedmodule.fragments.MyUserBusinessProfileFragment
import com.uyscuti.sharedmodule.model.User
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityViewMoreBusinessDetailsBinding
import com.uyscuti.social.core.models.UserData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewMoreBusinessDetails : AppCompatActivity() {

    companion object {
        const val ACTION_OPEN_PROFILE = "com.uyscuti.social.circuit.OPEN_VIEW_MORE"
        const val EXTRA_USER_ID = "user"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
    private lateinit var binding: ActivityViewMoreBusinessDetailsBinding

    @RequiresPermission("android.permission.BROADCAST_CLOSE_SYSTEM_DIALOGS")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMoreBusinessDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteId = intent.getIntExtra(EXTRA_NOTIFICATION_ID,-1)

        val userData = intent.getSerializableExtra(EXTRA_USER_ID) as UserData

        if (noteId != -1) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(noteId)
        }
        attemptToCollapseShade()

        val user = User(
            userData._id,
            userData.avatar,
            userData.email,
            userData.isEmailVerified,
            userData.role,
            userData.username,
            userData.lastSeen
        )

        val fragment = MyUserBusinessProfileFragment.newInstance(user)
        callFragment(fragment)


    }
    private fun callFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }


    @RequiresPermission("android.permission.BROADCAST_CLOSE_SYSTEM_DIALOGS")
    private fun attemptToCollapseShade() {
        // Make window more prominent
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        // Try to collapse (multiple methods)
        try {
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Bring window to front aggressively
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }
}