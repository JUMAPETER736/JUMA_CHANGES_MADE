package com.uyscuti.sharedmodule

import android.content.Context
import android.content.Intent
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.uyscuti.sharedmodule.databinding.ActivityGroupSettingsBinding
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class GroupSettingsActivity : AppCompatActivity() {

    companion object {
        const val RESULT_GROUP_DELETED = 100
        const val EXTRA_UPDATED_NAME = "updated_name"
        const val EXTRA_UPDATED_DESC = "updated_description"
        const val EXTRA_UPDATED_PHOTO = "updated_photo"

        fun open(
            context: Context,
            chatId: String,
            myRole: String,
            inviteLink: String?,
            groupName: String,
            memberCount: Int,
            description: String = "",
            editInfoLocked: Boolean = false
        ) {
            context.startActivity(
                Intent(context, GroupSettingsActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("myRole", myRole)
                    putExtra("inviteLink", inviteLink ?: "")
                    putExtra("groupName", groupName)
                    putExtra("memberCount", memberCount)
                    putExtra("description", description)
                    putExtra("editInfoLocked", editInfoLocked)
                }
            )
        }
    }


    @Inject
    lateinit var retrofitInterface: RetrofitInstance

    private lateinit var binding: ActivityGroupSettingsBinding

    private var chatId:          String  = ""
    private var myRole:          String  = "member"
    private var inviteLink:      String  = ""
    private var groupName:       String  = ""
    private var memberCount:     Int     = 0
    private var pendingName:     String  = ""
    private var pendingDesc:     String  = ""
    private var pendingPhotoUrl: String  = ""
    private var editInfoLocked:  Boolean = false














































}