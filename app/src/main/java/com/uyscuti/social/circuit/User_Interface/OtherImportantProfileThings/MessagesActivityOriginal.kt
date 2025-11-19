//package com.uyscuti.social.circuit.User_Interface.OtherImportantProfileThings
//
//import android.Manifest
//import android.animation.ObjectAnimator
//import android.animation.ValueAnimator
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.ContentValues
//import android.content.Context
//import android.content.Intent
//import android.content.SharedPreferences
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.graphics.drawable.GradientDrawable
//import android.graphics.drawable.InsetDrawable
//import android.media.AudioRecord
//import android.media.MediaMetadataRetriever
//import android.media.MediaPlayer
//import android.media.MediaRecorder
//import android.media.MediaScannerConnection
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.os.Handler
//import android.os.Looper
//import android.os.ResultReceiver
//import android.provider.MediaStore
//import android.provider.OpenableColumns
//import android.text.TextWatcher
//import android.text.Editable
//import android.text.format.DateUtils
//import android.util.Log
//import android.util.TypedValue
//import android.view.MenuItem
//import android.view.View
//import android.view.animation.AccelerateDecelerateInterpolator
//import android.view.animation.AnimationUtils
//import android.view.animation.LinearInterpolator
//import android.view.inputmethod.InputMethodManager
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.ProgressBar
//import android.widget.SeekBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.activity.viewModels
//import androidx.annotation.OptIn
//import androidx.annotation.RequiresApi
//import androidx.annotation.RequiresExtension
//import androidx.cardview.widget.CardView
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NavUtils
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.core.graphics.drawable.DrawableCompat
//import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
//import androidx.core.provider.FontRequest
//import androidx.core.view.WindowCompat
//import androidx.emoji.text.EmojiCompat
//import androidx.emoji.text.FontRequestEmojiCompatConfig
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.lifecycleScope
//import androidx.loader.content.CursorLoader
//import androidx.media3.common.util.UnstableApi
//import androidx.navigation.ui.AppBarConfiguration
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.SimpleTarget
//import com.bumptech.glide.request.transition.Transition
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.snackbar.Snackbar
//import com.uyscuti.social.business.MainActivity
//import com.uyscuti.social.call.models.DataModel
//import com.uyscuti.social.call.models.DataModelType
//import com.uyscuti.social.call.repository.MainRepository
//import com.uyscuti.social.call.ui.CallActivity
//import com.uyscuti.social.chatsuit.R
//import com.uyscuti.social.chatsuit.messages.MessageInput
//import com.uyscuti.social.chatsuit.messages.MessagesList
//import com.uyscuti.social.chatsuit.messages.MessagesListAdapter
//import com.uyscuti.social.chatsuit.messages.MessagesListAdapter.STATUS_DELIVERED
//import com.uyscuti.social.chatsuit.messages.MessagesListAdapter.STATUS_SENT
//import com.uyscuti.social.chatsuit.utils.DateFormatter
//import com.uyscuti.social.circuit.MainMessagesActivity
//import com.uyscuti.social.circuit.calls.viewmodel.CallViewModel
//import com.uyscuti.social.circuit.data.model.Dialog
//import com.uyscuti.social.circuit.data.model.Message
//import com.uyscuti.social.circuit.data.model.User
//import com.uyscuti.social.circuit.databinding.ActivityMessagesBinding
//import com.uyscuti.social.circuit.presentation.DialogViewModel
//import com.uyscuti.social.circuit.presentation.GroupDialogViewModel
//import com.uyscuti.social.circuit.presentation.MessageViewModel
//import com.uyscuti.social.circuit.User_Interface.OtherUserProfile.OtherUserProfileAccount
//import com.uyscuti.social.circuit.User_Interface.media.PlayVideoActivity
//import com.uyscuti.social.circuit.User_Interface.media.ViewImagesActivity
//import com.uyscuti.social.circuit.User_Interface.uploads.AudioActivity
//import com.uyscuti.social.circuit.User_Interface.uploads.CameraActivity
//import com.uyscuti.social.circuit.User_Interface.uploads.DocumentsActivity
//import com.uyscuti.social.circuit.User_Interface.uploads.ImagesActivity
//import com.uyscuti.social.circuit.User_Interface.uploads.VideosActivity
//import com.uyscuti.social.circuit.model.PauseShort
//import com.uyscuti.social.circuit.utils.AudioDurationHelper.getFormattedDuration
//import com.uyscuti.social.circuit.utils.ChatManager
//import com.uyscuti.social.circuit.utils.Timer
//import com.uyscuti.social.circuit.utils.UserStatusManager
//import com.uyscuti.social.circuit.utils.audio_compressor.FFMPEG_AudioCompressor
//import com.uyscuti.social.circuit.utils.audiomixer.AudioMixer
//import com.uyscuti.social.circuit.utils.audiomixer.input.GeneralAudioInput
//import com.uyscuti.social.circuit.utils.deleteFiles
//import com.uyscuti.social.core.common.data.api.RemoteMessageRepository
//import com.uyscuti.social.core.common.data.api.RemoteMessageRepositoryImpl
//import com.uyscuti.social.core.common.data.api.Result
//import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
//import com.uyscuti.social.core.common.data.room.entity.MessageEntity
//import com.uyscuti.social.core.common.data.room.entity.UserEntity
//import com.uyscuti.social.core.local.utils.FileType
//import com.uyscuti.social.core.pushnotifications.socket.chatsocket.CoreChatSocketClient
//import com.uyscuti.social.network.api.models.Notification
//import com.uyscuti.social.network.api.response.userstatus.UserStatusResponse
//import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
//import com.uyscuti.social.network.eventmodels.DirectReplyEvent
//import com.vanniktech.emoji.EmojiEditText
//import com.vanniktech.emoji.EmojiManager
//import com.vanniktech.emoji.EmojiPopup
//import com.vanniktech.emoji.facebook.FacebookEmojiProvider
//import com.vanniktech.emoji.google.GoogleEmojiProvider
//import com.vanniktech.emoji.googlecompat.GoogleCompatEmojiProvider
//import com.vanniktech.emoji.ios.IosEmojiProvider
//import com.vanniktech.emoji.twitter.TwitterEmojiProvider
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.NonCancellable
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import org.greenrobot.eventbus.EventBus
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//import java.io.IOException
//import java.lang.Math.sqrt
//import java.net.HttpURLConnection
//import java.net.URL
//import java.net.URLDecoder
//import java.net.URLEncoder
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import java.util.TimeZone
//import javax.inject.Inject
//import kotlin.random.Random
//
//@UnstableApi
//@AndroidEntryPoint
//class MessagesActivity : MainMessagesActivity(), MessageInput.InputListener,
//
//    MessageInput.EmojiListener, MessageInput.VoiceListener, MessageInput.AttachmentsListener,
//    DateFormatter.Formatter, CoreChatSocketClient.ChatSocketEvents,
//    MessagesListAdapter.MessageSentListener<Message>,
//    MessagesListAdapter.DateFormatterListener,
//    MessagesListAdapter.OnDownloadListener<Message>,
//    MessagesListAdapter.OnMediaClickListener<Message>,
//    MessagesListAdapter.OnAudioPlayListener<Message> , ChatManager.ChatManagerListener{
//
//    private val MAX_RETRY_COUNT = 3
//    private val REQUEST_CODE = 558
//    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 12
//    private val MY_MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 202
//    private val IMAGES_REQUEST_CODE = 2023
//    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 101
//    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
//
//
//    companion object {
//
//        const val SENT = 1
//        const val DELIVERED = 2
//        const val READ = 3
//
//
//        fun open(context: Context, dialogName: String, dialog: Dialog?, temporally: Boolean) {
//            val intent = Intent(context, MessagesActivity::class.java)
//
//            if (dialog != null) {
//
//
//                intent.putExtra("chatId", dialog.id)
//                intent.putExtra("dialogName", dialog.dialogName)
//                intent.putExtra("dialogPhoto", dialog.dialogPhoto)
//                intent.putExtra("isGroup", dialog.users.size > 1)
//                intent.putExtra("temporally", temporally)
//
//                if (dialog.users.isNotEmpty()) {
//                    intent.putExtra("firstUserId", dialog.users.first().id)
//                    intent.putExtra("firstUserName", dialog.users.first().name)
//                    intent.putExtra("firstUserAvatar", dialog.users.first().avatar)
//                }
//
//                dialog.lastMessage?.let {
//                    intent.putExtra("lastMessageId", it.id)
//                }
//            }
//
//            context.startActivity(intent)
//        }
//    }
//
//    private lateinit var appBarConfiguration: AppBarConfiguration
//    private lateinit var binding: ActivityMessagesBinding
//
//    private lateinit var messagesList: MessagesList
//    private lateinit var emojiPopup: EmojiPopup
//    private lateinit var inputMethodManager: InputMethodManager
//
//    private var observedMessages: ArrayList<String> = arrayListOf()
//
//    private lateinit var settings: SharedPreferences
//
//    private val messageViewModel: MessageViewModel by viewModels()
//    private val dialogViewModel: DialogViewModel by viewModels()
//    private val groupDialogViewModel: GroupDialogViewModel by viewModels()
//
//    private val PREFS_NAME = "LocalSettings"
//
//    private var mediaPlayer: MediaPlayer? = null
//    private lateinit var runnable: Runnable
//    private lateinit var handler: Handler
//    private var isPlaying = false
//    private lateinit var sendFileProgressBar: ProgressBar
//
//    private var userEntity: UserEntity? = null
//
//
//    private var mediaRecorder: MediaRecorder? = null
//    private var isRecording = false
//
//    private var lastSeen: Date? = null
//
//
//    private lateinit var username: String
//
//    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
//    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
//    private lateinit var audioPickerLauncher: ActivityResultLauncher<Intent>
//    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>
//    private lateinit var docsPickerLauncher: ActivityResultLauncher<Intent>
//    private lateinit var openFilePicker: ActivityResultLauncher<Intent>
//
//    private val TAG = "MessagesActivity"
//
//    private lateinit var groupAdminId: String
//    private lateinit var groupCreatedAt: String
//
//    @Inject
//    lateinit var retrofitIns: RetrofitInstance
//
//    private var currentAudio: String? = null
//
//    private var voiceNoteState: VoiceNoteState = VoiceNoteState.IDLE
//    private var isPaused = false
//    private var isAudioVNPlaying = false
//    private var vnRecordAudioPlaying = false
//    private var vnRecordProgress = 0
//    private var sending = false
//    private var wasPaused = false
//    private var player: MediaPlayer? = null
//    private var outputVnFile = ""
//    private val recordedAudioFiles = mutableListOf<String>()
//    private val waveBars = mutableListOf<View>()
//
//    private var playbackTimerRunnable: Runnable? = null
//    private var recordingStartTime = 0L
//    private var recordingElapsedTime = 0L
//    private var isListeningToAudio = false
//    private var audioRecord: AudioRecord? = null
//    private val maxWaveBars = 100
//    private var mixingCompleted = false
//    private var isAudioVNPaused = false
//    private var isDurationOnPause = false
//    private var isOnRecordDurationOnPause = false
//    private var totalRecordedDuration = 0L
//
//    // TIMERS & HANDLERS
//
//    private lateinit var timer: Timer
//    private var currentHandler: Handler? = null
//    private val timerHandler = Handler(Looper.getMainLooper())
//
//    internal enum class VoiceNoteState {
//        IDLE,
//        RECORDING,
//        PLAYING,
//        PAUSED
//    }
//
//    @Inject
//    lateinit var mainRepository: MainRepository
//
//    @Inject
//    lateinit var coreChatSocketClient: CoreChatSocketClient
//
//    @Inject // or another appropriate scope
//    lateinit var chatManager: ChatManager
//
//    private lateinit var callViewModel: CallViewModel
//
//    private var userStatusManager: UserStatusManager? = null
//
//
//    private lateinit var remoteMessageRepository: RemoteMessageRepository
//
//    private var dialog: Dialog? = null
//
//    private var emojiShowing = false
//
//    private var isVnRecording = false
//    private var audioRecorder: MediaRecorder? = null
//    private lateinit var outputFile: String
//
//    private var isGroup = false
//    private var isTemporally = false
//
//    private lateinit var chatId: String
//
//    private lateinit var chatName: String
//
//    private lateinit var myId: String
//
//
//    fun setMessageStatus(messageStatus: Int, tickImageView: ImageView) {
//        when (messageStatus) {
//            SENT -> tickImageView.setImageResource(R.drawable.ic_tick_single)
//            DELIVERED -> tickImageView.setImageResource(R.drawable.ic_tick_double)
//            READ -> tickImageView.setImageResource(R.drawable.ic_tick_double_blue)
//        }
//    }
//
//    private fun dpToPx(dp: Int): Int {
//        return (dp * resources.displayMetrics.density).toInt()
//    }
//
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMessagesBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        messagesList = binding.messagesList
//
//        EventBus.getDefault().register(this)
//
//        callViewModel = ViewModelProvider(this)[CallViewModel::class.java]
//
//        settings = getSharedPreferences(PREFS_NAME, 0)
//
//        myId = localStorage.getUserId()
//        username = localStorage.getUsername()
//
//        remoteMessageRepository = RemoteMessageRepositoryImpl(retrofitIns)
//
//        handler = Handler(Looper.getMainLooper())
//
//        runnable = Runnable { }
//
//        setSupportActionBar(binding.toolbar)
//
//        installTwitter()
//
//        chatId = intent.getStringExtra("chatId") ?: ""
//        chatName = intent.getStringExtra("dialogName") ?: ""
//        val dialogPhoto = intent.getStringExtra("dialogPhoto") ?: ""
//        isGroup = intent.getBooleanExtra("isGroup", false)
//        isTemporally = intent.getBooleanExtra("temporally", false)
//
//        if (chatId.isEmpty()) {
//            Log.e(TAG, "Missing chat ID")
//            finish()
//            return
//        }
//
//        setChatId(chatId)
//        setMyId(myId)
//
//        Log.d("ChatId", " ChatId : $chatId, Chat Name : $chatName")
//
//        setIsGroup(isGroup)
//
//        supportActionBar?.title = chatName
//
//        if (isGroup) {
//            supportActionBar?.subtitle = ""
//            binding.toolbar.setSubtitleTextColor(resources.getColor(R.color.white_two))
//
//            CoroutineScope(Dispatchers.IO).launch {
//                val group = groupDialogViewModel.getGroupDialog(chatId)
//                groupAdminId = group.adminId
//                groupCreatedAt = group.createdAt.toString()
//
//                val form = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
//                form.timeZone = TimeZone.getTimeZone("UTC")
//
//                val date = Date(group.createdAt)
//
//                groupCreatedAt = formatDate(date)
//
//                withContext(Dispatchers.Main) {
//                    // Update subtitle with members if needed
//                }
//            }
//        } else {
//            val friendId = intent.getStringExtra("firstUserId") ?: ""
//            val friendName = intent.getStringExtra("firstUserName") ?: ""
//            val friendAvatar = intent.getStringExtra("firstUserAvatar") ?: ""
//
//            if (friendId.isNotEmpty()) {
//                userStatusManager = UserStatusManager(retrofitIns, friendId) { userStatus ->
//                    Log.d("UserStatus", "UserStatus In Messages Activity: $userStatus")
//
//                    if (userStatus != null) {
//                        runOnUiThread {
//                            if (userStatus.isOnline) {
//                                supportActionBar?.subtitle = "online"
//                                binding.toolbar.setSubtitleTextColor(resources.getColor(R.color.white_two))
//                            } else {
//                                val formattedLastSeen = formatLastSeenDate(userStatus.lastSeen)
//                                supportActionBar?.subtitle = formattedLastSeen
//                                binding.toolbar.setSubtitleTextColor(resources.getColor(R.color.white_two))
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
//
//        val navigationIcon = ContextCompat.getDrawable(this, com.uyscuti.social.circuit.R.drawable.baseline_arrow_back_ios_24)
//
//        navigationIcon?.let {
//            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
//
//            val wrappedDrawable = DrawableCompat.wrap(it)
//            DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(this, R.color.white))
//            val drawableMargin = InsetDrawable(wrappedDrawable, 0, 0, 0, 0)
//            binding.toolbar.navigationContentDescription = "Navigate up"
//            binding.toolbar.navigationIcon = drawableMargin
//        }
//
//        binding.toolbar.setOnClickListener {
//            viewUser()
//        }
//
//        Glide.with(this)
//            .asBitmap()
//            .load(dialogPhoto)
//            .centerCrop()
//            .override(size, size)
//            .into(object : SimpleTarget<Bitmap>() {
//                override fun onResourceReady(
//                    resource: Bitmap,
//                    transition: Transition<in Bitmap>?
//                ) {
//                    val drawable = RoundedBitmapDrawableFactory.create(resources, resource)
//                    drawable.isCircular = true
//                    val marginDrawable = InsetDrawable(drawable, 0, 0, 10, 0)
//                    binding.toolbar.logo = marginDrawable
//                }
//            })
//
//        binding.toolbar.setContentInsetsRelative(0, 0)
//
//        binding.toolbar.setNavigationOnClickListener {
//            onBackPressed()
//        }
//
//        val rootView = binding.container
//
//        // Setup emoji popup
//        val emojiEditText = binding.inputEditText
//        emojiPopup = EmojiPopup(rootView, emojiEditText)
//        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//
//        initAdapter()
//        setupMessageInput()
//
//        getFilePath()
//
//        observeSendingMessagesI()
//
//        if (!isTemporally) {
//            observeTemporallyMessages(chatName)
//        }
//
//        dialogViewModel.updatedDialog.observe(this, Observer { updatedDialog ->
//            if (updatedDialog != null) {
//                Log.d(TAG, "Updated Dialog : $updatedDialog")
//                chatId = updatedDialog.id
//                trigger()
//            }
//        })
//    }
//
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    private fun setupMessageInput() {
//        // Access views through binding object
//        val inputEditText = binding.inputEditText
//        val sendBtn = binding.sendBtn
//        val sendCard = binding.sendCard
//        val vnCard = binding.vnCard
//        val voiceNote = binding.voiceNote
//        val attachment = binding.attachment
//        val emoji = binding.emoji
//
//        // Add null checks for safety
//        if (inputEditText == null) {
//            Log.e(TAG, "inputEditText is null - check your layout file")
//            return
//        }
//
//        // Show/hide send button based on text
//        inputEditText.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s?.length ?: 0 > 0) {
//                    sendCard?.visibility = View.VISIBLE
//                    sendBtn?.visibility = View.VISIBLE
//                    vnCard?.visibility = View.GONE
//                } else {
//                    sendCard?.visibility = View.GONE
//                    sendBtn?.visibility = View.GONE
//                    vnCard?.visibility = View.VISIBLE
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        // Send button click
//        sendBtn?.setOnClickListener {
//            val text = inputEditText.text.toString()
//            if (text.isNotEmpty()) {
//                onSubmit(text)
//                inputEditText.setText("")
//            }
//        }
//
//        // Voice note click - INITIAL START ONLY
//        voiceNote?.setOnClickListener {
//            if (voiceNoteState == VoiceNoteState.IDLE) {
//                // Show the VN recording layout
//                binding.VNLayout.visibility = View.VISIBLE
//                binding.inputContainer.visibility = View.GONE
//
//                // Start recording
//                startRecording()
//            }
//        }
//
//        // Setup voice note control buttons (includes recordVN button)
//        setupVoiceNoteControls()
//
//        // Attachment click
//        attachment?.setOnClickListener {
//            onAddAttachments()
//        }
//
//        // Emoji click
//        emoji?.setOnClickListener {
//            onAddEmoji()
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    private fun setupVoiceNoteControls() {
//        // THIS IS THE MISSING CLICK LISTENER!
//        // recordVN button - handles pause/resume during recording
//        binding.recordVN?.setOnClickListener {
//            Log.d(TAG, "recordVN clicked, current state: $voiceNoteState")
//            handleVoiceNoteClick()
//            when (voiceNoteState) {
//                VoiceNoteState.RECORDING -> {
//                    // Pause recording
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        Log.d(TAG, "Pausing recording")
//                        pauseRecording()
//                    }
//                }
//                VoiceNoteState.PAUSED -> {
//                    // Resume recording
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        Log.d(TAG, "Resuming recording")
//                        resumeRecording()
//                    }
//                }
//                else -> {
//                    Log.d(TAG, "recordVN clicked but state is $voiceNoteState")
//                }
//            }
//        }
//
//        // Play/Pause button for recorded audio (after pausing)
//        binding.playVnAudioBtn?.setOnClickListener {
//            Log.d("playVnAudioBtn", "Play VN button clicked")
//            handleVoiceNoteClick()
//            when {
//                !isAudioVNPlaying -> {
//                    Log.d("playVnAudioBtn", "Starting playback")
//                    startPlaying(outputVnFile)
//                }
//                else -> {
//                    Log.d("playVnAudioBtn", "Pausing VN")
//                    vnRecordAudioPlaying = true
//                    val currentProgress = player?.currentPosition ?: vnRecordProgress
//                    vnRecordProgress = currentProgress
//                    pauseVn(currentProgress)
//                }
//            }
//        }
//
//        // Delete button
//        binding.deleteVN?.setOnClickListener {
//            Log.d(TAG, "Delete button clicked")
//            deleteRecording()
//        }
//
//        // Send button for voice note
//        binding.sendVN?.setOnClickListener {
//            Log.d(TAG, "Send VN button clicked")
//
//            if (sending) {
//                Log.d(TAG, "Already sending, ignoring click")
//                return@setOnClickListener
//            }
//
//            // ALWAYS stop recording and send when send button is clicked
//            Log.d(TAG, "Stopping and sending voice note")
//            stopRecordingVoiceNote()
//        }
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun handleVoiceNoteClick() {
//        when (voiceNoteState) {
//            VoiceNoteState.IDLE -> {
//                // Show the VN recording layout
//                binding.VNLayout.visibility = View.VISIBLE
//                binding.inputContainer.visibility = View.GONE
//
//                // Start recording
//                startRecording()
//            }
//            VoiceNoteState.RECORDING -> {
//                // Pause recording
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    pauseRecording()
//                }
//            }
//            VoiceNoteState.PAUSED -> {
//                // Resume recording
//                resumeRecording()
//            }
//            VoiceNoteState.PLAYING -> {
//                // Pause playback
//                val currentProgress = player?.currentPosition ?: vnRecordProgress
//                vnRecordProgress = currentProgress
//                pauseVn(currentProgress)
//            }
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun startRecording() {
//        // Check for microphone permission
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                REQUEST_RECORD_AUDIO_PERMISSION
//            )
//            return
//        }
//
//        try {
//            // Initialize recording state
//            isRecording = true
//            isPaused = false
//            isListeningToAudio = true
//            recordingStartTime = System.currentTimeMillis()
//            recordingElapsedTime = 0L
//
//            // Clear previous recordings
//            recordedAudioFiles.clear()
//
//            // Create new output file
//            outputFile = com.uyscuti.social.circuit.utils.getOutputFilePath("rec")
//            recordedAudioFiles.add(outputFile)
//
//            // Setup MediaRecorder
//            mediaRecorder = MediaRecorder().apply {
//                setAudioSource(MediaRecorder.AudioSource.MIC)
//                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                setOutputFile(outputFile)
//                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                setAudioEncodingBitRate(128000)
//                setAudioSamplingRate(44100)
//                prepare()
//                start()
//            }
//
//            // Update UI
//            updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)
//            binding.recordVN.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_white_24)
//            binding.sendVN.setBackgroundResource(com.uyscuti.social.circuit.R.drawable.ic_ripple)
//            binding.sendVN.isClickable = true
//
//            // Initialize waveform
//            initializeDottedWaveform()
//
//            // Start timer
//            updateRecordingTimer()
//
//            // Start audio listening in background thread
//            Thread {
//                listenToAudio()
//            }.start()
//
//            Log.d("Recording", "Recording started successfully")
//
//        } catch (e: Exception) {
//            Log.e("Recording", "Error starting recording: ${e.message}", e)
//            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
//            isRecording = false
//            binding.VNLayout.visibility = View.GONE
//            binding.inputContainer.visibility = View.VISIBLE
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun pauseRecording() {
//        Log.d(TAG, "pauseRecording called")
//        if (isRecording && !isPaused) {
//            try {
//                isListeningToAudio = false
//
//                // Calculate elapsed time before stopping
//                val currentTime = System.currentTimeMillis()
//                recordingElapsedTime += (currentTime - recordingStartTime)
//
//                Log.d(TAG, "Elapsed time: $recordingElapsedTime ms")
//
//                mediaRecorder?.let { recorder ->
//                    try {
//                        recorder.stop()
//                        recorder.release()
//                        Log.d(TAG, "MediaRecorder stopped and released")
//                    } catch (e: Exception) {
//                        Log.e(TAG, "Error stopping recorder: $e")
//                    }
//                }
//                mediaRecorder = null
//
//                isPaused = true
//                isRecording = false  // Important: set to false when paused
//
//                // Stop the recording timer
//                timerHandler.removeCallbacksAndMessages(null)
//
//                // Update both timers to show the current recorded duration
//                runOnUiThread {
//                    val seconds = (recordingElapsedTime / 1000) % 60
//                    val minutes = (recordingElapsedTime / 1000) / 60
//                    val formatted = String.format("%02d:%02d", minutes, seconds)
//                    binding.recordingTimerTv.text = formatted
//                    binding.pausedTimerTv.text = formatted
//                    Log.d(TAG, "Timer updated to: $formatted")
//                }
//
//                updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
//
//                // Change icon to microphone (to indicate resume will start recording again)
//                binding.recordVN.setImageResource(com.uyscuti.social.circuit.R.drawable.mic_2)
//                binding.sendVN.setBackgroundResource(com.uyscuti.social.circuit.R.drawable.ic_ripple)
//                binding.sendVN.isClickable = true
//
//                Log.d(TAG, "Recordings: ${recordedAudioFiles.size}")
//                mixVoiceNote()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error in pauseRecording: $e")
//                e.printStackTrace()
//            }
//        } else {
//            Log.d(TAG, "pauseRecording: Cannot pause - isRecording=$isRecording, isPaused=$isPaused")
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    private fun resumeRecording() {
//        Log.d(TAG, "resumeRecording called")
//        if (isPaused) {
//            try {
//                // Create new recording file for this segment
//                outputFile = com.uyscuti.social.circuit.utils.getOutputFilePath("rec")
//                Log.d(TAG, "New recording file: $outputFile")
//
//                mediaRecorder = MediaRecorder().apply {
//                    setAudioSource(MediaRecorder.AudioSource.MIC)
//                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                    setOutputFile(outputFile)
//                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//                    setAudioEncodingBitRate(128000)
//                    setAudioSamplingRate(44100)
//                    prepare()
//                    start()
//                }
//
//                isPaused = false
//                isListeningToAudio = true
//                isRecording = true
//
//                // Resume timer from where it left off
//                recordingStartTime = System.currentTimeMillis()
//                updateRecordingTimer()
//
//                binding.playVNRecorded.visibility = View.GONE
//                binding.recordingTimerTv.visibility = View.VISIBLE
//
//                binding.playVnAudioBtn.setImageResource(com.uyscuti.social.circuit.R.drawable.play_svgrepo_com)
//                binding.recordVN.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_white_24)
//
//                updateVoiceNoteUserInterfaceState(VoiceNoteState.RECORDING)
//
//                recordedAudioFiles.add(outputFile)
//                Log.d(TAG, "Added file to recordings, total: ${recordedAudioFiles.size}")
//
//                // Resume audio listening
//                Thread {
//                    listenToAudio()
//                }.start()
//
//                Log.d(TAG, "Recording resumed successfully")
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error resuming recording: ${e.message}", e)
//                Toast.makeText(this, "Failed to resume recording", Toast.LENGTH_SHORT).show()
//                isPaused = true
//                isRecording = false
//            }
//        } else {
//            Log.d(TAG, "resumeRecording: Cannot resume - isPaused=$isPaused")
//        }
//    }
//
//    private fun deleteRecording() {
//        val TAG = "Recording"
//        try {
//            isListeningToAudio = false
//
//            // Stop all timers
//            timerHandler.removeCallbacksAndMessages(null)
//            playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }
//
//            mediaRecorder?.apply {
//                try {
//                    stop()
//                    release()
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error stopping recorder: $e")
//                }
//            }
//            mediaRecorder = null
//
//            isRecording = false
//            isPaused = false
//            isAudioVNPlaying = false
//
//            // Reset timer variables
//            recordingStartTime = 0L
//            recordingElapsedTime = 0L
//
//            binding.recordVN.setImageResource(com.uyscuti.social.circuit.R.drawable.mic_2)
//            binding.sendVN.setBackgroundResource(com.uyscuti.social.circuit.R.drawable.ic_ripple_disabled)
//            binding.sendVN.isClickable = false
//
//            updateVoiceNoteUserInterfaceState(VoiceNoteState.IDLE)
//
//            binding.recordingTimerTv.text = "00:00"
//            binding.pausedTimerTv.text = "00:00"
//
//            // Hide VN layout and show input container
//            binding.VNLayout.visibility = View.GONE
//            binding.inputContainer.visibility = View.VISIBLE
//
//            Log.d(TAG, "Recordings deleted: ${recordedAudioFiles.size}")
//            deleteVn()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e(TAG, "Error deleting recording: $e")
//        }
//    }
//
//    @SuppressLint("DefaultLocale")
//    private fun updateRecordingTimer() {
//        timerHandler.post(object : kotlinx.coroutines.Runnable {
//            override fun run() {
//                if (isRecording && !isPaused) {
//                    val currentTime = System.currentTimeMillis()
//                    val elapsed = recordingElapsedTime + (currentTime - recordingStartTime)
//
//                    val seconds = (elapsed / 1000) % 60
//                    val minutes = (elapsed / 1000) / 60
//
//                    val formatted = String.format("%02d:%02d", minutes, seconds)
//                    binding.recordingTimerTv.text = formatted
//
//                    timerHandler.postDelayed(this, 100) // Update every 100ms
//                }
//            }
//        })
//    }
//
//    @SuppressLint("DefaultLocale")
//    private fun updatePlaybackTimer() {
//        // Remove any existing callbacks first
//        playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }
//
//        playbackTimerRunnable = object : kotlinx.coroutines.Runnable {
//            override fun run() {
//                if (isAudioVNPlaying && player != null) {
//                    try {
//                        val currentPosition = player?.currentPosition ?: 0
//                        val currentMinutes = (currentPosition / 1000) / 60
//                        val currentSeconds = (currentPosition / 1000) % 60
//                        binding.pausedTimerTv.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
//                        timerHandler.postDelayed(this, 100)
//                    } catch (e: Exception) {
//                        Log.e("PlaybackTimer", "Error updating timer: ${e.message}")
//                    }
//                }
//            }
//        }
//        timerHandler.post(playbackTimerRunnable!!)
//    }
//
//    private fun animatePlaybackWaves() {
//        val duration = player?.duration?.toLong() ?: 0L
//        if (duration > 0) {
//            // Animate existing waveforms during playback
//            waveBars.forEachIndexed { index, bar ->
//                val storedHeight = bar.tag as? Float ?: 1.0f
//                val heights = floatArrayOf(
//                    storedHeight * 0.8f,
//                    storedHeight * 1.0f,
//                    storedHeight * 0.9f,
//                    storedHeight * 1.1f,
//                    storedHeight * 0.8f
//                )
//                val animator = ObjectAnimator.ofFloat(bar, "scaleY", *heights).apply {
//                    this.duration = 800 + (index * 20L)
//                    repeatCount = ObjectAnimator.INFINITE
//                    repeatMode = ObjectAnimator.RESTART
//                    interpolator = AccelerateDecelerateInterpolator()
//                }
//                animator.start()
//                bar.tag = animator
//            }
//
//            // Scroll animation from right to left
//            binding.waveformScrollView.post {
//                val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
//                if (maxScroll > 0) {
//                    val scrollAnimator = ValueAnimator.ofInt(maxScroll, 0).apply {
//                        this.duration = duration
//                        interpolator = LinearInterpolator()
//                        addUpdateListener { animation ->
//                            if (isAudioVNPlaying) {
//                                val scrollX = animation.animatedValue as Int
//                                binding.waveformScrollView.scrollTo(scrollX, 0)
//                            }
//                        }
//                    }
//                    scrollAnimator.start()
//                    binding.waveformScrollView.tag = scrollAnimator
//                }
//            }
//        }
//    }
//
//    private fun isVoiceNoteReady(): Boolean {
//        return !sending && (isRecording || isPaused || wasPaused)
//    }
//
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    private fun stopRecordingVoiceNote() {
//        val TAG = "StopRecording"
//        try {
//            // Stop media recorder
//            if (mediaRecorder != null) {
//                mediaRecorder?.apply {
//                    stop()
//                    release()
//                }
//                mediaRecorder = null
//            }
//
//            isRecording = false
//            isPaused = false
//            stopWaveDotsAnimation()
//            binding.recordingLayout.visibility = View.GONE
//            binding.recordingTimerTv?.text = "00:00"
//            binding.recordVN?.setImageResource(com.uyscuti.social.call.R.drawable.ic_mic_on)
//            binding.sendVN?.setBackgroundResource(com.uyscuti.social.circuit.R.drawable.ic_ripple_disabled)
//            binding.sendVN?.isClickable = false
//
//            if (player?.isPlaying == true) {
//                stopPlaying()
//            }
//
//            Log.d(TAG, "stopRecording: recorded files size ${recordedAudioFiles.size}")
//
//            // Select appropriate audio file
//            val audioFilePath = if (mixingCompleted && File(outputVnFile).exists()) {
//                Log.d(TAG, "Using mixed audio file: $outputVnFile")
//                outputVnFile
//            } else {
//                Log.d(TAG, "Using single recording file: $outputFile")
//                outputFile
//            }
//
//            val file = File(audioFilePath)
//            if (!file.exists()) {
//                Log.e(TAG, "Audio file not found: $audioFilePath")
//                Toast.makeText(this, "Voice note file not found", Toast.LENGTH_SHORT).show()
//                sending = false
//                binding.VNLayout.visibility = View.GONE
//                binding.inputContainer.visibility = View.VISIBLE
//                return
//            }
//
//            // Get duration in seconds
//            val durationMs = getDurationFromMediaMetadata(audioFilePath)
//            val durationSeconds = (durationMs / 1000).toInt()
//            Log.d(TAG, "Voice note duration: $durationSeconds seconds")
//
//            // Create message entities - SAME as text message flow
//            val messageId = "rec${Random.Default.nextInt()}"
//            Log.d("MessageSent", "Message Id : $messageId")
//
//            val date = Date(System.currentTimeMillis())
//            val avatar = settings.getString("avatar", "avatar").toString()
//
//            val user = User("0", "You", avatar, true, date)
//            val message = Message(
//                messageId,
//                user,
//                null, // No text content for voice note
//                date
//            )
//            message.status = "Sending"
//            message.setVoice(Message.Voice(audioFilePath, durationSeconds))
//
//            val userEntity = UserEntity(
//                "0",
//                "You",
//                avatar,
//                date,
//                true
//            )
//
//            val voiceMessageEntity = MessageEntity(
//                id = messageId,
//                chatId = chatId,
//                userName = "You",
//                user = userEntity,
//                userId = myId,
//                text = "", // Empty text for voice note
//                createdAt = System.currentTimeMillis(),
//                imageUrl = null,
//                voiceUrl = audioFilePath, // Local path initially
//                voiceDuration = durationSeconds,
//                status = "Sending",
//                videoUrl = null,
//                audioUrl = null,
//                docUrl = null,
//                fileSize = file.length()
//            )
//
//            // SAME flow as text messages: Insert to DB and add to UI
//            CoroutineScope(Dispatchers.IO).launch {
//                insertMessage(voiceMessageEntity)
//                updateLastMessage(isGroup, chatId, voiceMessageEntity)
//            }
//
//            super.messagesAdapter?.addToStart(message, true)
//
//            // Hide VN recording UI
//            binding.VNLayout.visibility = View.GONE
//            binding.inputContainer.visibility = View.VISIBLE
//
//            // Check if compression is needed
//            val fileSizeInMB = file.length() / (1024 * 1024)
//            Log.d(TAG, "Voice note file size: $fileSizeInMB MB")
//
//            if (fileSizeInMB > 2) {
//                Log.d(TAG, "Voice note needs compression")
//                val outputFileName = "AUD${System.currentTimeMillis()}.mp3"
//                val outputFilePath = File(cacheDir, outputFileName)
//
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val compressor = FFMPEG_AudioCompressor()
//                    val isCompressionSuccessful = compressor.compress(audioFilePath, outputFilePath.absolutePath)
//
//                    val fileToSend = if (isCompressionSuccessful) {
//                        Log.d(TAG, "Compression successful, using compressed file")
//                        outputFilePath
//                    } else {
//                        Log.e(TAG, "Compression failed, using original file")
//                        file
//                    }
//
//                    // Send through the unified pipeline
//                    sendVoiceNoteMessage(fileToSend, message, voiceMessageEntity)
//                }
//            } else {
//                // Send directly without compression
//                Log.d(TAG, "Voice note doesn't need compression")
//                lifecycleScope.launch(Dispatchers.IO) {
//                    sendVoiceNoteMessage(file, message, voiceMessageEntity)
//                }
//            }
//
//            // Clean up recording state
//            wasPaused = false
//            mixingCompleted = false
//            recordedAudioFiles.clear()
//
//            Log.d(TAG, "Voice note processing initiated")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Error in stopRecording: ${e.message}", e)
//            Toast.makeText(this, "Failed to send voice note", Toast.LENGTH_SHORT).show()
//            sending = false
//            binding.VNLayout.visibility = View.GONE
//            binding.inputContainer.visibility = View.VISIBLE
//        }
//    }
//
//    // Method to get audio duration
//    private fun getDurationFromMediaMetadata(audioFilePath: String): Long {
//        return try {
//            val retriever = MediaMetadataRetriever()
//            retriever.setDataSource(audioFilePath)
//            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//            retriever.release()
//            duration?.toLongOrNull() ?: 0L
//        } catch (e: Exception) {
//            Log.e("getDuration", "Error getting audio duration: ${e.message}", e)
//            0L
//        }
//    }
//
//
//    private fun sendVoiceNoteMessage(
//        file: File,
//        message: Message,
//        dBMessage: MessageEntity
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(NonCancellable) {
//                try {
//                    // Create MultipartBody.Part
//                    val requestFile = file.asRequestBody("audio/mp3".toMediaTypeOrNull())
//                    val body = MultipartBody.Part.createFormData(
//                        "attachments",
//                        file.name,
//                        requestFile
//                    )
//
//                    Log.d(TAG, "Sending voice note: ${file.name}, size: ${file.length()} bytes")
//
//                    // SAME as sendTextMessage - use repository
//                    when (val result = remoteMessageRepository.sendAttachment(
//                        chatId = chatId,
//                        message = null,
//                        filePath = body
//                    )) {
//                        is Result.Success -> {
//                            Log.d(TAG, "Voice note sent successfully")
//
//                            // SAME as text: notify adapter
//                            withContext(Dispatchers.Main) {
//                                super.messagesAdapter?.notifyMessageSent(message)
//                            }
//
//                            // SAME as text: update status in DB
//                            messageViewModel.updateMessageStatus(dBMessage)
//                        }
//
//                        is Result.Error -> {
//                            Log.e(TAG, "Failed to send voice note: ${result.exception.message}")
//
//                            // SAME as text: handle error
//                            withContext(Dispatchers.Main) {
//                                message.status = "Failed"
//                                dBMessage.status = "Failed"
//                                messageViewModel.updateMessageStatus(dBMessage)
//                                super.messagesAdapter?.notifyDataSetChanged()
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.e(TAG, "Exception sending voice note: ${e.message}", e)
//                    withContext(Dispatchers.Main) {
//                        message.status = "Failed"
//                        dBMessage.status = "Failed"
//                        messageViewModel.updateMessageStatus(dBMessage)
//                        super.messagesAdapter?.notifyDataSetChanged()
//                    }
//                } finally {
//                    sending = false
//                }
//            }
//        }
//    }
//
//
//    private fun stopPlaying() {
//        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
//        scrollAnimator?.cancel()
//
//        binding.playVnAudioBtn.setImageResource(com.uyscuti.social.circuit.R.drawable.play_svgrepo_com)
//        player?.release()
//        player = null
//        isAudioVNPlaying = false
//        vnRecordAudioPlaying = false
//        isOnRecordDurationOnPause = false
//
//        stopWaveDotsAnimation()
//        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
//
//        stopPlaybackTimerRunnable()
//        vnRecordProgress = 0
//    }
//
//    private fun stopWaveDotsAnimation() {
//        waveBars.forEach { bar ->
//            (bar.tag as? ObjectAnimator)?.cancel()
//        }
//    }
//
//    private fun stopPlaybackTimerRunnable() {
//        playbackTimerRunnable?.let { timerHandler.removeCallbacks(it) }
//        playbackTimerRunnable = null
//    }
//
//    @OptIn(androidx.media3.common.util.UnstableApi::class)
//    private fun updateVoiceNoteUserInterfaceState(newState: VoiceNoteState) {
//        voiceNoteState = newState
//
//        when (newState) {
//            VoiceNoteState.RECORDING -> {
//                binding.recordingTimerTv.visibility = View.VISIBLE
//                binding.playVNRecorded.visibility = View.GONE
//                binding.waveformScrollView.visibility = View.VISIBLE
//                binding.waveDotsContainer.visibility = View.VISIBLE
//            }
//
//            VoiceNoteState.PLAYING -> {
//                binding.recordingTimerTv.visibility = View.GONE
//                binding.playVNRecorded.visibility = View.VISIBLE
//                binding.playVnAudioBtn.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_black)
//                binding.waveformScrollView.visibility = View.VISIBLE
//                binding.waveDotsContainer.visibility = View.VISIBLE
//            }
//
//            VoiceNoteState.PAUSED -> {
//                binding.recordingTimerTv.visibility = View.GONE
//                binding.playVNRecorded.visibility = View.VISIBLE
//                binding.playVnAudioBtn.setImageResource(com.uyscuti.social.circuit.R.drawable.play_svgrepo_com)
//                binding.waveformScrollView.visibility = View.VISIBLE
//                binding.waveDotsContainer.visibility = View.VISIBLE
//
//                // Scroll to left to show full waveform when paused
//                binding.waveformScrollView.post {
//                    binding.waveformScrollView.scrollTo(0, 0)
//                }
//            }
//
//            VoiceNoteState.IDLE -> {
//                binding.recordingLayout.visibility = View.GONE
//                clearWaveform()
//            }
//        }
//    }
//
//    private fun clearWaveform() {
//        waveBars.forEach { bar ->
//            (bar.tag as? ObjectAnimator)?.cancel()
//        }
//        binding.waveDotsContainer.removeAllViews()
//        waveBars.clear()
//    }
//
//    @SuppressLint("DefaultLocale")
//    private fun pauseVn(progress: Int) {
//        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
//        scrollAnimator?.cancel()
//
//        player?.pause()
//        player?.seekTo(progress)
//
//        isAudioVNPlaying = false
//        isAudioVNPaused = true
//
//        stopPlaybackTimerRunnable()
//
//        // Stop animations but keep waveforms visible
//        waveBars.forEach { bar ->
//            (bar.tag as? ObjectAnimator)?.cancel()
//            val storedHeight = bar.tag as? Float ?: 1.0f
//            bar.scaleY = storedHeight
//        }
//
//        // Show current playback position
//        val currentMinutes = (progress / 1000) / 60
//        val currentSeconds = (progress / 1000) % 60
//        binding.pausedTimerTv.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
//
//        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
//    }
//
//    private fun mixVoiceNote() {
//        val TAG = "mixVN"
//        try {
//            wasPaused = true
//            Log.d(TAG, "pauseRecording: outputFile: $outputVnFile")
//
//            val audioMixer = AudioMixer(outputVnFile)
//            for (input in recordedAudioFiles) {
//                val ai = GeneralAudioInput(input)
//                audioMixer.addDataSource(ai)
//            }
//            audioMixer.mixingType = AudioMixer.MixingType.SEQUENTIAL
//
//            audioMixer.setProcessingListener(object : AudioMixer.ProcessingListener {
//                override fun onProgress(progress: Double) {}
//
//                override fun onEnd() {
//                    runOnUiThread {
//                        audioMixer.release()
//                        mixingCompleted = true
//                        val file = File(outputVnFile)
//                        Log.d(TAG, "onEnd: output vn file exists ${file.exists()}")
//                        Log.d(TAG, "onEnd: media muxed success")
//
//                        binding.waveformScrollView.visibility = View.VISIBLE
//                        binding.waveDotsContainer.visibility = View.VISIBLE
//                        binding.wave.visibility = View.GONE
//
//                        binding.playVnAudioBtn.setOnClickListener {
//                            Log.d("playVnAudioBtn", "onEnd: play vn button clicked")
//                            when {
//                                !isAudioVNPlaying -> {
//                                    Log.d("playVnAudioBtn", "play vn")
//                                    startPlaying(outputVnFile)
//                                }
//                                else -> {
//                                    Log.d("playVnAudioBtn", "pause VN")
//                                    vnRecordAudioPlaying = true
//                                    val currentProgress = player?.currentPosition ?: vnRecordProgress
//                                    vnRecordProgress = currentProgress
//                                    pauseVn(currentProgress)
//                                }
//                            }
//                        }
//                    }
//                }
//            })
//
//            try {
//                audioMixer.start()
//                audioMixer.processAsync()
//            } catch (e: IOException) {
//                audioMixer.release()
//                e.printStackTrace()
//                Log.d(TAG, "pauseRecording: exception 1 $e")
//                Log.d(TAG, "pauseRecording: exception 1 ${e.message}")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.d(TAG, "pauseRecording: exception 2 $e")
//            Log.d(TAG, "pauseRecording: exception 2 ${e.message}")
//        }
//    }
//
//    private fun listenToAudio() {
//        try {
//            val minBufferSize = AudioRecord.getMinBufferSize(
//                44100,
//                android.media.AudioFormat.CHANNEL_IN_MONO,
//                android.media.AudioFormat.ENCODING_PCM_16BIT
//            )
//
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.RECORD_AUDIO),
//                    REQUEST_RECORD_AUDIO_PERMISSION
//                )
//                return
//            }
//
//            audioRecord = AudioRecord(
//                MediaRecorder.AudioSource.MIC,
//                44100,
//                android.media.AudioFormat.CHANNEL_IN_MONO,
//                android.media.AudioFormat.ENCODING_PCM_16BIT,
//                minBufferSize * 2
//            )
//
//            audioRecord?.startRecording()
//            val buffer = ShortArray(minBufferSize)
//
//            while (isListeningToAudio && isRecording) {
//                val readSize = audioRecord?.read(buffer, 0, minBufferSize) ?: 0
//
//                if (readSize > 0) {
//                    // Calculate RMS (Root Mean Square) for better amplitude detection
//                    var sum = 0.0
//                    for (i in 0 until readSize) {
//                        sum += (buffer[i].toDouble() * buffer[i].toDouble())
//                    }
//                    val rms = sqrt(sum / readSize)
//
//                    // Normalize amplitude to 0-1 range (adjust 5000.0 for sensitivity)
//                    val normalizedAmplitude = (rms / 5000.0).coerceIn(0.0, 1.0).toFloat()
//
//                    runOnUiThread {
//                        if (normalizedAmplitude > 0.05f) { // Sound detected threshold
//                            // Map amplitude to height multiplier (0.3 to 2.5)
//                            val heightMultiplier = 0.3f + (normalizedAmplitude * 2.2f)
//                            addWaveBarForSound(heightMultiplier)
//                        } else { // No sound or very quiet
//                            addIdleDottedBarAtEnd()
//                        }
//                        scrollToRight()
//                    }
//                }
//
//                Thread.sleep(50) // Update every 50ms for smooth animation
//            }
//
//            audioRecord?.release()
//            audioRecord = null
//        } catch (e: Exception) {
//            Log.e("ListenToAudio", "Error: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//    private fun addWaveBarForSound(heightMultiplier: Float) {
//        val bar = View(this).apply {
//            layoutParams = LinearLayout.LayoutParams(
//                dpToPx(4), // 4dp width
//                dpToPx(48) // 48dp max height
//            ).apply {
//                marginEnd = dpToPx(6) // 6dp spacing between bars
//                gravity = android.view.Gravity.CENTER_VERTICAL
//            }
//            background = GradientDrawable().apply {
//                shape = GradientDrawable.RECTANGLE
//                setColor(Color.parseColor("#2563EB")) // Blue color
//                cornerRadius = dpToPx(2).toFloat() // Rounded corners
//            }
//            // Apply height multiplier with clamping
//            scaleY = heightMultiplier.coerceIn(0.2f, 2.5f)
//            alpha = 1.0f
//            tag = heightMultiplier // Store original height
//        }
//
//        binding.waveDotsContainer.addView(bar)
//        waveBars.add(bar)
//
//        // Remove old bars from START (left side) if exceeding limit
//        if (waveBars.size > maxWaveBars) {
//            binding.waveDotsContainer.removeViewAt(0)
//            waveBars.removeAt(0)
//        }
//
//        scrollToRight()
//    }
//
//    private fun addIdleDottedBarAtEnd() {
//        val bar = View(this).apply {
//            val dotSize = dpToPx(5) // 5dp circular dot
//            layoutParams = LinearLayout.LayoutParams(
//                dotSize,
//                dotSize
//            ).apply {
//                marginEnd = dpToPx(3) // 3dp spacing between dots
//                gravity = android.view.Gravity.CENTER_VERTICAL
//            }
//
//            // Create circular dot with blue color
//            background = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                setColor(Color.parseColor("#2563EB")) // Same blue as bars
//            }
//
//            scaleY = 1.0f
//            alpha = 1.0f
//            tag = "idle_dot" // Mark as idle dot
//        }
//
//        binding.waveDotsContainer.addView(bar)
//        waveBars.add(bar)
//
//        // Remove old bars from START (left side) if exceeding limit
//        if (waveBars.size > maxWaveBars) {
//            binding.waveDotsContainer.removeViewAt(0)
//            waveBars.removeAt(0)
//        }
//    }
//
//    private fun initializeDottedWaveform() {
//        binding.waveDotsContainer.removeAllViews()
//        waveBars.clear()
//
//        val barsToFill = calculateBarsNeededForFullWidth()
//        repeat(barsToFill) {
//            addIdleDottedBarAtEnd()
//        }
//
//        // Scroll to right after initialization
//        binding.waveformScrollView.post {
//            val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
//            if (maxScroll > 0) {
//                binding.waveformScrollView.scrollTo(maxScroll, 0)
//            }
//        }
//    }
//
//    private fun calculateBarsNeededForFullWidth(): Int {
//        val screenWidth = resources.displayMetrics.widthPixels
//        val barWidth = dpToPx(4)
//        val barMargin = dpToPx(6)
//        val totalBarWidth = barWidth + barMargin
//        return (screenWidth / totalBarWidth) + 5 // Add extra for smooth scrolling
//    }
//
//    private fun scrollToRight() {
//        binding.waveformScrollView.post {
//            val maxScroll = (binding.waveDotsContainer.width - binding.waveformScrollView.width).coerceAtLeast(0)
//            if (maxScroll > 0) {
//                binding.waveformScrollView.smoothScrollTo(maxScroll, 0)
//            }
//        }
//    }
//
//    private fun startPlaying(vnAudio: String) {
//        EventBus.getDefault().post(PauseShort(true))
//        isAudioVNPlaying = true
//        vnRecordAudioPlaying = true
//
//        updateVoiceNoteUserInterfaceState(VoiceNoteState.PLAYING)
//
//        isOnRecordDurationOnPause = false
//
//        if (isAudioVNPaused) {
//            if (vnRecordProgress != 0) {
//                player?.seekTo(vnRecordProgress)
//            }
//            player?.start()
//        } else {
//            player = MediaPlayer().apply {
//                try {
//                    setDataSource(vnAudio)
//                    prepare()
//                    totalRecordedDuration = duration.toLong()
//                    if (vnRecordProgress != 0) {
//                        seekTo(vnRecordProgress)
//                    }
//                    start()
//                    setOnCompletionListener {
//                        isAudioVNPaused = false
//                        isAudioVNPlaying = false
//                        stopPlayingOnCompletion()
//                    }
//                } catch (e: IOException) {
//                    Log.e("MediaRecorder", "prepare() failed")
//                }
//            }
//        }
//
//        animatePlaybackWaves()
//        updatePlaybackTimer() // This will now work correctly
//    }
//
//    @SuppressLint("DefaultLocale")
//    private fun stopPlayingOnCompletion() {
//        val scrollAnimator = binding.waveformScrollView.tag as? ValueAnimator
//        scrollAnimator?.cancel()
//
//        val totalDuration = player?.duration ?: 0
//
//        player?.release()
//        player = null
//
//        isAudioVNPlaying = false
//        isAudioVNPaused = false
//        vnRecordAudioPlaying = false
//
//        stopPlaybackTimerRunnable()
//        stopWaveDotsAnimation()
//
//        // Return to paused state showing total duration and PLAY icon
//        updateVoiceNoteUserInterfaceState(VoiceNoteState.PAUSED)
//
//        binding.playVnAudioBtn.setImageResource(com.uyscuti.social.circuit.R.drawable.play_svgrepo_com)
//
//        val totalMinutes = (totalDuration / 1000) / 60
//        val totalSeconds = (totalDuration / 1000) % 60
//        binding.pausedTimerTv.text = String.format("%02d:%02d", totalMinutes, totalSeconds)
//
//        vnRecordProgress = 0
//
//        // Scroll back to start
//        binding.waveformScrollView.post {
//            binding.waveformScrollView.scrollTo(0, 0)
//        }
//    }
//
//    private fun deleteVn() {
//        recordedAudioFiles.clear()
//        val isDeleted = deleteFiles(recordedAudioFiles)
//        val outputVnFileList = mutableListOf<String>().apply { add(outputVnFile) }
//        val deleteMixVn = deleteFiles(outputVnFileList)
//        if (isDeleted) {
//            Log.d(TAG, "File record deleted successfully")
//        } else {
//            println("Failed to delete file.")
//        }
//
//        if (deleteMixVn) {
//            Log.d(TAG, "File mix vn deleted successfully")
//        } else {
//            println("Failed to delete file.")
//        }
//    }
//
//    private fun observeThisDialog(name: String) {
//        CoroutineScope(Dispatchers.Main).launch {
//            dialogViewModel.observeThisDialog(name)
//                .observe(this@MessagesActivity, Observer { dialog ->
//                    if (dialog != null) {
//                        Log.d("DialogObservation", "Changed : $dialog")
//                        Log.d("DialogObservation", "Changed Id : ${dialog.id}")
//                        chatId = dialog.id
//                        trigger()
//                    }
//                })
//        }
//    }
//
//    private fun trigger() {
//        userStatusManager?.start()
//        observeTemporallyMessages(chatName)
//        observeSendingMessagesI()
//    }
//
//    private fun updateStatus(userStatus: UserStatusResponse) {
//        if (userStatus.isOnline) {
//            // Update the subtitle of the toolbar
//            supportActionBar?.subtitle = "online"
//            binding.toolbar.setSubtitleTextColor(resources.getColor(R.color.white_two))
//        } else {
//            // Format the last seen date
//            val formattedLastSeen = formatLastSeenDate(userStatus.lastSeen)
//            // Update the subtitle of the toolbar
//            supportActionBar?.subtitle = formattedLastSeen
//            binding.toolbar.setSubtitleTextColor(resources.getColor(R.color.white_two))
//        }
//    }
//
//    private fun formatDate(lastSeen: Date?): String {
//        if (lastSeen == null) {
//            return "Unknown"
//        }
//
//        val now = Date()
//
//        // Check if the date is today
//        return if (DateUtils.isToday(lastSeen.time)) {
//
//            // Format the time for today
//            val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
//            " ${dateFormat.format(lastSeen)}"
//
//        } else {
//            // Format the date for other days
//            val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
//            " ${dateFormat.format(lastSeen)}"
//        }
//    }
//
//
//    fun formatLastSeenDa(lastSeen: Date?): String {
//        if (lastSeen == null) {
//            return "Unknown"
//        }
//
//        val now = Date()
//
//        // Check if the date is today
//        return if (DateUtils.isToday(lastSeen.time)) {
//            // Check if the time is within the last minute (considered "online")
//            if (now.time - lastSeen.time < DateUtils.MINUTE_IN_MILLIS) {
//                "Online"
//            } else {
//                // Format the time for today
//                val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
//                "Last Seen Today at ${dateFormat.format(lastSeen)}"
//            }
//        } else {
//            // Format the date for other days
//            val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
//            "Last Seen on ${dateFormat.format(lastSeen)}"
//        }
//    }
//
//
//    private fun formatLastSeenDate(lastSeen: Date?): String {
//        if (lastSeen == null) {
//            return ""
//        }
//
//        val now = Date()
//
//        // Check if the date is today
//        if (DateUtils.isToday(lastSeen.time)) {
//            // Check if the time is within the last minute (considered "online")
////            if (now.time - lastSeen.time < DateUtils.MINUTE_IN_MILLIS) {
////                return "Online"
////            } else {
//            // Format the time for today
//            val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
//            return "Last Seen Today at ${dateFormat.format(lastSeen)}"
////            return ""
////            }
//        } else if (DateUtils.isToday(lastSeen.time + DateUtils.DAY_IN_MILLIS)) {
//            // Format the time for yesterday
//            val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
//            return "Last Seen Yesterday at ${dateFormat.format(lastSeen)}"
//        } else {
//            // Format the date for other days
//            val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
//            return "Last Seen on ${dateFormat.format(lastSeen)}"
//        }
//    }
//
//    @OptIn(UnstableApi::class)
//    private fun viewUser() {
//        if (dialog?.users?.size == 1) {
//            OtherUserProfileAccount.Companion.open(
//                this@MessagesActivity,
//                dialog!!.users[0],
//                dialog!!.dialogPhoto,
//                dialog!!.id
//            )
//        } else {
//            dialog?.let {
//                GroupProfileActivity.open(
//                    this@MessagesActivity, it, groupAdminId,
//                    groupCreatedAt
//                )
//            }
//        }
//    }
//
//    private fun installFonts() {
//        EmojiManager.install(
//            GoogleCompatEmojiProvider(
//                EmojiCompat.init(
//                    FontRequestEmojiCompatConfig(
//                        this@MessagesActivity,
//                        FontRequest(
//                            "com.google.android.gms.fonts",
//                            "com.google.android.gms",
//                            "Noto Color Emoji Compat",
//                            R.array.com_google_android_gms_fonts_certs,
//                        )
//                    ).setReplaceAll(true)
//                )
//            )
//        )
//    }
//
//    private fun getLastSeen(userId: String, callback: (Date?, String?) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = retrofitIns.apiService.getUserLastSeen(userId)
//                if (response.isSuccessful) {
//                    withContext(Dispatchers.Main) {
//                        if (response.body() != null) {
//                            val lastSeen = response.body()!!.data?.lastSeen
//
//                            Log.d("LastSeen", "Response : ${response.body()}")
//                            callback(lastSeen, null)
//                        } else {
//                            callback(null, "Response body is null")
//                        }
//                    }
//                } else {
//                    callback(null, "Unsuccessful response: ${response.code()}")
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                callback(null, "Error: ${e.message}")
//            }
//        }
//    }
//
//
//    private fun installTwitter() {
//        EmojiManager.install(TwitterEmojiProvider())
//    }
//
//    private fun installIos() {
//        EmojiManager.install(IosEmojiProvider())
//    }
//
//    private fun installGoogle() {
//        EmojiManager.install(GoogleEmojiProvider())
//    }
//
//    private fun installFacebook() {
//        EmojiManager.install(FacebookEmojiProvider())
//    }
//
//    private fun initViews() {
//        emojiShowing = if (emojiPopup.isShowing && emojiShowing) {
//            // Close the emoji keyboard
//            emojiPopup.dismiss() // Dismisses the Popup.
//            inputMethodManager.showSoftInput(
//                binding.inputEditText,
//                InputMethodManager.SHOW_IMPLICIT
//            )
//            false
//        } else {
//            // Open the emoji keyboard
//            inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
//            emojiPopup.toggle() // Toggles visibility of the Popup.
//            true
//        }
//    }
//
//    private fun initView() {
//        Thread {
//            runOnUiThread {
//                emojiShowing = if (emojiPopup.isShowing && emojiShowing) {
//                    // Close the emoji keyboard
//                    emojiPopup.dismiss() // Dismisses the Popup.
//                    inputMethodManager.showSoftInput(
//                        binding.inputEditText,
//                        InputMethodManager.SHOW_IMPLICIT
//                    )
//                    false
//                } else {
//                    // Open the emoji keyboard
//                    inputMethodManager.hideSoftInputFromWindow(
//                        binding.inputEditText.windowToken,
//                        0
//                    )
//                    emojiPopup.toggle() // Toggles visibility of the Popup.
//                    true
//                }
//            }
//        }.start()
//    }
//
//
//    private fun getFileNameFromUrl(videoUrl: String): String {
//        // Split the URL using '/' as a delimiter and get the last part, which is the video filename
//        val parts = videoUrl.split("/")
//
//        // You can further process the filename if needed, such as removing the file extension
//        return parts.last()
//    }
//
//    private fun initV() {
//        Thread {
//            Handler(mainLooper).postDelayed({
//                emojiShowing = if (emojiPopup.isShowing && emojiShowing) {
//                    // Close the emoji keyboard
//                    emojiPopup.dismiss() // Dismisses the Popup.
//                    inputMethodManager.showSoftInput(
//                        binding.inputEditText,
//                        InputMethodManager.SHOW_IMPLICIT
//                    )
//                    false
//                } else {
//                    // Open the emoji keyboard
//                    inputMethodManager.hideSoftInputFromWindow(
//                        binding.inputEditText.windowToken,
//                        0,
//                    )
//                    inputMethodManager.hideSoftInputFromWindow(
//                        binding.inputEditText.windowToken,
//                        0
//                    )
//                    Handler().postDelayed({
//                        emojiPopup.toggle() // Toggles visibility of the Popup.
//                    }, 500)
//                    true
//                }
//            }, 300)
//        }.start()
//    }
//
//
//    private fun initV3() {
//        val resultReceiver = object : ResultReceiver(Handler(mainLooper)) {
//            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
//                // Handle result if needed
//                Log.d("KeyBoardResults", "Result received: $resultCode : $resultData")
//            }
//        }
//
//        Handler(mainLooper).postDelayed({
//            emojiShowing = if (emojiPopup.isShowing && emojiShowing) {
//                // Close the emoji keyboard
//                emojiPopup.dismiss()
//
//                // Hiding the keyboard and sending a result
//                showKeyboard(binding.inputEditText)
//                false
//            } else {
//                // Open the emoji keyboard
//                hideKeyboard(binding.inputEditText, resultReceiver)
//
//                Handler().postDelayed({
//                    emojiPopup.toggle() // Toggles visibility of the Popup.
//                }, 100)
//                true
//            }
//        }, 100)
//    }
//
//    private fun hideKeyboard(view: View, resultReceiver: ResultReceiver?) {
//        val inputMethodManager =
//            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0, resultReceiver)
//    }
//
//    private fun showKeyboard(view: View) {
//        val inputMethodManager =
//            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
//    }
//
//    private fun initEmoji() {
////        messageEdit = findViewById(R.id.messageEdit)
////        sendBtn = findViewById(R.id.sendBtn)
////        pickImgBtn = findViewById(R.id.voiceNote)
////        recyclerView = findViewById(R.id.recyclerView)
////        emojiButton = findViewById(R.id.emoji)
////        rootView = findViewById(R.id.rootView)
////        chatAvatar = findViewById(R.id.chatAvatar)
////        videoCall = findViewById(R.id.videoCall)
////        voiceCall = findViewById(R.id.voiceCall)
//
//        val emojiPopup = EmojiPopup(binding.container, binding.inputEditText)
//
//        binding.emoji.setOnClickListener {
//            val inputMethodManager =
//                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            if (emojiPopup.isShowing) {
//                // Close the emoji keyboard
//                emojiPopup.dismiss() // Dismisses the Popup.
//                inputMethodManager.showSoftInput(
//                    binding.inputEditText,
//                    InputMethodManager.SHOW_IMPLICIT
//                )
////                emojiButton.background = ContextCompat.getDrawable(this, R.drawable.baseline_insert_emoticon_24)
//                binding.emoji.background =
//                    resources.getDrawable(R.drawable.baseline_insert_emoticon_24)
//            } else {
//                // Open the emoji keyboard
//                inputMethodManager.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
//                emojiPopup.toggle() // Toggles visibility of the Popup.
////                binding.emoji.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_keyboard_24))
//                binding.emoji.background =
//                    resources.getDrawable(R.drawable.baseline_keyboard_24)
////                binding.emoji.background =  ResourcesCompat.getDrawable(, R.drawable.baseline_insert_emoticon_24, null)
//            }
//        }
//
//    }
//
//    override fun onSubmit(input: CharSequence): Boolean {
//
//        val user = User("0", "You", "avatar", true, Date())
//
//        val date = Date(System.currentTimeMillis())
//
//        val messageId = "Text_${Random.Default.nextInt()}"
//
//        val avatar = settings.getString("avatar", "avatar").toString()
//
//
//        Log.d("MessageSent", "Message Id : $messageId")
//
//        val message = Message(
//            messageId,
//            user,
//            input.toString(),
//            date
//        )
//
//        message.status = "Sending"
//
//
//
//        val userEntity = UserEntity(
//            "0",
//            "You",
//            avatar,
//            Date(),
//            true
//        )
//
//        val textMessage = MessageEntity(
//            id = messageId,
//            chatId = chatId,
//            userName = "You",
//            user = userEntity,
//            userId = myId,
//            text = input.toString(),
//            createdAt = System.currentTimeMillis(),
//            imageUrl = null,
//            voiceUrl = null,
//            voiceDuration = 0,
//            status = "Sending",
//            videoUrl = null,
//            audioUrl = null,
//            docUrl = null,
//            fileSize = 0
//        )
//
//        CoroutineScope(Dispatchers.IO).launch {
//            insertMessage(textMessage)
////            dialogViewModel.updateLastMessageForThisChat(chatId, textMessage)
//            updateLastMessage(isGroup, chatId, textMessage)
//
//
//
//        }
//
//        super.messagesAdapter?.addToStart(message, true)
//        return true
//    }
//
//
//    private fun sendTextMessage(text: String, message: Message, dBMessage: MessageEntity, callback: (Boolean) -> Unit) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(NonCancellable) {
//                when (val result = remoteMessageRepository.sendMessage(chatId, text)) {
//                    is Result.Success -> {
//                        // Message sent successfully, update the UI as needed
//                        withContext(Dispatchers.Main) {
//
//                            super.messagesAdapter?.notifyMessageSent(message)
//                        }
//                        messageViewModel.updateMessageStatus(dBMessage)
//                        callback(true)
//                    }
//
//                    is Result.Error -> {
//
//                        callback(false)
//                    }
//
//                    is Result.Error -> TODO()
//                    is Result.Success<*> -> TODO()
//                }
//            }
//        }
//    }
//
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private suspend fun insertMessage(message: MessageEntity) {
//        CoroutineScope(Dispatchers.IO).launch {
//            messageViewModel.insertMessage(message)
//        }
//    }
//
//    private fun resolveContentUriToFilePath(contentUri: Uri): String? {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursorLoader = CursorLoader(this, contentUri, projection, null, null, null)
//        val cursor = cursorLoader.loadInBackground()
//
//        return cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
//                it.getString(columnIndex)
//            } else {
//                null // Handle the error, unable to retrieve the actual path
//            }
//        }
//    }
//
//    private fun getFilePath() {
//        val avatar = settings.getString("avatar", "avatar").toString()
//        val userEntity = UserEntity(
//            "0",
//            "You",
//            avatar,
//            Date(),
//            true
//        )
//
//        cameraLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    // Handle image selection result here
//                    val data = result.data
//                    // Process the selected image data
//                    val imagePath = data?.getStringExtra("image_url")
//
//                    if (imagePath != null) {
//
//
//                        // You now have the imagePath from the DisplayImages activity.
//                        // You can use it as needed, for example, to send the image in a message.
//                        Log.d(
//                            "ChatActivityImagePath",
//                            "Selected image path from camera: $imagePath"
//                        )
//
//                        //val imagePathRef =
//                        //val fileRef = storageRef.child("files/$fileName")
//                        // You can proceed to send the image or display it in your chat.
//
//                        val url = resolveContentUriToFilePath(Uri.parse(imagePath))
//                        val imageFileName =
//                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                        Log.d(TAG, "file name $imageFileName")
////                        Log.d(TAG, "image path $imagePath")
//                        Log.d(TAG, "camera image path $url")
//
//                        val user = User("0", "You", "test", true, Date())
//
//                        val date = Date(System.currentTimeMillis())
//                        val messageId = "Image_${Random.Default.nextInt()}"
//
//                        val message = Message(
//                            messageId,
//                            user, // Set user ID as needed
//                            null,
//                            date
//                        )
//
//
//                        val imageUrl = Uri.parse(imagePath)
//                        Log.d(TAG, "camera image url $imageUrl")
//
//                        val file = File(imagePath)
//                        if (file.exists()) {
//                            val absolutePath = file.absolutePath
//                            Log.d(TAG, "camera image absolute path $absolutePath")
//                            val fileUri = Uri.fromFile(file)
//                            val fileUrl = fileUri.toString()
//                            //                            Log.d(TAG, "image file url path $fileUrl")
//
//                            message.setImage(Message.Image(fileUrl))
//
//                            val imageMessage = MessageEntity(
//                                id = messageId,
//                                chatId = chatId,
//                                userName = "You",
//                                user = userEntity,
//                                userId = myId,
//                                text = "📷 Image",
//                                createdAt = System.currentTimeMillis(),
//                                imageUrl = fileUrl,
//                                voiceUrl = null,
//                                voiceDuration = 0,
//                                status = "Sending",
//                                videoUrl = null,
//                                audioUrl = null,
//                                docUrl = null,
//                                fileSize = getFileSize(fileUrl)
//
//                            )
//
//                            CoroutineScope(Dispatchers.IO).launch {
//                                insertMessage(imageMessage)
//
//                                updateLastMessage(isGroup, chatId, imageMessage)
////                                dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                            }
//                        }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
//                        message.setImage(Message.Image(imageUrl.toString()))
//                        message.setUser(user)
//                        message.status = "Sending"
//
//                        CoroutineScope(Dispatchers.Main).launch {
//                            super.messagesAdapter?.addToStart(message, true)
//                        }
//
////                        sendFile(imagePath, message)
//
//// Convert the image path to a File or use the path directly if it's a valid file path
//                        //val imageFile = File(imagePath)
//
//
////
//                        // sendFile(chatId, imagePath)
//                    }
//                }
//
//            }
//
//        imagePickerLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    // Handle image selection result here
//                    val data = result.data
//                    // Process the selected image data
//                    val imagePath = data?.getStringExtra("image_url")
//
//                    if (imagePath != null) {
//
//                        val messageId = "Image_${Random.Default.nextInt()}"
//                        val imageUrl = Uri.parse(imagePath)
//                        val url = resolveContentUriToFilePath(Uri.parse(imagePath))
//
//                        val file = url?.let { File(it) }
//
//
//                        // You now have the imagePath from the DisplayImages activity.
//                        // You can use it as needed, for example, to send the image in a message.
//                        Log.d("ChatActivityImagePath", "Selected image path: $imagePath")
//
//                        val saved = copyFileToInternalStorage(this, url.toString(), messageId)
//
//                        Log.d("FileOperation", "Completed : $saved")
//
//                        //val imagePathRef =
//                        //val fileRef = storageRef.child("files/$fileName")
//                        // You can proceed to send the image or display it in your chat.
//
//                        val imageFileName =
//                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                        Log.d(TAG, "file name $imageFileName")
////                        Log.d(TAG, "image path $imagePath")
//                        Log.d(TAG, "image path $url")
//
//                        val user = User("0", "You", "test", true, Date())
//
//                        val date = Date(System.currentTimeMillis())
////                        val messageId = "Image_${Random.nextInt()}"
//
//                        val message = Message(
//                            messageId,
//                            user, // Set user ID as needed
//                            null,
//                            date
//                        )
//
//
//                        Log.d(TAG, "image url $imageUrl")
//
//                        if (file != null) {
//                            if (file.exists()) {
//                                val absolutePath = file.absolutePath
//                                Log.d(TAG, "image absolute path $absolutePath")
//                                val fileUri = Uri.fromFile(file)
//                                val fileUrl = fileUri.toString()
//                                //                            Log.d(TAG, "image file url path $fileUrl")
//
//                                message.setImage(Message.Image(fileUrl))
//
//                                val imageMessage = MessageEntity(
//                                    id = messageId,
//                                    chatId = chatId,
//                                    userName = "You",
//                                    user = userEntity,
//                                    userId = myId,
//                                    text = "📷 Image",
//                                    createdAt = System.currentTimeMillis(),
//                                    imageUrl = fileUrl,
//                                    voiceUrl = null,
//                                    voiceDuration = 0,
//                                    status = "Sending",
//                                    videoUrl = null,
//                                    audioUrl = null,
//                                    docUrl = null,
//                                    fileSize = getFileSize(fileUrl)
//
//                                )
//
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    insertMessage(imageMessage)
////                                    dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                                    updateLastMessage(isGroup, chatId, imageMessage)
//                                }
//                            }
//                        }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
//                        message.setImage(Message.Image(imageUrl.toString()))
//                        message.setUser(user)
//                        message.status = "Sending"
//
//                        CoroutineScope(Dispatchers.Main).launch {
//                            super.messagesAdapter?.addToStart(message, true)
//                        }
//
////                        sendFile(imagePath, message)
//
//// Convert the image path to a File or use the path directly if it's a valid file path
//                        //val imageFile = File(imagePath)
//
//
////
//                        // sendFile(chatId, imagePath)
//                    }
//                }
//
//            }
//        audioPickerLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    // Handle image selection result here
//                    val data = result.data
//                    // Process the selected image data
//                    val audioPath = data?.getStringExtra("audio_url")
//
//                    if (audioPath != null) {
//                        // You now have the imagePath from the DisplayImages activity.
//                        // You can use it as needed, for example, to send the image in a message.
//                        Log.d("ChatActivityAudioPath", "Selected audio path: $audioPath")
//
//                        //val imagePathRef =
//                        //val fileRef = storageRef.child("files/$fileName")
//                        // You can proceed to send the image or display it in your chat.
//                        val audioFileName =
//                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                        Log.d(TAG, "file name $audioFileName")
////                        Log.d(TAG, "audio path $audioPath")
//
//
//                        val user = User("0", "You", "test", true, Date())
//
//                        val date = Date(System.currentTimeMillis())
//                        val messageId = "Audio_${Random.Default.nextInt()}"
//
//                        val message = Message(
//                            messageId,
//                            user, // Set user ID as needed
//                            null,
//                            date
//                        )
//
//
//                        val audioUrl = Uri.parse(audioPath)
//                        val file = File(audioPath)
//                        if (file.exists()) {
//
//                            Log.d("Audio File", "Audio File Exists : $file")
//                            val absolutePath = file.absolutePath
////                            Log.d(TAG, "image absolute path $absolutePath")
//                            val fileUri = Uri.fromFile(file)
//                            val fileUrl = fileUri.toString()
////                            Log.d(TAG, "image file url path $fileUrl")
//
//                            message.setAudio(
//                                Message.Audio(
//                                    fileUrl,
//                                    10000,
//                                    getNameFromUrl(fileUrl)
//                                )
//                            )
//
//                            val imageMessage = MessageEntity(
//                                id = messageId,
//                                chatId = chatId,
//                                userName = "You",
//                                user = userEntity,
//                                userId = myId,
//                                text = "🎵 Audio",
//                                createdAt = System.currentTimeMillis(),
//                                imageUrl = null,
//                                voiceUrl = null,
//                                voiceDuration = 0,
//                                status = "Sending",
//                                videoUrl = null,
//                                audioUrl = fileUrl,
//                                docUrl = null,
//                                fileSize = getFileSize(fileUrl)
//
//                            )
//
//                            CoroutineScope(Dispatchers.IO).launch {
//                                insertMessage(imageMessage)
////                                dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                                updateLastMessage(isGroup, chatId, imageMessage)
//                            }
//                        }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
////                        message.setImage(Message.Image(imageUrl.toString()))
//                        message.setUser(user)
//                        message.status = "Sending"
//
//                        CoroutineScope(Dispatchers.Main).launch {
////                            delay(500)
//                            super.messagesAdapter?.addToStart(message, true)
//                        }
//
//                        // sendFile(chatId, imagePath)
//                    }
//                }
//
//            }
//
//        videoPickerLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                Log.d("VideoDebug", "onActivityResult callback triggered")
//                if (result.resultCode == RESULT_OK) {
//                    // Handle image selection result here
//                    val data = result.data
//                    // Process the selected image data
//                    val videoPath = data?.getStringExtra("video_url")
//
//                    if (videoPath != null) {
//                        // You now have the imagePath from the DisplayImages activity.
//                        // You can use it as needed, for example, to send the image in a message.
////                        Log.d("ChatActivityVideoPath", "Selected video path: $videoPath")
//
//                        //val imagePathRef =
//                        //val fileRef = storageRef.child("files/$fileName")
//                        // You can proceed to send the image or display it in your chat.
//                        val videoFileName =
//                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                        Log.d(TAG, "file name $videoFileName")
////                        Log.d(TAG, "video path $videoPath")
//
//
//                        val user = User("0", "You", "test", true, Date())
//
//                        val date = Date(System.currentTimeMillis())
//                        val messageId = "Video_${Random.Default.nextInt()}"
//
//                        val message = Message(
//                            messageId,
//                            user, // Set user ID as needed
//                            null,
//                            date
//                        )
//
//                        val audioUrl = Uri.parse(videoPath)
//                        val file = File(videoPath)
//                        if (file.exists()) {
//
////                            Log.d("Video File", "Video File Exists : $file")
//                            val absolutePath = file.absolutePath
////                            Log.d(TAG, "image absolute path $absolutePath")
//                            val fileUri = Uri.fromFile(file)
//                            val fileUrl = fileUri.toString()
////                            Log.d(TAG, "image file url path $fileUrl")
//
//                            message.setVideo(Message.Video(fileUrl))
//
//                            val imageMessage = MessageEntity(
//                                id = messageId,
//                                chatId = chatId,
//                                userName = "You",
//                                user = userEntity,
//                                userId = myId,
//                                text = "🎬 Video",
//                                createdAt = System.currentTimeMillis(),
//                                imageUrl = null,
//                                voiceUrl = null,
//                                voiceDuration = 0,
//                                status = "Sending",
//                                videoUrl = fileUrl,
//                                audioUrl = null,
//                                docUrl = null,
//                                fileSize = getFileSize(fileUrl)
//
//                            )
//
//                            CoroutineScope(Dispatchers.IO).launch {
//                                insertMessage(imageMessage)
////                                dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                                updateLastMessage(isGroup, chatId, imageMessage)
//                            }
//                        }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
////                        message.setImage(Message.Image(imageUrl.toString()))
//                        message.setUser(user)
//                        message.status = "Sending"
//
//                        CoroutineScope(Dispatchers.Main).launch {
////                            delay(500)
//                            super.messagesAdapter?.addToStart(message, true)
//                        }
//
//                    } else {
//                        Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        docsPickerLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    // Handle image selection result here
//                    val data = result.data
//                    // Process the selected image data
//                    val docPath = data?.getStringExtra("doc_url")
//
//                    Log.d("Document Results", "Picked Document : $docPath")
//
//                    if (docPath != null) {
//                        // You now have the imagePath from the DisplayImages activity.
//                        // You can use it as needed, for example, to send the image in a message.
//                        Log.d("ChatActivityDocPath", "Selected Document path: $docPath")
//
//                        //val imagePathRef =
//                        //val fileRef = storageRef.child("files/$fileName")
//                        // You can proceed to send the image or display it in your chat.
//                        val docFileName =
//                            "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                        Log.d(TAG, "file name $docFileName")
////                        Log.d(TAG, "image path $docPath")
//
//                        val user = User("0", "You", "test", true, Date())
//                        val messageId = "Doc_${Random.Default.nextInt()}"
//
//                        val date = Date(System.currentTimeMillis())
//
//                        val message = Message(
//                            messageId,
//                            user, // Set user ID as needed
//                            null,
//                            date
//                        )
//
//                        val audioUrl = Uri.parse(docPath)
//                        val file = File(docPath)
//                        if (file.exists()) {
//
//                            Log.d("Document File", "Document File Exists : $file")
//                            val absolutePath = file.absolutePath
////                            Log.d(TAG, "image absolute path $absolutePath")
//                            val fileUri = Uri.fromFile(file)
//                            val fileUrl = fileUri.toString()
////                            Log.d(TAG, "image file url path $fileUrl")
//
//                            message.setDocument(
//                                Message.Document(
//                                    fileUrl,
//                                    formatFileSize(getFileSize(fileUrl)),
//                                    getNameFromUrl(fileUrl)
//                                )
//                            )
//
//                            val imageMessage = MessageEntity(
//                                id = messageId,
//                                chatId = chatId,
//                                userName = "You",
//                                user = userEntity,
//                                userId = myId,
//                                text = "📄 Document",
//                                createdAt = System.currentTimeMillis(),
//                                imageUrl = null,
//                                voiceUrl = null,
//                                voiceDuration = 0,
//                                status = "Sending",
//                                videoUrl = null,
//                                audioUrl = null,
//                                docUrl = fileUrl,
//                                fileSize = getFileSize(fileUrl)
//                            )
//
//                            CoroutineScope(Dispatchers.IO).launch {
//                                insertMessage(imageMessage)
////                                dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                                updateLastMessage(isGroup, chatId, imageMessage)
//
//                            }
//                        }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
////                        message.setImage(Message.Image(imageUrl.toString()))
//                        message.setUser(user)
//                        message.status = "Sending"
//
//                        CoroutineScope(Dispatchers.Main).launch {
////                            delay(500)
//                            super.messagesAdapter?.addToStart(message, true)
//                        }
//
//                        // sendFile(chatId, imagePath)
//                    }
//                }
//
//            }
//
//
//        val audioPermission = Manifest.permission.RECORD_AUDIO
//        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
//        val readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
//
//        if (ContextCompat.checkSelfPermission(
//                this,
//                audioPermission
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(audioPermission, writePermission, readPermission),
//                1
//            )
//        }
//
//        openFilePicker =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == RESULT_OK) {
//                    val data = result.data?.data
//
//                    data?.let { uri ->
//                        // Handle the selected file here
//                        val contentResolver = contentResolver
//                        val docName = getPathFromUri(uri)
//                        val selectedPath = copyFileToInternalStorage(uri, docName!!)
//                        val filePath = getFilePathFromUri(uri)
//                        val localPath = uri.toString() // You can store this URI for later use
//                        Log.i("File Path", localPath)
//                        Log.i("File Path", "docName - $docName")
//                        Log.d("File Path", "File Path - $selectedPath")
//                        Log.d("File Path", "File Path from uri- $filePath")
//
////                        val fileExtension = getFileExtension(selectedPath!!)
//
//                        if (selectedPath != null) {
//                            // You now have the imagePath from the DisplayImages activity.
//                            // You can use it as needed, for example, to send the image in a message.
//                            Log.d("ChatActivityDocPath", "Selected Document path: $selectedPath")
//
//                            //val imagePathRef =
//                            //val fileRef = storageRef.child("files/$fileName")
//                            // You can proceed to send the image or display it in your chat.
//                            val docFileName =
//                                "files/${System.currentTimeMillis()}.jpg" // Change the file name as needed
////                            Log.d(TAG, "file name $docFileName")
////                            Log.d(TAG, "document path $selectedPath")
//
//                            val user = User("0", "You", "test", true, Date())
//
//                            val date = Date(System.currentTimeMillis())
//                            val messageId = "Doc_${Random.Default.nextInt()}"
//
//                            val message = Message(
//                                messageId,
//                                user, // Set user ID as needed
//                                null,
//                                date
//                            )
//
//                            val audioUrl = Uri.parse(selectedPath)
//                            val file = File(selectedPath)
//                            if (file.exists()) {
//
//                                Log.d("Document File", "Document File Exists : $file")
//                                val absolutePath = file.absolutePath
////                            Log.d(TAG, "image absolute path $absolutePath")
//                                val fileUri = Uri.fromFile(file)
//                                val fileUrl = fileUri.toString()
//
//                                message.setDocument(
//                                    Message.Document(
//                                        fileUrl,
//                                        getNameFromUrl(fileUrl),
//                                        formatFileSize(getFileSize(fileUrl))
//                                    )
//                                )
//
//
//                                val imageMessage = MessageEntity(
//                                    id = messageId,
//                                    chatId = chatId,
//                                    userName = "This",
//                                    user = userEntity,
//                                    userId = myId,
//                                    text = "📄 Document",
//                                    createdAt = System.currentTimeMillis(),
//                                    imageUrl = null,
//                                    voiceUrl = null,
//                                    voiceDuration = 0,
//                                    status = "Sending",
//                                    videoUrl = null,
//                                    audioUrl = null,
//                                    docUrl = fileUrl,
//                                    fileSize = getFileSize(fileUrl)
//
//                                )
//
//
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    insertMessage(imageMessage)
////                                    dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//                                    updateLastMessage(isGroup, chatId, imageMessage)
//
//                                }
//                            }
//
////                        message.setImage(Message.Image("https://habrastorage.org/getpro/habr/post_images/e4b/067/b17/e4b067b17a3e414083f7420351db272b.jpg"))
////                        message.setImage(Message.Image(imageUrl.toString()))
//                            message.setUser(user)
//                            message.status = "Sending"
//
//                            CoroutineScope(Dispatchers.Main).launch {
////                            delay(500)
//                                super.messagesAdapter?.addToStart(message, true)
//                            }
//
//                            //val fileExtension = getFileExtension(selectedPath!!)
//
//                        }
//                    }
//
//                    // Handle the selected file here
//                    val contentResolver = contentResolver
//
//
//                    Log.d("Document Results", "Picked Document : $data")
//
//
//                }
//            }
//    }
//
//    private suspend fun updateLastMessage(
//        group: Boolean,
//        chatId: String,
//        imageMessage: MessageEntity
//    ) {
//        if (group) {
//            groupDialogViewModel.updateLastMessageForThisGroup(chatId, imageMessage)
//        } else {
//            dialogViewModel.updateLastMessageForThisChat(chatId, imageMessage)
//        }
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//    }
//
//    private fun formatFileSize(fileSize: Long): String {
//        if (fileSize <= 0) {
//            return "0 B"
//        }
//        val units = arrayOf("B", "KB", "MB", "GB", "TB")
//        val digitGroups = (Math.log10(fileSize.toDouble()) / Math.log10(1024.0)).toInt()
//        return String.format(
//            "%.1f %s",
//            fileSize / Math.pow(1024.0, digitGroups.toDouble()),
//            units[digitGroups]
//        )
//    }
//
//    private fun copyFileToInternalStorage(uri: Uri, docName: String): String? {
//        val outputDir = filesDir // Internal storage directory
//        val outputFile = File(outputDir, docName)
//
//        try {
//            val inputStream = contentResolver.openInputStream(uri)
//            val outputStream = FileOutputStream(outputFile)
//            inputStream?.use { input ->
//                outputStream.use { output ->
//                    input.copyTo(output)
//                }
//            }
//            return outputFile.absolutePath
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return null
//    }
//
//    private fun getFilePathFromUri(uri: Uri): String? {
//        val cursor = contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            it.moveToFirst()
//            val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            if (columnIndex != -1) {
//                val fileName = it.getString(columnIndex)
//                val cacheDir = cacheDir
//                val file = File(cacheDir, fileName)
//                val inputStream = contentResolver.openInputStream(uri)
//                val outputStream = FileOutputStream(file)
//                inputStream?.use { input ->
//                    outputStream.use { output ->
//                        input.copyTo(output)
//                    }
//                }
//                return file.absolutePath
//            }
//        }
//        return null
//    }
//
//    private fun getPathFromUri(uri: Uri): String? {
//        val cursor = contentResolver.query(uri, null, null, null, null)
//        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//        cursor?.moveToFirst()
//        val name = cursor?.getString(nameIndex ?: -1)
//        cursor?.close()
//        return name
//    }
//
//    override fun onAddAttachments() {
////        messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true)
//
//        showAttachmentDialog()
//    }
//
//    override fun format(date: Date): String {
////        Log.d("Formatter", "Formatter Initiated And Working Fine.......")
//        return when {
//            DateFormatter.isToday(date) -> {
////                Log.d("Formatter", "The Date is Today")
//                getString(com.uyscuti.social.circuit.R.string.date_header_today)
//            }
//
//            DateFormatter.isYesterday(date) -> {
////                Log.d("Formatter", "The Date is Yesterday")
//                getString(com.uyscuti.social.circuit.R.string.date_header_yesterday)
//            }
//
//            else -> {
////                Log.d("Formatter", "The Date is : " + DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR))
//                DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
//            }
//        }
//    }
//
//    private fun initAdapter() {
//        super.messagesAdapter = MessagesListAdapter(super.senderId, super.imageLoader)
//        super.messagesAdapter?.setDateHeadersFormatter(this)
//        super.messagesAdapter?.enableDateListener(this)
//        super.messagesAdapter?.enableSelectionMode(this)
//        super.messagesAdapter?.setLoadMoreListener(this)
//        super.messagesAdapter?.setIsGroup(isGroup)
//        super.messagesAdapter?.setMessageSentListener(this)
//        super.messagesAdapter?.setDownloadListener(this)
//        super.messagesAdapter?.setMediaClickListener(this)
//        super.messagesAdapter?.setAudioPlayListener(this)
//        super.messagesAdapter?.setDeleteListener(this)
//        messagesList.setAdapter(super.messagesAdapter)
//    }
//
//
//
//
//    override fun onFormatDate(date: Date?): String {
//        return when {
//            DateFormatter.isToday(date) -> {
//                getString(com.uyscuti.social.circuit.R.string.date_header_today)
//            }
//
//            DateFormatter.isYesterday(date) -> {
//                getString(com.uyscuti.social.circuit.R.string.date_header_yesterday)
//            }
//
//            else -> {
//                DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
//            }
//        }
//    }
//
//    private fun showAttachmentDialog() {
//
//        val dialog = BottomSheetDialog(this)
//        dialog.setContentView(com.uyscuti.social.circuit.R.layout.shorts_and_all_feed_file_upload_bottom_dialog)
//
//        val video = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.upload_video)
//        val audio = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.upload_audio)
//        val image = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.upload_image)
//        val camera = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.open_camera)
//        val doc = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.upload_document)
//        val location = dialog.findViewById<LinearLayout>(com.uyscuti.social.circuit.R.id.share_location)
//        // Apply animation to the dialog's view
//        val dialogView =
//            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//        dialogView?.startAnimation(AnimationUtils.loadAnimation(this, com.uyscuti.social.circuit.R.anim.slide_up))
//
//
//        val selectableItemBackground = TypedValue()
//        image?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        image?.setBackgroundResource(selectableItemBackground.resourceId)
//
//
//        video?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        video?.setBackgroundResource(selectableItemBackground.resourceId)
//
//
//        audio?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        audio?.setBackgroundResource(selectableItemBackground.resourceId)
//
//
//        camera?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        camera?.setBackgroundResource(selectableItemBackground.resourceId)
//
//
//        doc?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        doc?.setBackgroundResource(selectableItemBackground.resourceId)
//
//        location?.context?.theme?.resolveAttribute(
//            android.R.attr.selectableItemBackground,
//            selectableItemBackground,
//            true
//        )
//        location?.setBackgroundResource(selectableItemBackground.resourceId)
//
//
//
//        image?.setOnClickListener {
//            val intent = Intent(this@MessagesActivity, ImagesActivity::class.java)
//            dialog.dismiss()
//            imagePickerLauncher.launch(intent)
//
//            // Apply slide-up animation
//
//        }
//
//        video?.setOnClickListener {
////            val intent = Intent(this@ChatActivity, DisplayVideosActivity::class.java)
//            val intent = Intent(this@MessagesActivity, VideosActivity::class.java)
//            dialog.dismiss()
//            videoPickerLauncher.launch(intent)
//
//
//        }
//
//        audio?.setOnClickListener {
//            val intent = Intent(this@MessagesActivity, AudioActivity::class.java)
//
//
//
//            dialog.dismiss()
//            audioPickerLauncher.launch(intent)
//
//
//        }
//
//        doc?.setOnClickListener {
//
//            val currentApiVersion = Build.VERSION.SDK_INT
//            if (currentApiVersion < Build.VERSION_CODES.Q) {
//                val intent = Intent(this@MessagesActivity, DocumentsActivity::class.java)
//                dialog.dismiss()
//                docsPickerLauncher.launch(intent)
//
//
//            } else {
//                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                val mimeTypes = arrayOf(
//                    "application/pdf",
//                    "application/msword",
//                    "application/ms-doc",
//                    "application/doc",
//                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
//                    "text/plain"
//                )
//                intent.type = "*/*" // You can specify the MIME type of the files you want to select
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//                openFilePicker.launch(intent)
//                dialog.dismiss()
//            }
//        }
//        camera?.setOnClickListener {
//            val intent = Intent(this@MessagesActivity, CameraActivity::class.java)
//
//            cameraLauncher.launch(intent)
//            dialog.dismiss()
//        }
//
//        location?.setOnClickListener {
//
//        }
//
//
//        dialog.show()
//    }
//
//
//
//    override fun onAddEmoji() {
//
//        initView()
//    }
//
//
////    private fun observeSendingMessagesI() {
////        CoroutineScope(Dispatchers.Main).launch {
////            messageViewModel.observePendingMessages(chatId)
////                .observe(this@MessagesActivity, Observer { sendingMessages ->
////                    val filteredMessages =
////                        sendingMessages.filter { !observedMessages.contains(it.id) }
////                    filteredMessages.map { observedMessages.add(it.id) }
////
////                    if (isGroup) {
////                        sendPendingMessagesWithRetry(filteredMessages)
////                    } else {
////                        val filtered = filteredMessages.filter { it.chatId != dialog?.dialogName }
////                        sendPendingMessagesWithRetry(filtered)
////                    }
////                })
////        }
////    }
//
//
//    private fun observeSendingMessagesI() {
//        CoroutineScope(Dispatchers.Main).launch {
//            messageViewModel.observePendingMessages(chatId)
//                .observe(this@MessagesActivity, Observer { sendingMessages ->
//                    val filteredMessages =
//                        sendingMessages.filter { !observedMessages.contains(it.id) }
//                    filteredMessages.map { observedMessages.add(it.id) }
//
//                    if (isGroup) {
//                        sendPendingMessagesWithRetry(filteredMessages)
//                    } else {
//                        val filtered = filteredMessages.filter { it.chatId != dialog?.dialogName }
//                        sendPendingMessagesWithRetry(filtered)
//                    }
//                })
//        }
//    }
//
//
//    private fun onMessageSendSuccess(messageId: String) {
//        CoroutineScope(Dispatchers.Main).launch {
//            // Update message status in adapter to "Sent"
//            messagesAdapter?.updateMessageStatus(messageId, STATUS_SENT)
//
//            // Update in database
//            CoroutineScope(Dispatchers.IO).launch {
//                messageViewModel.updateMessageStatus(messageId, STATUS_SENT)
//            }
//        }
//    }
//
//
//    private fun onMessageDelivered(messageId: String) {
//        CoroutineScope(Dispatchers.Main).launch {
//            messagesAdapter?.updateMessageStatus(messageId, STATUS_DELIVERED)
//
//            CoroutineScope(Dispatchers.IO).launch {
//                messageViewModel.updateMessageStatus(messageId, STATUS_DELIVERED)
//            }
//        }
//    }
//
//    private fun observeTemporallyMessages(name: String) {
//        CoroutineScope(Dispatchers.Main).launch {
//            messageViewModel.observeTempMessages(name)
//                .observe(this@MessagesActivity, Observer { tempMessages ->
////                    Log.d("Temporally", "Temporally Messages Found : $tempMessages")
//                    if (tempMessages.isNotEmpty()) {
//                        updateTempMessages(tempMessages)
//                    }
//                })
//        }
//    }
//
//    private fun updateTempMessages(messages: List<MessageEntity>) {
//        Log.d("ChatId", "ChatId To Insert : $chatId")
//        if (chatId != messages.first().chatId) {
//            CoroutineScope(Dispatchers.IO).launch {
//                messages.map {
//                    it.chatId = chatId
//                    messageViewModel.updateMessage(it)
//                }
//            }
//        }
//    }
//
//    private fun showInternetConnectionSnackbar(view: View) {
//        Snackbar.make(view, "Check your internet connection", Snackbar.LENGTH_LONG)
//            .setAction("Retry") {
//                // You can add a retry action here if needed.
//            }
//            .show()
//    }
//
//    private fun addIdToFilePath(originalPath: String, id: String): String {
//        val file = File(originalPath)
//        val fileName = file.nameWithoutExtension
//        val fileExtension = file.extension
//
//        // Construct the new path with the ID inserted before the extension
////        Log.i(offlineTag, "New file path - ${file.parent}/$fileName$id.$fileExtension")
//        return "${file.parent}/$fileName$id.$fileExtension"
//    }
//
//    private fun sendAttach10(
//        contentUri: Uri,
//        message: MessageEntity,
//        callback: (Boolean) -> Unit
//    ) {
//
//        Log.d("DecodedAtt", "Decoded : $contentUri")
//
//        try {
//            // Existing code for sending an image...
//
//
//            val encodedFilePath = URLDecoder.decode(contentUri.path, "UTF-8")
//            val decoded = Uri.decode(encodedFilePath)
//
//            val uniqueId = System.currentTimeMillis().toString()
//            val newFilePath = addIdToFilePath(decoded, uniqueId)
//
//            val newAttachmentFile = File(newFilePath)
//
////            val encodedFilePath = URLDecoder.decode(filePath, "UTF-8")
//            val attachmentFile = File(decoded)
//
//            // Use the ContentResolver to open an InputStream for the content URI
//            contentResolver.openInputStream(contentUri)?.use { inputStream ->
//                // Read data from the input stream
//                val fileBytes = inputStream.readBytes()
//
//                // Continue with your existing logic...
//
//                // Create a RequestBody from the byte array
//                val requestAttachment = RequestBody.Companion.create("image/*".toMediaTypeOrNull(), fileBytes)
//
//                // Create a MultipartBody.Part from the RequestBody
//                val filePart = MultipartBody.Part.createFormData(
//                    "attachments",
//                    newAttachmentFile.name,
//                    requestAttachment
//                )
//
//                Log.i("SendAttachment", "Sending attachment name: ${newAttachmentFile.name}")
//
//                // Continue with the rest of your code...
//            }
//        } catch (e: Exception) {
//            // Handle exceptions
//            e.printStackTrace()
//        }
//    }
//
//
//    private fun getUriForFileByName(context: Context, fileName: String): Uri? {
//        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(MediaStore.MediaColumns.DATA)
//        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
//        val selectionArgs = arrayOf(fileName)
//
//        context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
//            ?.use { cursor ->
//                if (cursor.moveToFirst()) {
//                    val filePath =
//                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
//                    return Uri.parse("file://$filePath")
//                }
//            }
//        return null
//    }
//
//
//    private fun sendAttachmentContent(
//        contentUri: Uri,
//        message: MessageEntity,
//        callback: (Boolean) -> Unit
//    ) {
//        Log.d("DecodedAtt", "Decoded : $contentUri")
//
//        try {
//            val decoded = Uri.decode(contentUri.toString())
//            val prepared = Uri.parse(decoded)
//
//            // Use the ContentResolver to open an InputStream for the content URI
//            contentResolver.openInputStream(prepared)?.use { inputStream ->
//                // Read data from the input stream
//                val fileBytes = inputStream.readBytes()
//
//                val uniqueId = System.currentTimeMillis().toString()
//                val newFilePath = addIdToFilePath(decoded, uniqueId)
//
//                val newAttachmentFile = File(newFilePath)
//
//                // Continue with your existing logic...
//
//                // Create a RequestBody from the byte array
//                val requestAttachment = RequestBody.Companion.create("image/*".toMediaTypeOrNull(), fileBytes)
//
//                // Create a MultipartBody.Part from the RequestBody
//                val filePart = MultipartBody.Part.createFormData(
//                    "attachments",
//                    newAttachmentFile.name,
//                    requestAttachment
//                )
//
//                Log.i("SendAttachment", "Sending attachment name: ${newAttachmentFile.name}")
//
//                if (newAttachmentFile.name.isNotEmpty()) {
//                    GlobalScope.launch(Dispatchers.IO) {
//                        val response = retrofitIns.apiService.uploadImage(chatId, filePart)
//
//                        if (response.isSuccessful) {
//                            // Existing code for a successful response...
//                            CoroutineScope(Dispatchers.IO).launch {
//                                messageViewModel.updateMessageStatus(message)
//                            }
//
//                            // Continue with your existing logic...
//                            val date = Date(message.createdAt)
//                            val user =
//                                User(
//                                    "0",
//                                    message.userName,
//                                    "http://i.imgur.com/ROz4Jgh.png",
//                                    true,
//                                    Date()
//                                )
//                            val msg = Message(
//                                message.id,
//                                user,
//                                null,
//                                date
//                            )
//
//                            val messageContent = if (message.imageUrl != null) {
//
//                                Log.d("File Sent", "Image found ${message.imageUrl}")
////                        user.id = "0"
//                                Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                ).apply {
//                                    setImage(Message.Image(message.imageUrl!!))
//
//                                }
//                            } else if (message.videoUrl != null) {
////                        user.id = "0"
//                                Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                ).apply {
//                                    setVideo(Message.Video(message.videoUrl!!))
//                                }
//                            } else if (message.audioUrl != null) {
////                        user.id = "0"
//                                Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                ).apply {
//                                    setAudio(
//                                        Message.Audio(
//                                            message.audioUrl!!,
//                                            0,
//                                            getNameFromUrl(message.audioUrl!!)
//                                        )
//                                    )
//                                }
//                            } else if (message.text == "None" && message.voiceUrl != null) {
////                        user.id = "0"
//                                Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                ).apply {
//                                    setVoice(Message.Voice(message.voiceUrl!!, 10000))
//                                }
//                            } else if (message.docUrl != null) {
////                        user.id = "0"
//                                Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                ).apply {
//
//                                    val size = getFileSize(message.docUrl!!)
//                                    setDocument(
//                                        Message.Document(
//                                            message.docUrl!!,
//                                            getNameFromUrl(message.docUrl!!),
//                                            formatFileSize(size)
//                                        )
//                                    )
//                                }
//                            } else {
//                                Message(
//                                    message.id,
//                                    user,
//                                    message.text,
//                                    date
//                                )
//                            }
//                            runOnUiThread {
//                                showToast("File Sent")
//                                messageContent.status = "Sent"
//                                messagesAdapter?.notifyMessageSent(messageContent)
//                            }
//
//                            callback(true) // Signal that the message was sent successfully
//                        } else {
//                            // Existing code for an unsuccessful response...
//                            runOnUiThread {
//                                showToast("File Not Sent")
//                            }
//                            callback(false) // Signal that the message failed to send
//                        }
//                    }
//                } else {
//                    // Handle the case where newAttachmentFile.name is empty.
//                    // You can choose to skip or take other actions.
//                    callback(false)
//                }
//            }
//        } catch (e: Exception) {
//            Log.e("AttachmentError", "Error : ${e.message}")
//            CoroutineScope(Dispatchers.IO).launch {
//                messageViewModel.updateMessageStatus(message)
//            }
//            e.printStackTrace()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        userStatusManager?.start()
//        coreChatSocketClient.chatListener = this
//        chatManager.listener = this
//
//        if (!isGroup){
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(1000)
//                dialog?.users?.first()?.id?.let { sendSeenReport(chatId, it) }
//            }
//        } else {
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(1000)
//                dialog?.lastMessage?.user?.id?.let { sendDeliveryReport(chatId, it) }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        userStatusManager?.stop()
//        coreChatSocketClient.chatListener = null
//        EventBus.getDefault().unregister(this)
//    }
//
//    private fun sendAttachment(
//        filePath: String,
//        message: MessageEntity,
//        callback: (Boolean) -> Unit
//    ) {
//        CoroutineScope(Dispatchers.IO).launch {
//            withContext(NonCancellable) {
//                Log.d("DecodedAtt", "Decoded : $filePath")
//                try {
//                    // Existing code for sending an image...
//
//                    val encodedFilePath = URLDecoder.decode(filePath, "UTF-8")
//                    val decoded = Uri.decode(encodedFilePath)
//
//                    val uniqueId = System.currentTimeMillis().toString()
//                    val newFilePath = addIdToFilePath(decoded, uniqueId)
//
//                    val newAttachmentFile = File(newFilePath)
//
////            val encodedFilePath = URLDecoder.decode(filePath, "UTF-8")
//                    val attachmentFile = File(decoded)
//                    val fileBytes = attachmentFile.readBytes()
//
//                    Log.i("SendAttachment", "Sending attachment path: $filePath")
//                    Log.i("SendAttachment", "Sending attachment encoded path: $encodedFilePath")
//
//                    // Create a RequestBody from the byte array
//                    val requestAttachment =
//                        RequestBody.Companion.create("image/*".toMediaTypeOrNull(), fileBytes)
//
//                    // Create a MultipartBody.Part from the RequestBody
//                    val filePart =
//                        MultipartBody.Part.createFormData(
//                            "attachments",
//                            newAttachmentFile.name,
//                            requestAttachment
//                        )
//
//                    Log.i("SendAttachment", "Sending attachment name: ${attachmentFile.name}")
//
//
//                    if (attachmentFile.name.isNotEmpty()) {
//                        GlobalScope.launch(Dispatchers.IO) {
//                            val response = retrofitIns.apiService.uploadImage(chatId, filePart)
//
//                            if (response.isSuccessful) {
//                                // Existing code for a successful response...
//                                val responseData =
//                                    response.body() // This will contain the response data from the server
//
//                                CoroutineScope(Dispatchers.IO).launch {
//                                    messageViewModel.updateMessageStatus(message)
//                                }
//
//                                val date = Date(message.createdAt)
//                                val user =
//                                    User(
//                                        "0",
//                                        message.userName,
//                                        "http://i.imgur.com/ROz4Jgh.png",
//                                        true,
//                                        Date()
//                                    )
//                                val msg = Message(
//                                    message.id,
//                                    user,
//                                    null,
//                                    date
//                                )
//
//                                val messageContent = if (message.imageUrl != null) {
//
//                                    Log.d("File Sent", "Image found ${message.imageUrl}")
//
//                                    Message(
//                                        message.id,
//                                        user,
//                                        null,
//                                        date
//                                    ).apply {
//                                        setImage(Message.Image(message.imageUrl!!))
//
//                                    }
//                                } else if (message.videoUrl != null) {
//
//                                    Message(
//                                        message.id,
//                                        user,
//                                        null,
//                                        date
//                                    ).apply {
//                                        setVideo(Message.Video(message.videoUrl!!))
//                                    }
//                                } else if (message.audioUrl != null) {
//
//                                    Message(
//                                        message.id,
//                                        user,
//                                        null,
//                                        date
//                                    ).apply {
//                                        setAudio(
//                                            Message.Audio(
//                                                message.audioUrl!!,
//                                                0,
//                                                getNameFromUrl(message.audioUrl!!)
//                                            )
//                                        )
//                                    }
//                                } else if (message.text == "None" && message.voiceUrl != null) {
//
//                                    Message(
//                                        message.id,
//                                        user,
//                                        null,
//                                        date
//                                    ).apply {
//                                        setVoice(Message.Voice(message.voiceUrl!!, 10000))
//                                    }
//                                } else if (message.docUrl != null) {
//
//                                    Message(
//                                        message.id,
//                                        user,
//                                        null,
//                                        date
//                                    ).apply {
//
//                                        val size = getFileSize(message.docUrl!!)
//                                        setDocument(
//                                            Message.Document(
//                                                message.docUrl!!,
//                                                getNameFromUrl(message.docUrl!!),
//                                                formatFileSize(size)
//                                            )
//                                        )
//                                    }
//                                } else {
//                                    Message(
//                                        message.id,
//                                        user,
//                                        message.text,
//                                        date
//                                    )
//                                }
//
//                                runOnUiThread {
//                                    showToast("File Sent")
//
//                                    messageContent.status = "Sent"
//                                    messagesAdapter?.notifyMessageSent(messageContent)
//                                }
//                                callback(true) // Signal that the message was sent successfully
//                            } else {
//                                // Existing code for an unsuccessful response...
//                                runOnUiThread {
//                                    showToast("File Not Sent")
//                                }
//                                callback(false) // Signal that the message failed to send
//                            }
//                        }
//                    } else {
//                        // Handle the case where imageFile.name is empty.
//                        // You can choose to skip or take other actions.
//                        callback(false)
//                    }
//                } catch (e: Exception) {
//                    Log.e("AttachmentError", "Error : ${e.message}")
//                    CoroutineScope(Dispatchers.IO).launch {
//                        messageViewModel.updateMessageStatus(message)
//                    }
//                    e.printStackTrace()
//                }
//            }
//        }
//
//
//
//    }
//
////    private fun permit() {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
////            Log.d("DeviceVersion", "Higher Than 11")
////
////            if (Environment.isExternalStorageManager()) {
////                // Permission already granted
////                // Continue with your file operations
////                Log.d("DeviceVersion", "Access Already Permitted")
////
////            } else {
////                Log.d("DeviceVersion", "Access Not Permitted, Please Grant Manually")
////
////                // Request the permission
////                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
////                intent.data = Uri.parse("package:$packageName")
////                startActivityForResult(intent, MY_MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
////            }
////        } else {
////            // Device is not running Android 11 or higher
////            // Continue with your file operations
////            Log.d("DeviceVersion", "Lower Than 11")
////        }
////    }
//
//
//    private fun copyFileToInternalStorage(
//        context: Context,
//        sourceFilePath: String,
//        destinationFileName: String
//    ): File? {
//        val destinationDir = context.filesDir // Use "context.cacheDir" for cache directory
//
//        // Create the destination file
//        val destinationFile = File(destinationDir, destinationFileName)
//
//        try {
//            // Open the source and destination streams
//            FileInputStream(File(sourceFilePath)).use { inputStream ->
//                FileOutputStream(destinationFile).use { outputStream ->
//                    // Copy the file content
//                    val buffer = ByteArray(4 * 1024)
//                    var read: Int
//                    while (inputStream.read(buffer).also { read = it } != -1) {
//                        outputStream.write(buffer, 0, read)
//                    }
//                    outputStream.flush()
//                }
//            }
//            return destinationFile
//        } catch (e: IOException) {
//            Log.e("FileOperation", "Error : ${e.message}")
//            e.printStackTrace()
//        }
//
//        return null
//    }
//
//    private fun sendPendingMessagesWithRetry(
//        sendingMessages: List<MessageEntity>,
//        currentIndex: Int = 0,
//        retryCount: Int = 0
//
//    ) {
//        if (currentIndex >= sendingMessages.size || retryCount >= MAX_RETRY_COUNT) {
//            // All messages have been sent or reached the maximum number of retries.
//            if (currentIndex >= sendingMessages.size) {
//                // All messages have been sent.
//                Log.d("Sending Messages", "All Messages Have Been Sent")
//            } else {
//                // Maximum retry attempts reached.
//                Log.d("Sending Messages", "Maximum Retry Attempts Reached")
//                showInternetConnectionSnackbar(binding.root) // Show the Snackbar
//            }
//            return
//        }
//
//        Log.d("Sending Messages", "Messages To send : ${sendingMessages.size}")
//
//        val currentMessage = sendingMessages[currentIndex]
//
//        val filePath: String? = if (currentMessage.imageUrl?.isNotEmpty() == true) {
//            currentMessage.imageUrl?.let { File(it).absolutePath.substringAfter("/file:") }
//        } else if (currentMessage.voiceUrl?.isNotEmpty() == true) {
//            currentMessage.voiceUrl?.let { File(it).absolutePath.substringAfter("/file:") }
//        } else if (currentMessage.audioUrl?.isNotEmpty() == true) {
//            currentMessage.audioUrl?.let { File(it).absolutePath.substringAfter("/file:") }
//        } else if (currentMessage.videoUrl?.isNotEmpty() == true) {
//            currentMessage.videoUrl?.let { File(it).absolutePath.substringAfter("/file:") }
//        } else if (currentMessage.docUrl?.isNotEmpty() == true) {
//            currentMessage.docUrl?.let { File(it).absolutePath.substringAfter("/file:") }
//        } else {
//            null
//        }
//
//        if (filePath != null) {
//            val encodedFilePath = URLEncoder.encode(filePath, "UTF-8")
//
//            if (encodedFilePath != null) {
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//
//                    val contentUri = try {
//                        getFileUri(this@MessagesActivity, filePath)
//                    } catch (e: Exception) {
//                        Uri.fromFile(File(filePath));
//                    }
//
//                    val ul = getUriForFileByName(this, getFileNameFromUrl(filePath))
//
//
//                    sendAttachmentContent(contentUri, currentMessage) { success ->
//                        if (success) {
//
//                            onMessageSendSuccess(currentMessage.id)
//                            // If the message was sent successfully, proceed to the next one.
//                            sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
//                        } else {
//                            Log.e("SendAttachment", "Failed to send attachment")
//                        }
//                    }
//                } else {
//
//                    sendAttachment(encodedFilePath, currentMessage) { success ->
//                        if (success) {
//
//                            onMessageSendSuccess(currentMessage.id)
//                            // If the message was sent successfully, proceed to the next one.
//                            sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
//                        } else {
//                            // Handle the case where the message failed to send.
//                            if (retryCount > MAX_RETRY_COUNT) {
//                                // Retry sending the message after a delay (e.g., 5 seconds).
//                                val delayMillis = 5000
//
//                            } else {
//                                // Reached the maximum retry count for this message.
//                                // You can choose to skip or take other actions.
//                                sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
////                                showToast("Sending Failed, Check Your Internet Connection And Try Again")
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            // Handle the case where filePath is null (no valid image or voice URL).
//            // You can choose to skip or take other actions.
//            val date = Date(currentMessage.createdAt)
//            val user = User("0", currentMessage.userName, currentMessage.user.avatar, true, Date())
//
////            Log.d("MessageSent", "Current Message Id : ${currentMessage.id}")
//
//            val uiMessage = Message(
//                currentMessage.id,
//                user,
//                currentMessage.text,
//                date
//            )
//
//            sendTextMessage(currentMessage.text, uiMessage, currentMessage) { success ->
//                if (success) {
//
//                    onMessageSendSuccess(currentMessage.id)
//
//                    sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
//                } else {
//                    // Handle the case where the message failed to send.
//                    retryCount+1
//                    if (retryCount > MAX_RETRY_COUNT) {
//                        // Retry sending the message after a delay (e.g., 5 seconds).
//
//                        sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
//
//                    } else {
//                        // Reached the maximum retry count for this message.
//                        // You can choose to skip or take other actions.
//                        sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
////                        showToast("Sending Failed, Check Your Internet Connection And Try Again")
//                    }
//                }
//            }
//            sendPendingMessagesWithRetry(sendingMessages, currentIndex + 1, 0)
//        }
//    }
//
//
//    private fun getFileUri(context: Context, filePath: String): Uri {
//        val file = File(filePath)
//        return FileProvider.getUriForFile(
//            context,
//            "com.uyscuti.social.provider",  // Change to your app's package name
//            file
//        )
//    }
//
//    override fun onMessageSent(message: Message) {
//
////        Log.d("MessageSent", "On Message Sent : $message")
//        CoroutineScope(Dispatchers.Main).launch {
//            message.status = "Sent"
//            super.messagesAdapter?.modifyMessageStatus(message)
//        }
//    }
//
//    override fun onMediaClick(url: String, view: View?, message: Message?) {
//        when (getFileType(url)) {
//            FileType.IMAGE -> {
//                val intent = Intent(this, ViewImagesActivity::class.java)
//                intent.putExtra("imageUrl", url)
//                intent.putExtra("owner", message?.user?.name)
//                startActivity(intent)
//            }
//
//            FileType.AUDIO -> {
//
//
//            }
//
//            FileType.VIDEO -> {
//                val intent = Intent(this, PlayVideoActivity::class.java)
//                intent.putExtra("videoPath", url)
//                intent.putExtra("owner", message?.user?.name)
//                startActivity(intent)
//            }
//
//            FileType.DOCUMENT -> {
//                Log.d("MediaPreview", "Doc Url : $url")
//
//                if (url.startsWith("/storage/") || url.startsWith("file:/")) {
//                    // Determine the actual file path from the URL
//                    val filePath = if (url.startsWith("file:/")) {
//                        // Remove the "file:/" prefix
//                        url.substring(7)
//                    } else {
//                        url
//                    }
//
//                    val file = File(filePath)
//                    Log.d("MediaPreview", "The Doc is a Local File")
//                    Log.d("MediaPreview", "File Path : $file")
//
//                    if (file.exists()) {
//                        Log.d("MediaPreview", "File Exist : $file")
//                        val fileUri = FileProvider.getUriForFile(
//                            this,
//                            "com.uyscuti.social.provider",
//                            file
//                        )
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        intent.setDataAndType(fileUri, "application/pdf")
//                        startActivity(intent)
//                    } else {
//                        Log.d("MediaPreview", "File Does Not Exist")
//                    }
//                } else {
//                    Log.d("MediaPreview", "Not A Local File")
//
//                    if (message != null) {
//                        down(url, "Documents", message)
//                    }
//                }
//            }
//
//            FileType.OTHER -> {
//                // Handle other types, if needed
//            }
//        }
//    }
//
//    override fun onAudioPlayClick(
//        url: String?,
//        playPause: ImageView?,
//        duration: TextView?,
//        seekBar: SeekBar?,
//        message: Message?
//    ) {
//        Log.i("PlayAudio", "Audio Player listener")
//        Log.i("PlayAudio", "Audio Listener file path - $url")
//
//        if (url != null) {
//            if (message != null) {
//                if (duration != null) {
//                    if (playPause != null) {
//                        togglePlayPause(url, seekBar, duration, message, playPause)
//                    }
//                }
//            }
//        }
//
//
//        if (mediaPlayer != null) {
//            mediaPlayer!!.setOnCompletionListener {
//                // The audio playback has completed.
//                // You can perform any actions you need when the audio finishes.
//                // For example, you can update UI elements or play the next audio.
//                // If you're using a different audio library, use the respective callback.
////            playButton.visibility = View.VISIBLE
////            pauseButton.visibility = View.GONE
//            }
//        }
//    }
//
//
//    override fun onDownloadClick(
//        url: String?,
//        progressCountTv: TextView?,
//        progressbar: ProgressBar?,
//        downloadImageView: ImageView?,
//        fileDisplay: ImageView?,
//        fileLocation: String?,
//        message: Message
//    ) {
//        Log.d(
//            "Download",
//            "OnDownload $url  to path : $fileLocation progressBar : $progressbar fileDisplay : $fileDisplay"
//        )
//
//        if (url != null) {
//            if (progressbar != null) {
//                if (fileDisplay != null) {
//                    if (fileLocation != null) {
//
//                        val permissions = arrayOf(
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.READ_EXTERNAL_STORAGE
//                        )
//
//                        if (ContextCompat.checkSelfPermission(
//                                this,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE
//                            )
//                            != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
//                        } else {
//                            // You have permission, proceed with your file operations
//
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//                                // Check if the permission is not granted
//                                if (ContextCompat.checkSelfPermission(
//                                        this,
//                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                                    ) != PackageManager.PERMISSION_GRANTED
//                                ) {
//                                    // Request the permission
//                                    ActivityCompat.requestPermissions(
//                                        this,
//                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                                        WRITE_EXTERNAL_STORAGE_REQUEST_CODE
//                                    )
//                                } else {
//                                    // Permission already granted, proceed with your code
////                                    downloadFile(mUrl)
//                                    downld(url, progressbar, fileDisplay, fileLocation, message)
//
//                                }
//
////                                downlod(url, progressbar, fileDisplay, fileLocation, message)
//                            } else {
//                                download(url, progressbar, fileDisplay, fileLocation, message)
//                            }
//                        }
//
////                        download2(url,progressbar,fileLocation)
//                    }
//                }
//            }
//        }
//    }
//
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // Permission granted, proceed with your code
////                    downloadFile(mUrl)
//
//                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
//
//                } else {
//                    // Permission denied, handle accordingly (e.g., show a message to the user)
//                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//
//
//    override fun audioDownloadClick(
//        url: String?,
//        progressCountTv: TextView?,
//        progressbar: ProgressBar?,
//        downloadImageView: ImageView?,
//        audioPlay: ImageView?,
//        audioPause: ImageView?,
//        seekBar: SeekBar?,
//        fileLocation: String?,
//        startDurationTv: TextView?,
//        endDurationTv: TextView?
//    ) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onSocketConnect() {
//        CoroutineScope(Dispatchers.Main).launch {
////            showToast("Socket Connected")
//            userStatusManager?.start()
//            if (!isGroup){
//                CoroutineScope(Dispatchers.IO).launch {
//                    delay(1000)
//                    dialog?.users?.first()?.id?.let { sendSeenReport(chatId, it) }
//                }
//            }
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onDirectReplyEvent(event: DirectReplyEvent) {
//        // Handle the direct reply in your activity
//        // Update UI, perform actions, etc.
////        Toast.makeText(this, "Direct Reply: ${event.message}", Toast.LENGTH_SHORT).show()
//
////        val message = createMessage(event.message,event.chatId)
////        CoroutineScope(Dispatchers.IO).launch {
////            messageViewModel.insertMessage(message)
////            dialogViewModel.updateLastMessageForThisChat(event.chatId,message)
////        }
//
//        if (event.chatId == chatId){
//            val avatar = settings.getString("avatar", "avatar").toString()
//
//            val user = User(
//                "0",
//                "You",
//                avatar,
//                true,
//                Date()
//            )
//
//            val newMessage = Message(
//                event.chatId,
//                user,
//                event.message,
//                Date()
//            )
//
//            newMessage.status = "Sent"
//
//            super.messagesAdapter?.addToStart(newMessage, true)
//
//            CoroutineScope(Dispatchers.IO).launch {
//                if (isGroup){
//                    resetGroupUnreadCount(chatId)
//                } else {
//                    dialog?.let { resetUnreadCount(it) }
//                }
//            }
//        }
//    }
//
//    override fun onNewMessage(message: com.uyscuti.social.network.api.models.Message) {
//        CoroutineScope(Dispatchers.IO).launch {
//            Log.d(TAG, "In This Chat : $message")
//            Log.d(TAG, "In This Chat attachment: ${message.attachments}")
//
//
//
////            val messageEntity: MessageEntity = message.toMessageEntity()
////            insertMessage(messageEntity)
//
//            if (message.chat == chatId) {
//                val user = User(
//                    "1",
//                    message.sender.username,
//                    message.sender.avatar.url,
//                    true, Date()
//                )
//
//                // Initialize URLs as null
//                var imageUrl: String? = null
//                var audioUrl: String? = null
//                var videoUrl: String? = null
//                var docUrl: String? = null
//
//                // Handle attachments and assign URLs
//                if (message.attachments != null && message.attachments?.isNotEmpty() == true) {
//                    val attachments = message.attachments
//                    if (attachments != null) {
//                        for (attachment in attachments) {
//                            when (getFileType(attachment.url)) {
//                                FileType.IMAGE -> {
//                                    imageUrl = attachment.url
//                                    Log.d(
//                                        "Received Attachment",
//                                        "Image, Path Of Image Received: $imageUrl"
//                                    )
//                                }
//
//                                FileType.AUDIO -> {
//                                    audioUrl = attachment.url
//                                    Log.d(
//                                        "Received Attachment",
//                                        "Audi, Path Of Image Received: $audioUrl"
//                                    )
//
//                                }
//
//                                FileType.VIDEO -> {
//                                    videoUrl = attachment.url
//                                    Log.d(
//                                        "Received Attachment",
//                                        "Video, Path Of Image Received: $videoUrl"
//                                    )
//
//                                }
//
//                                FileType.DOCUMENT -> {
//                                    docUrl = attachment.url
//                                    Log.d(
//                                        "Received Attachment",
//                                        "Document, Path Of Image Received: $docUrl"
//                                    )
//
//                                }
//
//                                FileType.OTHER -> {
//                                    // Handle other types, if needed
//                                }
//                            }
//                        }
//                    }
//                }
//
//
////                showToast(message.content)
//
//                Log.d(TAG, "In This Chat : ${message.content}")
//
//                val createdAt = convertIso8601ToUnixTimestamp(message.createdAt)
//
//                val date = Date(createdAt)
//
//                val newMessage = Message(
//                    message._id,
//                    user,
//                    message.content,
//                    date
//                )
//
//                newMessage.setImage(imageUrl?.let { Message.Image(it) })
//
//                newMessage.setVideo(videoUrl?.let { Message.Video(it) })
//
//                newMessage.setDocument(
//                    docUrl?.let {
//                        Message.Document(
//                            it,
//                            getNameFromUrl(docUrl),
//                            formatFileSize(getFileSize(docUrl))
//                        )
//                    }
//                )
//
//                newMessage.setAudio(
//                    audioUrl?.let {
//                        Message.Audio(
//                            it,
//                            0,
//                            getNameFromUrl(audioUrl)
//                        )
//                    }
//                )
//
//                withContext(Dispatchers.Main) {
//                    super.messagesAdapter?.addToStart(newMessage, true)
//                }
//
//                if (!isGroup){
//
//                    onMessageDelivered(message._id)
//                    delay(700)
//                    sendSeenReport(chatId,message.sender._id)
//                }
//
//                if(isGroup){
//                    resetGroupUnreadCount(chatId)
//                } else {
//                    resetUnreadCount(dialog)
//                }
//
//
//            }
//        }
//    }
//
//    private fun sendDeliveryReport(chatId: String,senderId: String){
//        try {
//            coreChatSocketClient.sendDeliveryReport(chatId,senderId)
//        } catch (e:Exception){
//            e.printStackTrace()
//        }
//    }
//
//
//    override fun onDeliveryReport() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val deliveredMessage = messageViewModel.getMyLastMessageByChat(chatId)
//
//            delay(500)
//
//            if (deliveredMessage != null) {
//                deliveredMessage.status = "Delivered"
//                messageViewModel.updateMessage(deliveredMessage)
//
//                val date = Date(deliveredMessage.createdAt)
//                val user =
//                    User("0", deliveredMessage.userName, deliveredMessage.user.avatar, true, Date())
//
////            Log.d("MessageSent", "Current Message Id : ${currentMessage.id}")
//
//                val uiMessage = Message(
//                    deliveredMessage.id,
//                    user,
//                    deliveredMessage.text,
//                    date
//                )
//
//                withContext(Dispatchers.Main) {
//                    uiMessage.status = "Delivered"
////                    super.messagesAdapter?.modifyMessageStatus(uiMessage)
////                    showToast("Message Delivered")
//                    getAndUpdateMessages("Delivered")
//                }
//            }
//        }
//    }
//
//    override fun onNotification(notification: Notification) {
//        TODO("Not yet implemented")
//    }
//
//    private fun resetUnreadCount(dialog: Dialog?) {
//        try {
//            if (dialog != null){
//                CoroutineScope(Dispatchers.IO).launch {
//                    val dg = dialogViewModel.getDialog(dialog.id)
//                    dg.unreadCount = 0
//                    dialogViewModel.updateDialog(dg)
//                }
//            }
//        } catch (e:Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun resetGroupUnreadCount(chatId: String) {
//        try {
//            if (dialog != null){
//                CoroutineScope(Dispatchers.IO).launch {
//                    val dg = groupDialogViewModel.getGroupDialog(chatId)
//                    dg.unreadCount = 0
//                    groupDialogViewModel.updateGroupDialog(dg)
//                }
//            }
//        } catch (e:Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun onMessageOpenedReport() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val deliveredMessage = messageViewModel.getMyLastMessageByChat(chatId)
//
//            delay(1000)
//
//            if (deliveredMessage != null) {
////                deliveredMessage.status = "Seen"
////                messageViewModel.updateMessage(deliveredMessage)
//
//                val date = Date(deliveredMessage.createdAt)
//                val user =
//                    User("0", deliveredMessage.userName, deliveredMessage.user.avatar, true, Date())
//
////            Log.d("MessageSent", "Current Message Id : ${currentMessage.id}")
//
//                val uiMessage = Message(
//                    deliveredMessage.id,
//                    user,
//                    deliveredMessage.text,
//                    date
//                )
//
//                withContext(Dispatchers.Main) {
//                    uiMessage.status = "Seen"
////                    super.messagesAdapter?.modifyMessageStatus(uiMessage)
////                    showToast("Message Delivered")
//                    getAndUpdateMessages("Seen")
//                }
//            }
//        }
//    }
//
//    private fun getAndUpdateMessages(status: String){
//        CoroutineScope(Dispatchers.Main).launch {
//            val sentMessages = if (status == "Delivered") super.messagesAdapter?.allSentMessages else super.messagesAdapter?.allMessagesToUpdate
//
//            if (sentMessages!!.isNotEmpty()){
//                sentMessages.map {
//                    it.status = status
//                    super.messagesAdapter?.modifyMessageStatus(it)
//                }
//            }
//        }
//    }
//
//
//    private fun com.uyscuti.social.network.api.models.Message.toMessageEntity(): MessageEntity {
//
//        val createdAt = convertIso8601ToUnixTimestamp(createdAt)
//
//        val user = UserEntity(
//            id = _id,
//            name = username,
//            avatar = sender.avatar.url,
//            lastSeen = sender.lastseen,
//            online = false
//        )
//
//
//        return MessageEntity(
//            id = _id,
//            chatId = chatId,
//            userName = sender.username,
//            user = user,
//            text = content,
//            createdAt = createdAt,
//            imageUrl = null,
//            voiceUrl = null,
//            voiceDuration = 0,
//            userId = sender._id,
//            status = "Received",
//            videoUrl = null,
//            audioUrl = null,
//            docUrl = null,
//            fileSize = 0
//        )
//    }
//
//
//    private fun convertIso8601ToUnixTimestamp(iso8601Date: String): Long {
//        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//        sdf.timeZone = TimeZone.getTimeZone("UTC")
//
//        val date = sdf.parse(iso8601Date)
//        return date?.time ?: 0
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val itemName = when (item.itemId) {
//            com.uyscuti.social.circuit.R.id.menu_video -> "Video"
//            com.uyscuti.social.circuit.R.id.menu_voice -> "Voice"
//            com.uyscuti.social.circuit.R.id.menu_edit -> "Edit"
//            else -> ""
//        }
//        if (itemName.isNotEmpty()) {
//
//            when (itemName) {
//                "Video" -> {
//                    //
//                    mainRepository.sendConnectionRequest(
//                        DataModel(
//                            DataModelType.StartVideoCall, username, dialog?.dialogName, null
//                        )
//                    ) {
//                        if (it) {
//                            startActivity(Intent(this, CallActivity::class.java).apply {
//                                putExtra("target", dialog?.dialogName)
//                                putExtra("isVideoCall", true)
//                                putExtra("isCaller", true)
//                                putExtra("avatar", dialog!!.dialogPhoto)
//                            })
//                        }
//                    }
//                    val newCallLog = CallLogEntity(
//                        id = Random.Default.nextLong(),
//                        callerName = dialog!!.dialogName,
//                        System.currentTimeMillis(),
//                        callDuration = 0,
//                        "Outgoing",
//                        "Not Answered",
//                        dialog!!.dialogPhoto,
//                        dialog!!.id,
//                        true,
//                        false
//                    )
//                    insertCallLog(newCallLog)
//                }
//
//                "Voice" -> {
//                    //
//                    mainRepository.sendConnectionRequest(
//                        DataModel(
//                            DataModelType.StartVoiceCall, username, dialog?.dialogName, null
//                        )
//                    ) {
//                        if (it) {
//                            startActivity(Intent(this, CallActivity::class.java).apply {
//                                putExtra("target", dialog?.dialogName)
//                                putExtra("isVideoCall", false)
//                                putExtra("isCaller", true)
//                                putExtra("avatar", dialog!!.dialogPhoto)
//                            })
//                        }
//                    }
//
//                    val newCallLog = CallLogEntity(
//                        id = Random.Default.nextLong(),
//                        callerName = dialog!!.dialogName,
//                        System.currentTimeMillis(),
//                        callDuration = 0,
//                        "Outgoing",
//                        "Not Answered",
//                        dialog!!.dialogPhoto,
//                        dialog!!.id,
//                        false,
//                        false
//                    )
//                    insertCallLog(newCallLog)
//                }
//            }
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    private fun insertCallLog(callLog: CallLogEntity) {
//        callViewModel.insertCallLog(callLog)
//    }
//
//    @OptIn(UnstableApi::class)
//    override fun onSupportNavigateUp(): Boolean {
//        val upIntent = Intent(this, MainActivity::class.java)
//        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        NavUtils.navigateUpTo(this, upIntent)
//        userStatusManager?.stop()
//        return true
//    }
//
//    private fun down(mUrl: String, fileLocation: String, message: Message) {
//        //STORAGE_FOLDER += fileLocation
//        Log.d("Download", "directory path - $fileLocation")
//
//        if (mUrl.startsWith("/storage/")) {
//
//            Log.d("Download", "Cannot download a local file")
//            return
//        }
//
//        //STORAGE_FOLDER += fileLocation
//        val STORAGE_FOLDER = "/Download/FlashApp/$fileLocation"
//        val fileName = mUrl.split("/").last()
//        val storageDirectory =
//            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"
//
//        Log.d("Download", "directory path - $storageDirectory")
//        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
//        if (!file.exists()) {
//            file.mkdirs()
//        }
//
//        GlobalScope.launch(Dispatchers.IO) {
//
//            try {
//                val url = URL(mUrl)
//                val connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "GET"
//                connection.setRequestProperty("Accept-Encoding", "identity")
//                connection.connect()
//
//                if (connection.responseCode in 200..299) {
//                    val fileSize = connection.contentLength
//                    val inputStream = connection.inputStream
//                    val outputStream = FileOutputStream(storageDirectory)
//
//                    var bytesCopied: Long = 0
//                    val buffer = ByteArray(1024)
//                    var bytes = inputStream.read(buffer)
//                    while (bytes >= 0) {
//                        bytesCopied += bytes
//                        val downloadProgress =
//                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
//                        runOnUiThread {
//                            //progressbar.visibility = View.VISIBLE
////                        progressbar.progress = downloadProgress
////                        progressCountTv.text = "$downloadProgress%"
//                        }
//                        outputStream.write(buffer, 0, bytes)
//                        bytes = inputStream.read(buffer)
//                    }
//                    // progressbar.visibility = View.GONE
//                    //progressCountTv.visibility = View.GONE
//                    runOnUiThread {
//                        // Update the UI components here
////                    progressbar.visibility = View.GONE
//
//                        coreChatSocketClient.sendDownLoadedEvent(myId,message.id)
//                        Log.d("Download", "File Downloaded : $storageDirectory")
//
//                        val downloadedFile = File(storageDirectory)
//
//                        when (getFileType(mUrl)) {
//                            FileType.IMAGE -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
////                                        val msg =
////                                            messageRepository.getMessageByMessageId(message.id)
////                                        if (msg != null) {
////                                            msg.imageUrl = downloadedFile.toString()
////                                            Log.d("Download", "Message to update : $msg")
////                                            messageRepository.updateMessage(msg)
////                                        }
//                                    }
//                                }
//
//
//                                message.setImage(Message.Image(storageDirectory))
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.AUDIO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.audioUrl = downloadedFile.toString()
//                                            msg.voiceUrl = downloadedFile.toString()
//                                            Log.d("Download", "Audio Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
//                                message.setAudio(
//                                    Message.Audio(
//                                        storageDirectory,
//                                        5050,
//                                        getFileNameFromUrl(storageDirectory)
//                                    )
//                                )
//
//                                super.messagesAdapter?.update(message)
//
//                            }
//
//                            FileType.VIDEO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
////                                        val msg =
////                                            messageRepository.getMessageByMessageId(message.id)
////                                        if (msg != null) {
////                                            msg.videoUrl = downloadedFile.toString()
////                                            Log.d("Download", "Message to update : $msg")
////                                            messageRepository.updateMessage(msg)
////                                        }
//                                    }
//                                }
//
//                                message.setVideo(Message.Video(storageDirectory))
//
//                                super.messagesAdapter?.update(message)
//
//                            }
//
//                            FileType.DOCUMENT -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
////                                        val msg =
////                                            messageRepository.getMessageByMessageId(message.id)
////                                        if (msg != null) {
////                                            msg.docUrl = downloadedFile.toString()
////                                            Log.d("Download", "Document Message to update : $msg")
////                                            messageRepository.updateMessage(msg)
////                                        }
//                                    }
//                                }
//
////                                message.setDocument(
////                                    Message.Document(
////                                        storageDirectory,
////                                        getFileNameFromUrl(storageDirectory),
////                                        formatFileSize(getFileSize(storageDirectory))
////                                    )
////                                )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.OTHER -> {
//                                // Handle other types, if needed
//                            }
//                        }
//
//                        val fileExtension = getFileExtension(mUrl)
//                        if (fileExtension == "jpg" || fileExtension == "png") {
//
//
//                        } else if (fileExtension == "mp4") {
//                            if (downloadedFile.exists()) {
//                                CoroutineScope(Dispatchers.IO).launch {
////                                    val msg = messageRepository.getMessageByMessageId(message.id)
////                                    if (msg != null) {
////                                        msg.videoUrl = downloadedFile.toString()
////                                        Log.d("Download", "Message to update : $msg")
////                                        messageRepository.updateMessage(msg)
////                                    }
//                                }
//                            }
//                        }
//                    }
//
//
////                val createdAt = getDateTimeStamp()
////                val uniqueId = System.currentTimeMillis().toString()
////                val saveDownloadPath = DownloadedFile(
////                    chatId,
////                    uniqueId,
////                    directoryPath,
////                    mUrl,
////                    createdAt
////                )
////
////                insertDownload(saveDownloadPath)
//                    //insertOfflinePath(saveLocalPath)
////                Log.i("Download", "saved download path: $saveDownloadPath")
//                    outputStream.close()
//                    inputStream.close()
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(
//                            this@MessagesActivity,
//                            "Not successful",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("Download Error", "Reason : ${e.message}")
//                e.printStackTrace()
//            }
//
//        }
//    }
//
//    private fun setProfilePicture(imageUri: String, imageView: ImageView) {
//        Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform())
//            .into(imageView)
//    }
//
//    private fun download(
//        mUrl: String,
//        progressbar: ProgressBar,
//        displayView: ImageView,
//        fileLocation: String, message: Message
//    ) {
//        //STORAGE_FOLDER += fileLocation
//        Log.d("Download", "directory path - $fileLocation")
//
//        if (mUrl.startsWith("/storage/") || mUrl.startsWith("/storage/")) {
//
//            Log.d("Download", "Cannot download a local file")
//            return
//        }
//
//        //STORAGE_FOLDER += fileLocation
//        val STORAGE_FOLDER = "/Download/Flash/$fileLocation"
//        val fileName = mUrl.split("/").last()
//        val storageDirectory =
//            Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER + "/$fileName"
//
//        Log.d("Download", "directory path - $storageDirectory")
//        val file = File(Environment.getExternalStorageDirectory().toString() + STORAGE_FOLDER)
//        if (!file.exists()) {
//            file.mkdirs()
//        }
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val url = URL(mUrl)
//            val connection = url.openConnection() as HttpURLConnection
//            connection.requestMethod = "GET"
//            connection.setRequestProperty("Accept-Encoding", "identity")
//            connection.connect()
//
//            try {
//                if (connection.responseCode in 200..299) {
//                    val fileSize = connection.contentLength
//                    val inputStream = connection.inputStream
//                    val outputStream = FileOutputStream(storageDirectory)
//
//                    var bytesCopied: Long = 0
//                    val buffer = ByteArray(1024)
//                    var bytes = inputStream.read(buffer)
//                    while (bytes >= 0) {
//                        bytesCopied += bytes
//                        val downloadProgress =
//                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
//                        runOnUiThread {
//                            //progressbar.visibility = View.VISIBLE
//                            progressbar.progress = downloadProgress
////                        progressCountTv.text = "$downloadProgress%"
//                        }
//                        outputStream.write(buffer, 0, bytes)
//                        bytes = inputStream.read(buffer)
//                    }
//                    // progressbar.visibility = View.GONE
//                    //progressCountTv.visibility = View.GONE
//                    runOnUiThread {
//                        // Update the UI components here
//                        progressbar.visibility = View.GONE
//
//                        coreChatSocketClient.sendDownLoadedEvent(myId,message.id)
//
//                        Log.d("Download", "File Downloaded : $storageDirectory")
//
//                        val downloadedFile = File(storageDirectory)
//
//                        when (getFileType(mUrl)) {
//                            FileType.IMAGE -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.imageUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            ViewImagesActivity::class.java
//                                        )
//                                        intent.putExtra("imageUrl", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//                                }
//
//
//
//
//                                message.setImage(Message.Image(storageDirectory))
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.AUDIO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.audioUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setAudio(
////                                Message.Audio(
////                                    storageDirectory,
////                                    getAudioDuration(storageDirectory),
////                                    getFileNameFromUrl(storageDirectory)
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.VIDEO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.videoUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            PlayVideoActivity::class.java
//                                        )
//                                        intent.putExtra("videoPath", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//
//                                    message.setVideo(Message.Video(storageDirectory))
//
//                                    super.messagesAdapter?.update(message)
//                                }
//                            }
//
//                            FileType.DOCUMENT -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.docUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setDocument(
////                                Message.Document(
////                                    storageDirectory,
////                                    getFileNameFromUrl(storageDirectory),
////                                    formatFileSize(getFileSize(storageDirectory))
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.OTHER -> {
//                                // Handle other types, if needed
//                            }
//                        }
//
//
////                    progressCountTv.visibility = View.GONE
//                        //videoHolder.progressCountTv.visibility = View.VISIBLE
//                        val fileExtension = getFileExtension(mUrl)
//                        if (fileExtension == "jpg" || fileExtension == "png" || fileExtension == "gif" || fileExtension == "jpeg") {
//
//
////                        displayView.setOnClickListener {
////                            val intent = Intent(
////                                this@StyledMessagesActivity,
////                                ViewImagesActivity::class.java
////                            )
////                            intent.putExtra("imageUrl", mUrl)
////                            startActivity(intent)
////                        }
//                        } else if (fileExtension == "mp4") {
//
//
//                            if (downloadedFile.exists()) {
//                                CoroutineScope(Dispatchers.IO).launch {
////                                val msg = messageRepository.getMessageByMessageId(message.id)
////                                if (msg != null) {
////                                    msg.videoUrl = downloadedFile.toString()
////                                    Log.d("Download", "Message to update : $msg")
////                                    messageRepository.updateMessage(msg)
////                                }
//                                }
//
//                            }
//                            displayView.setOnClickListener {
////                            val intent = Intent(
////                                this@ChatActivity,
////                                PlayVideoActivity::class.java
////                            )
////                            intent.putExtra("videoPath", mUrl)
////                            startActivity(intent)
//                            }
//                        }
//                    }
//
//
////                val createdAt = getDateTimeStamp()
////                val uniqueId = System.currentTimeMillis().toString()
////                val saveDownloadPath = DownloadedFile(
////                    chatId,
////                    uniqueId,
////                    directoryPath,
////                    mUrl,
////                    createdAt
////                )
////
////                insertDownload(saveDownloadPath)
//                    //insertOfflinePath(saveLocalPath)
////                Log.i("Download", "saved download path: $saveDownloadPath")
//                    outputStream.close()
//                    inputStream.close()
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(
//                            this@MessagesActivity,
//                            "Not successful",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("DownloadFailed", e.message.toString())
//
//                e.printStackTrace()
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    val msg = messageViewModel.getMessage(message.id)
//                    if (msg != null) {
//                        msg.imageUrl = storageDirectory.toString()
//                        Log.d("Download", "Message to update : $msg")
//                        messageViewModel.updateMessage(msg)
//                    }
//                }
//
//
//
//                runOnUiThread {
//                    progressbar.visibility = View.GONE
//
//                    when (getFileType(mUrl)) {
//                        FileType.IMAGE -> {
//                            message.setImage(Message.Image(storageDirectory))
//
//                            super.messagesAdapter?.update(message)
//
//                        }
//
//                        FileType.VIDEO -> {
//                            message.setVideo(Message.Video(storageDirectory))
//
//                            super.messagesAdapter?.update(message)
//                        }
//
//                        else -> {}
//                    }
//
//                }
//            }
//
//
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun downld(
//        mUrl: String,
//        progressbar: ProgressBar,
//        displayView: ImageView,
//        fileLocation: String, message: Message
//    ) {
//        val fileName = mUrl.split("/").last()
//        val flashDir = "Flash"
//        val storageDirectory = File(
//            getExternalFilesDir(flashDir),
//            "Media/$fileLocation/$fileName"
//        )
//
//
//        // Ensure that the directories leading up to the file exist
//        storageDirectory.parentFile?.mkdirs()
//
//        Log.d("Download", "directory path - $storageDirectory")
//
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val url = URL(mUrl)
//                val connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "GET"
//                connection.setRequestProperty("Accept-Encoding", "identity")
//                connection.connect()
//
//                if (connection.responseCode in 200..299) {
//                    val fileSize = connection.contentLength
//                    val inputStream = connection.inputStream
//                    val outputStream = FileOutputStream(storageDirectory)
//
//                    var bytesCopied: Long = 0
//                    val buffer = ByteArray(1024)
//                    var bytes = inputStream.read(buffer)
//                    while (bytes >= 0) {
//                        bytesCopied += bytes
//                        val downloadProgress =
//                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
//                        runOnUiThread {
//                            progressbar.progress = downloadProgress
//                        }
//                        outputStream.write(buffer, 0, bytes)
//                        bytes = inputStream.read(buffer)
//                    }
//
//                    runOnUiThread {
//                        progressbar.visibility = View.GONE
//                        Log.d("Download", "File Downloaded: ${storageDirectory.absolutePath}")
//
//                        coreChatSocketClient.sendDownLoadedEvent(myId,message.id)
//
//                        Log.d("Download", "File Downloaded : $storageDirectory")
//
//                        val downloadedFile = storageDirectory
//
//                        when (getFileType(mUrl)) {
//                            FileType.IMAGE -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.imageUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            ViewImagesActivity::class.java
//                                        )
//                                        intent.putExtra("imageUrl", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//                                }
//
//
//
//
//                                message.setImage(Message.Image(storageDirectory.absolutePath))
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.AUDIO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.audioUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setAudio(
////                                Message.Audio(
////                                    storageDirectory,
////                                    getAudioDuration(storageDirectory),
////                                    getFileNameFromUrl(storageDirectory)
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.VIDEO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.videoUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            PlayVideoActivity::class.java
//                                        )
//                                        intent.putExtra("videoPath", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//
//                                    message.setVideo(Message.Video(storageDirectory.absolutePath))
//
//                                    super.messagesAdapter?.update(message)
//                                }
//                            }
//
//                            FileType.DOCUMENT -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.docUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setDocument(
////                                Message.Document(
////                                    storageDirectory,
////                                    getFileNameFromUrl(storageDirectory),
////                                    formatFileSize(getFileSize(storageDirectory))
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.OTHER -> {
//                                // Handle other types, if needed
//                            }
//                        }
//
//
//                        // Notify the system about the new file using MediaStore API
//                        val contentResolver = applicationContext.contentResolver
//                        val contentValues = ContentValues().apply {
//                            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
//                            put(
//                                MediaStore.Downloads.MIME_TYPE,
//                                "application/octet-stream"
//                            ) // Change to actual MIME type
//                            put(MediaStore.Downloads.IS_PENDING, 1) // Mark the file as pending
//                        }
//
//
//                        val uri = contentResolver.insert(
//                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//                            contentValues
//                        )
//
//                        uri?.let {
//                            // Open an output stream using the obtained URI
//                            val output = contentResolver.openOutputStream(uri)
//                            output?.use { outputStream ->
//                                // Copy the file to the output stream
//                                storageDirectory.inputStream().use { inputStream ->
//                                    inputStream.copyTo(outputStream)
//                                }
//                            }
//
//                            // Mark the file as complete
//                            contentValues.clear()
//                            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
//                            contentResolver.update(uri, contentValues, null, null)
//                        }
//
//                        // Update the UI components and handle different file types here
//
//                        // Example for updating the image URL in the message:
//                        // message.setImage(Message.Image(storageDirectory.toString()))
//                        // super.messagesAdapter?.update(message)
//                    }
//
//                    outputStream.close()
//                    inputStream.close()
//
//                    // Notify the system about the new file
//                    MediaScannerConnection.scanFile(
//                        applicationContext,
//                        arrayOf(storageDirectory.absolutePath),
//                        null
//                    ) { path, uri ->
//                        Log.d("MediaScanner", "Scanned $path:")
//                        Log.d("MediaScanner", "-> uri=$uri")
//                    }
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(
//                            this@MessagesActivity,
//                            "Not successful",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("DownloadFailed", e.message.toString())
//                e.printStackTrace()
//
//                runOnUiThread {
//                    progressbar.visibility = View.GONE
//
//                    // Handle failure scenarios and update UI accordingly
//                }
//            }
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun downlod(
//        mUrl: String,
//        progressbar: ProgressBar,
//        displayView: ImageView,
//        fileLocation: String, message: Message
//    ) {
//        val fileName = mUrl.split("/").last()
//        val flashDir = "Flash"
//        val storageDirectory = File(
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//            fileName
//        )
//
//        // Ensure that the directories leading up to the file exist
//        storageDirectory.parentFile?.mkdirs()
//
//
//        Log.d("Download", "directory path - $storageDirectory")
//
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val url = URL(mUrl)
//                val connection = url.openConnection() as HttpURLConnection
//                connection.requestMethod = "GET"
//                connection.setRequestProperty("Accept-Encoding", "identity")
//                connection.connect()
//
//                if (connection.responseCode in 200..299) {
//                    val fileSize = connection.contentLength
//                    val inputStream = connection.inputStream
//                    val outputStream = FileOutputStream(storageDirectory)
//
//                    var bytesCopied: Long = 0
//                    val buffer = ByteArray(1024)
//                    var bytes = inputStream.read(buffer)
//                    while (bytes >= 0) {
//                        bytesCopied += bytes
//                        val downloadProgress =
//                            (bytesCopied.toFloat() / fileSize.toFloat() * 100).toInt()
//                        runOnUiThread {
//                            progressbar.progress = downloadProgress
//                        }
//                        outputStream.write(buffer, 0, bytes)
//                        bytes = inputStream.read(buffer)
//                    }
//
//                    runOnUiThread {
//                        progressbar.visibility = View.GONE
//                        Log.d("Download", "File Downloaded: ${storageDirectory.absolutePath}")
//
//                        // ... (rest of your code remains unchanged)
//
//                        // Notify the system about the new file using MediaStore API
//                        val contentResolv = applicationContext.contentResolver
//                        val contentValu = ContentValues().apply {
//                            put(MediaStore.MediaColumns.DATA, storageDirectory.absolutePath)
//                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
//                            put(
//                                MediaStore.MediaColumns.MIME_TYPE,
//                                "application/octet-stream"
//                            ) // Change to actual MIME type
//                            put(MediaStore.Downloads.IS_PENDING, 1) // Mark the file as pending
//                            put(MediaStore.MediaColumns.SIZE, storageDirectory.length())
//                        }
//
//                        // Notify the system about the new file using MediaStore API
//                        val contentResolver = applicationContext.contentResolver
//                        val contentValues = ContentValues().apply {
//                            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
//                            put(
//                                MediaStore.Downloads.MIME_TYPE,
//                                "application/octet-stream"
//                            ) // Change to actual MIME type
//                            put(MediaStore.Downloads.IS_PENDING, 1) // Mark the file as pending
//                        }
//
//
//                        val uri = contentResolver.insert(
//                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//                            contentValues
//                        )
//
//                        uri?.let {
//                            // Open an output stream using the obtained URI
//                            val output = contentResolver.openOutputStream(uri)
//                            output?.use { outputStream ->
//                                // Copy the file to the output stream
//                                storageDirectory.inputStream().use { inputStream ->
//                                    inputStream.copyTo(outputStream)
//                                }
//                            }
//
//                            // Mark the file as complete
//                            contentValues.clear()
//                            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
//                            contentResolver.update(uri, contentValues, null, null)
//                        }
//
//                        Log.d("Download", "Uri : $uri")
//
//
//                        val downloadedFile = storageDirectory
//
//                        when (getFileType(mUrl)) {
//                            FileType.IMAGE -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.imageUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            ViewImagesActivity::class.java
//                                        )
//                                        intent.putExtra("imageUrl", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//                                }
//
//
//
//
//                                message.setImage(Message.Image(storageDirectory.absolutePath))
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.AUDIO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.audioUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setAudio(
////                                Message.Audio(
////                                    storageDirectory,
////                                    getAudioDuration(storageDirectory),
////                                    getFileNameFromUrl(storageDirectory)
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.VIDEO -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.videoUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//
//                                    displayView.setOnClickListener {
//                                        val intent = Intent(
//                                            this@MessagesActivity,
//                                            PlayVideoActivity::class.java
//                                        )
//                                        intent.putExtra("videoPath", url)
//                                        intent.putExtra("owner", message.user.name)
//                                        startActivity(intent)
//                                    }
//
//                                    message.setVideo(Message.Video(storageDirectory.absolutePath))
//
//                                    super.messagesAdapter?.update(message)
//                                }
//                            }
//
//                            FileType.DOCUMENT -> {
//                                if (downloadedFile.exists()) {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        val msg = messageViewModel.getMessage(message.id)
//                                        if (msg != null) {
//                                            msg.docUrl = downloadedFile.toString()
//                                            Log.d("Download", "Message to update : $msg")
//                                            messageViewModel.updateMessage(msg)
//                                        }
//                                    }
//                                }
//
////                            message.setDocument(
////                                Message.Document(
////                                    storageDirectory,
////                                    getFileNameFromUrl(storageDirectory),
////                                    formatFileSize(getFileSize(storageDirectory))
////                                )
////                            )
//
//                                super.messagesAdapter?.update(message)
//                            }
//
//                            FileType.OTHER -> {
//                                // Handle other types, if needed
//                            }
//                        }
//
//                        // Update the UI components and handle different file types here
//                        // Example for updating the image URL in the message:
//                        // message.setImage(Message.Image(storageDirectory.toString()))
//                        // super.messagesAdapter?.update(message)
//                    }
//
//                    outputStream.close()
//                    inputStream.close()
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(
//                            this@MessagesActivity,
//                            "Not successful",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e("DownloadFailed", e.message.toString())
//                e.printStackTrace()
//
//                runOnUiThread {
//                    progressbar.visibility = View.GONE
//
//                    // Handle failure scenarios and update UI accordingly
//                }
//            }
//        }
//    }
//
//
//    private fun getFileType(url: String): FileType {
//
//        return when (url.substringAfterLast(".").toLowerCase()) {
//            "jpg", "jpeg", "png", "gif" -> FileType.IMAGE
//            "mp3", "wav", "ogg", "m4a" -> FileType.AUDIO
//            "mp4", "avi", "mkv" -> FileType.VIDEO
//            "pdf", "doc", "docx", "txt" -> FileType.DOCUMENT
//            else -> FileType.OTHER
//        }
//    }
//
//    private fun getFileExtension(url: String): String {
//        val parts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        val fileName = parts[parts.size - 1]
//        val fileNameParts =
//            fileName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        return if (fileNameParts.isNotEmpty()) fileNameParts[fileNameParts.size - 1] else ""
//    }
//
//
////    private fun external(){
////        getExternalFilesDir()
////    }
//
//    private fun togglePlayPause(
//        url: String,
//        seekBar: SeekBar?,
//        duration: TextView,
//        message: Message,
//        playPause: ImageView
//    ) {
//
//
//        if (url.startsWith("file:/") || url.startsWith("/storage/")) {
//            if (mediaPlayer == null) {
//                mediaPlayer = MediaPlayer()
//                try {
//                    mediaPlayer?.setDataSource(url)
//                    currentAudio = url
//                    Log.i("Current Audio", "CA = url = $currentAudio = $url")
//                    mediaPlayer?.prepareAsync()
//                    mediaPlayer?.setOnPreparedListener { mp ->
//                        if (seekBar != null) {
//                            initializeSeekBar(seekBar, duration)
//                        }
//                        if (isPlaying) {
//                            playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_play_black)
//                            mp.pause() // Pause playback if isPlaying is true
//                        } else {
//                            playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_white_24)
//                            mp.start() // Start or resume playback if isPlaying is false
//                        }
//                        isPlaying = !isPlaying // Toggle the playback state
//                    }
//                } catch (e: Exception) {
//                    Log.d("PlayAudio", "Error: " + e.message)
//                    e.printStackTrace()
//                    // Handle the exception appropriately (e.g., show an error message to the user)
//                }
//            } else {
//                // Release the previous MediaPlayer before creating a new one
//                if (currentAudio != url) {
//                    Log.i("Current Audio", "CA != url = $currentAudio != $url")
//
//                    mediaPlayer?.release()
//                    mediaPlayer = null
//
//                    mediaPlayer = MediaPlayer()
//                    try {
//                        mediaPlayer?.setDataSource(url)
//                        currentAudio = url
//                        mediaPlayer?.prepareAsync()
//                        mediaPlayer?.setOnPreparedListener { mp ->
//                            if (seekBar != null) {
//                                initializeSeekBar(seekBar, duration)
//                            }
//                            if (isPlaying) {
//                                playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_play_black)
//                                mp.pause() // Pause playback if isPlaying is true
//                            } else {
//                                playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_white_24)
//                                mp.start() // Start or resume playback if isPlaying is false
//                            }
//                            isPlaying = !isPlaying // Toggle the playback state
//                        }
//                    } catch (e: Exception) {
//                        Log.d("PlayAudio", "Error: " + e.message)
//                        e.printStackTrace()
//                        // Handle the exception appropriately (e.g., show an error message to the user)
//                    }
//                } else {
//                    if (isPlaying) {
//                        playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_play_black)
//                        mediaPlayer?.pause() // Pause playback if isPlaying is true
//                    } else {
//                        playPause.setImageResource(com.uyscuti.social.circuit.R.drawable.baseline_pause_white_24)
//                        mediaPlayer?.start() // Start or resume playback if isPlaying is false
//                    }
//                    isPlaying = !isPlaying
//                }
//
//            }
//        } else {
//            down(url, "Audio", message)
//        }
//    }
//
//    private fun initializeSeekBar(seekBar: SeekBar, audioDuration: TextView) {
//        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) mediaPlayer?.seekTo(progress)
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//        })
//
//        val duration = mediaPlayer?.duration ?: 0
//        seekBar.max = duration
//        runnable = Runnable {
//            val currentPosition = mediaPlayer?.currentPosition ?: 0
//            seekBar.progress = currentPosition
//
//            val playedTimeSeconds = currentPosition / 1000
//            val durationSeconds = duration / 1000
//
//            val playedMinutes = playedTimeSeconds / 60
//            val playedSeconds = playedTimeSeconds % 60
//
//            val dueTimeSeconds = (durationSeconds - playedTimeSeconds).coerceAtLeast(0)
//            val dueMinutes = dueTimeSeconds / 60
//            val dueSeconds = dueTimeSeconds % 60
////
//            audioDuration.text = String.format("%02d:%02d", playedMinutes, playedSeconds)
////            endDurationTv.text = String.format("%02d:%02d", dueMinutes, dueSeconds)
//
//            handler.postDelayed(runnable, 1000)
//        }
//        handler.postDelayed(runnable, 1000)
//    }
//
//    private fun showSaveConfirmationDialog(vnPath: String) {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Send Audio Recording?")
//
//        builder.setPositiveButton("Send") { _, _ ->
//            val userEntity = UserEntity(
//                "0",
//                "You",
//                "local",
//                Date(),
//                true
//            )
//
//            val user = User("0", "You", "test", true, Date())
//            val date = Date(System.currentTimeMillis())
//            val messageId = "Audio_${Random.Default.nextInt()}"
//            val message = Message(
//                messageId,
//                user,
//                null,
//                date
//            )
//
////            val newFilePath = getOutputFilePath(this)
//            val newFile = File(vnPath)
////            Log.d("AudioFile", "File created: $newFile")
//            Log.d("AudioFile", "vn file path: $vnPath")
//
//            try {
//                Log.d("AudioFile", "File created: $newFile")
//
//                // Now you can use 'newFile' as needed, for example, setting it in your message.
//                val audioUrl = Uri.fromFile(newFile)
//
//                message.setAudio(
//                    Message.Audio(
//                        audioUrl.toString(),
//                        10000,
//                        getNameFromUrl(audioUrl.toString())
//                    )
//                )
//
//                val imageMessage = MessageEntity(
//                    id = messageId,
//                    chatId = chatId,
//                    userName = "This",
//                    user = userEntity,
//                    userId = myId,
//                    text = "🎵 Audio",
//                    createdAt = System.currentTimeMillis(),
//                    imageUrl = null,
//                    voiceUrl = null,
//                    voiceDuration = 0,
//                    status = "Sending",
//                    videoUrl = null,
//                    audioUrl = audioUrl.toString(),
//                    docUrl = null,
//                    fileSize = getFileSize(audioUrl.toString())
//                )
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    insertMessage(imageMessage)
//                    updateLastMessage(isGroup, chatId, imageMessage)
//                }
//
//            } catch (e: IOException) {
//                Log.e("AudioFile", "Error creating file", e)
//            }
//
//            message.setUser(user)
//            message.status = "Sending"
//
//            CoroutineScope(Dispatchers.Main).launch {
////                            delay(500)
//                super.messagesAdapter?.addToStart(message, true)
//            }
//        }
//
//        builder.setNegativeButton("Delete") { dialog, which ->
//            // Handle the case when the user chooses not to save the recording
//            // You may want to delete the recording or take other actions here
//            val audioFile = File(vnPath)
//            if (audioFile.exists() && audioFile.delete()) {
//                // File was successfully deleted
//                Log.i("VoiceNote", "Recording discarded and deleted.")
//            } else {
//                // Error occurred while deleting the file
//                Log.e("VoiceNote", "Failed to delete recording.")
//            }
//        }
//
//        builder.create().show()
//    }
//
//    private fun getOutputFilePath(context: Context): String {
//        val STORAGE_FOLDER = "FlashMedia/FlashVN"
//        val uniqueId = System.currentTimeMillis().toString()
//        val fileName = "FlashNote$uniqueId.mp3"
//
//        val storageDirectory = File(context.getExternalFilesDir(null), STORAGE_FOLDER)
//
//        if (!storageDirectory.exists()) {
//            storageDirectory.mkdirs()
//        }
//
//        return File(storageDirectory, fileName).absolutePath
//    }
//
//
//
//    private fun stopRecording() {
//        try {
//            audioRecorder?.apply {
//                stop()
//                release()
//            }
//            audioRecorder = null
//            // Add any UI changes or notifications indicating recording has stopped
//            showSaveConfirmationDialog(outputFile)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Handle exceptions as needed
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onAddVoiceNote() {
//        isVnRecording = if (!isVnRecording) {
//            startRecording()
//            true
//        } else {
//            stopRecording()
//            false
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        mediaPlayer?.release()
//        mediaPlayer = null
//        userStatusManager?.stop()
//        coreChatSocketClient.chatListener = null
//    }
//
//    override fun onDialogUpdated(newDialogId: String) {
//        Log.d(TAG,"onDialogUpdated : $newDialogId")
//        this.chatId = newDialogId
//        trigger()
//    }
//
//
//    private fun sendSeenReport(chatId: String, senderId: String) {
//        try {
//            // Make sure socket client is initialized and connected
//            coreChatSocketClient?.sendMessageOpenedReport(chatId, senderId)
//        } catch (e: Exception) {
//            Log.e("SendSeenReport", "Error sending seen report: ${e.message}")
//            e.printStackTrace()
//        }
//    }
//
//
//}