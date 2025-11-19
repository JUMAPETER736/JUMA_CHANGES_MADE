package com.uyscuti.social.circuit.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi

import com.uyscuti.social.circuit.MainActivity
import com.uyscuti.social.circuit.MainDialogsFragment

import com.uyscuti.social.circuit.interfaces.OnBackPressedListener
import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
import com.uyscuti.social.circuit.presentation.MainViewModel
import com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings.MessagesActivity
import com.uyscuti.social.circuit.utils.getChatNavigationController
import com.uyscuti.social.chatsuit.dialogs.DialogsList
import com.uyscuti.social.chatsuit.dialogs.DialogsListAdapter
import com.uyscuti.social.chatsuit.utils.DateFormatter
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.data.fixtures.DialogsFixtures
import com.uyscuti.social.circuit.data.model.Dialog
import com.uyscuti.social.circuit.data.model.Message
import com.uyscuti.social.circuit.data.model.User
import com.uyscuti.social.core.common.data.room.entity.GroupDialogEntity
import com.uyscuti.social.core.common.data.room.entity.MessageEntity
import com.uyscuti.social.core.common.data.room.entity.UserEntity
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupChats.newInstance] factory method to
 * create an instance of this fragment.
 */

@UnstableApi
@AndroidEntryPoint
class GroupChats : MainDialogsFragment() ,  DateFormatter.Formatter, OnBackPressedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var dialogsList: DialogsList
    private var unreadCount = 0
    private var groupUnread = 0
    private var selectedDialogs = 0


    private val groupDialogViewModel: GroupDialogViewModel by viewModels()

    private val mainViewModel: MainViewModel by activityViewModels()


    private val mainActivity: MainActivity by lazy {
        requireActivity() as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.resetSelectedDialogsCount()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_group_chats, container, false)
        dialogsList = view.findViewById(R.id.dialogsList)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installTwitter()
