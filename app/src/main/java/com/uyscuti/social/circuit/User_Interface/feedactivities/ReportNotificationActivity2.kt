package com.uyscuti.social.circuit.User_Interface.feedactivities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.uyscuti.social.circuit.R

class ReportNotificationActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report_notification2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get user data from Intent
        val username = intent.getStringExtra("REPORTED_USERNAME") ?: "Unknown User"
        val userHandle = intent.getStringExtra("REPORTED_USER_HANDLE") ?: "@unknown"
        val profileImageUrl = intent.getStringExtra("REPORTED_PROFILE_IMAGE_URL")
        val reportedUserId = intent.getStringExtra("REPORTED_USER_ID") ?: ""

        profileImageUrl?.let { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.bottom_nav_profile)
                .error(R.drawable.bottom_nav_profile)
                .into(findViewById<ImageView>(R.id.userProfileImage))
        }

        // Setup report reasons and buttons
        val reportReasons = findViewById<RadioGroup>(R.id.reportReasons)
        val nextButton = findViewById<MaterialButton>(R.id.nextButton)
        val whyAskingText = findViewById<TextView>(R.id.whyAskingText)

        // Enable Next button when a reason is selected
        reportReasons.setOnCheckedChangeListener { _, checkedId ->
            nextButton.isEnabled = checkedId != -1
            nextButton.backgroundTintList = if (checkedId != -1) {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.bluejeans)) // Enable with blue
            } else {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.gray_dark)) // Disable with gray
            }
        }

        // Handle "Why are we asking this?" click
        whyAskingText.setOnClickListener {
            // Show a dialog or toast explaining the process
            android.widget.Toast.makeText(
                this,
                "We use this info to review and take appropriate action on reported content.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }

        // Handle Next button click
        nextButton.setOnClickListener {
            val selectedReason = when (reportReasons.checkedRadioButtonId) {
                R.id.reasonHate -> "Hate"
                R.id.reasonAbuseHarassment -> "Abuse & Harassment"
                R.id.reasonViolentSpeech -> "Violent Speech"
                R.id.reasonChildSafety -> "Child Safety"
                R.id.reasonPrivacy -> "Privacy"
                R.id.reasonSpam -> "Spam"
                R.id.reasonSuicideSelfHarm -> "Suicide or Self-Harm"
                R.id.reasonSensitiveMedia -> "Sensitive or Disturbing Media"
                R.id.reasonImpersonation -> "Impersonation"
                R.id.reasonViolentHatefulEntities -> "Violent & Hateful Entities"
                else -> ""
            }
        }

    }
    fun onCloseClicked(view: View) {
        finish()
    }
}