//        initAdapter()
        initDialogs()

        mainActivity.addOnBackPressedListener(this)

    }

    override fun onDialogClick(dialog: Dialog?) {


        val isAlreadySelected = mainViewModel.selectedDialogsList.contains(dialog)

        if (selectedDialogs < 1) {
            openMessages(dialog!!)
        } else {
            if (dialog != null) {

                if (isAlreadySelected){
                    deselectDialog(dialog)
                } else {
                    selectDialog(dialog)
                }
            }

        }


    }

    private fun installTwitter() {
        EmojiManager.install(TwitterEmojiProvider())
    }

    private fun openMessages(dialog: Dialog) {
        MessagesActivity.open(requireContext(), dialog.dialogName, dialog, false)
        resetUnreadCount(dialog)
    }

    private fun selectDialog(dialog: Dialog) {
        selectedDialogs++

        mainViewModel.incrementAndAddToSelectedDialogs(dialog)
        dialog.setSelected(true)
        dialogsAdapter.updateItemById(dialog)

    }

    private fun deselectDialog(dialog: Dialog) {
        selectedDialogs--
        mainViewModel.decrementAndRemoveFromSelectedDialogs(dialog)
        dialog.setSelected(false)
        dialogsAdapter.updateItemById(dialog)
    }



    private fun resetUnreadCount(dialog: Dialog) {
        CoroutineScope(Dispatchers.IO).launch {
            val dg = groupDialogViewModel.getGroupDialog(dialog.id)
            dg.unreadCount = 0
            groupDialogViewModel.updateGroupDialog(dg)
        }
    }



    override fun format(date: Date): String {
        return when {
            DateFormatter.isToday(date) -> DateFormatter.format(date, DateFormatter.Template.TIME)
            DateFormatter.isYesterday(date) -> getString(R.string.date_header_yesterday)
            DateFormatter.isCurrentYear(date) -> DateFormatter.format(
                date,
                DateFormatter.Template.STRING_DAY_MONTH
            )
            else -> DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
        }
    }

    private fun initDialogs() {
        dialogsAdapter = DialogsListAdapter<Dialog>(imageLoader)
        var dialogs: List<Dialog> = emptyList() // Initialize as empty




        lifecycleScope.launch {
            groupDialogViewModel.allGroupDialogs.observe(viewLifecycleOwner) { dialogsData ->

                dialogs = dialogsData.map { fromGroupDialogEntity(it) }.sortedByDescending { it.lastMessage.createdAt }

                dialogsAdapter.setItems(dialogs)
            }
        }




        // Fetch data only once when the fragment starts

        dialogsAdapter.setOnDialogClickListener(this)
        dialogsAdapter.setOnDialogLongClickListener(this)
        dialogsAdapter.setDatesFormatter(this)

        dialogsList.setAdapter(dialogsAdapter)
    }

    private fun fromGroupDialogEntity(entity: GroupDialogEntity): Dialog {
        val users = convertUserEntitiesToUsers(entity.users)

        val usersList: List<User> = users
        val usersArrayList: ArrayList<User> = ArrayList(usersList)

        val lastMessage = entity.lastMessage?.let { convertMessageEntityToMessage(it) }

        return Dialog(
            entity.id,
            entity.dialogName,
            entity.dialogPhoto,
            usersArrayList,
            lastMessage,
            entity.unreadCount
        )
    }

    private fun convertMessageEntityToMessage(messageEntity: MessageEntity): Message {
        // Convert the properties from ChatMessageEntity to Message
        val id = messageEntity.id
        val user =
            User(
                messageEntity.userId,
                messageEntity.userName,
                messageEntity.user.avatar,
                messageEntity.user.online,
                messageEntity.user.lastSeen
            ) // You might need to fetch the user details
        val text = messageEntity.text
        val createdAt = Date(messageEntity.createdAt)

        val message = Message(id, user, text, createdAt)

        // Set additional properties like image and voice if needed
        if (messageEntity.imageUrl != null) {
            message.setImage(Message.Image(messageEntity.imageUrl!!))
        }

        if (messageEntity.videoUrl != null) {
            message.setVideo(Message.Video(messageEntity.videoUrl!!))
        }

        if (messageEntity.voiceUrl != null) {
            message.setVoice(Message.Voice(messageEntity.voiceUrl!!, messageEntity.voiceDuration))
        }

        return message
    }


    private fun convertUserEntitiesToUsers(userEntities: List<UserEntity>): List<User> {
        return userEntities.map { userEntity ->
            User(
                userEntity.id,
                userEntity.name,
                userEntity.avatar,
                userEntity.online,
                userEntity.lastSeen
            )
        }
    }


    private fun initAdapter() {
        val dialogsAdapter = DialogsListAdapter<Dialog>(imageLoader)

        val dialogs = DialogsFixtures.getDialogs().filter { it.users.size == 1 }
        val groupDialogs = DialogsFixtures.getDialogs().filter { it.users.size > 1 }

        dialogs.forEach {
            if (it.unreadCount > 0) {
                unreadCount += it.unreadCount
            }
        }

        groupDialogs.forEach {
            if (it.unreadCount > 0) {
                groupUnread += it.unreadCount
            }
        }

        getChatNavigationController()?.unreadCount(0, unreadCount)


        dialogsAdapter.setItems(dialogs)
        dialogsAdapter.setOnDialogClickListener(this)
        dialogsAdapter.setOnDialogLongClickListener { }
        dialogsAdapter.setDatesFormatter(this)

        dialogsList.setAdapter(dialogsAdapter)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PersonalChats.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
           GroupChats().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDialogLongClick(dialog: Dialog?) {
        if (dialog != null && selectedDialogs == 0) {
            selectDialog(dialog)
        }
    }

    override fun onBackButtonPressed() {
        selectedDialogs = 0
        dialogsAdapter.resetSelectionForAll()
    }
}