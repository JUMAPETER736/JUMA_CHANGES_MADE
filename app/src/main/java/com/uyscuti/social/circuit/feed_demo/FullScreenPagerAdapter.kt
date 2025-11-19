package com.uyscuti.social.circuit.feed_demo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uyscuti.social.circuit.R
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.Player
import kotlin.math.max

class FullScreenPagerAdapter(
    private val mediaUrls: List<String>,
    private val context: Context,
    private val callback: () -> Unit
) : RecyclerView.Adapter<FullScreenPagerAdapter.ViewHolder>() {

    private val players = mutableMapOf<Int, ExoPlayer>()
    private val progressHandlers = mutableMapOf<Int, Handler>()
    private val progressRunnables = mutableMapOf<Int, Runnable>()
    private var currentPosition = 0

    var onProgressUpdate: ((position: Int, progress: Int, timeText: String) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val playerView: PlayerView = itemView.findViewById(R.id.playerView)
        val audioContainer: View = itemView.findViewById(R.id.audioContainer)
        val audioPlayerView: PlayerView = itemView.findViewById(R.id.audioPlayerView)
        val documentContainer: View = itemView.findViewById(R.id.documentContainer)
        val documentIcon: ImageView = itemView.findViewById(R.id.documentIcon)
        val documentName: TextView = itemView.findViewById(R.id.documentName)
        val mediaControlsContainer: LinearLayout = itemView.findViewById(R.id.mediaControlsContainer)
     //   val audioIcon: ImageView = itemView.findViewById(R.id.audioIcon)
        var container: RelativeLayout = itemView.findViewById(R.id.container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_pager, parent, false)
        return ViewHolder(view)
    }

    @OptIn(UnstableApi::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaUrl = mediaUrls[position]

        hideAllViews(holder)

        holder.container.setOnClickListener {
            callback.invoke()
        }

        when (getMediaType(mediaUrl)) {
            MediaType.IMAGE -> handleImageMedia(holder, mediaUrl)
            MediaType.VIDEO -> handleVideoMedia(holder, mediaUrl, position)
            MediaType.AUDIO -> handleAudioMedia(holder, mediaUrl, position)
            MediaType.DOCUMENT -> handleDocumentMedia(holder, mediaUrl)
            MediaType.UNKNOWN -> handleUnknownMedia(holder, mediaUrl)
        }

        // Auto-play for video/audio if this is the current position
        if (position == currentPosition && getMediaType(mediaUrl) in listOf(MediaType.VIDEO, MediaType.AUDIO)) {
            players[position]?.playWhenReady = true
        }
    }

    @OptIn(UnstableApi::class)
    private fun handleVideoMedia(holder: ViewHolder, mediaUrl: String, position: Int) {
        holder.playerView.visibility = View.VISIBLE
        holder.mediaControlsContainer.visibility = View.VISIBLE

        if (!players.containsKey(position)) {
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = MediaItem.fromUri(mediaUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = position == currentPosition // Auto-play if current
            player.repeatMode = Player.REPEAT_MODE_ONE
            players[position] = player

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            startProgressUpdates(position)
                        }
                        Player.STATE_ENDED -> {
                            if (position == currentPosition) {
                                player.seekTo(0)
                                player.playWhenReady = true // Auto-play after ending
                            }
                        }
                    }
                }
            })
        }

        val player = players[position]
        holder.playerView.player = player
        holder.playerView.useController = false
        holder.playerView.setShowFastForwardButton(false)
        holder.playerView.setShowRewindButton(false)
        holder.playerView.setShowNextButton(false)
        holder.playerView.setShowPreviousButton(false)
    }

    @OptIn(UnstableApi::class)
    private fun handleAudioMedia(holder: ViewHolder, mediaUrl: String, position: Int) {
        holder.audioContainer.visibility = View.VISIBLE
        holder.mediaControlsContainer.visibility = View.VISIBLE
        holder.audioPlayerView.visibility = View.VISIBLE

//        holder.audioIcon?.let { audioIcon ->
//            audioIcon.setImageResource(FullScreenAudioIcon(mediaUrl))
//            audioIcon.visibility = View.GONE
//            // Center audio icon
//            (audioIcon.layoutParams as RelativeLayout.LayoutParams).apply {
//                addRule(RelativeLayout.CENTER_IN_PARENT)
//            }
//        }

        if (!players.containsKey(position)) {
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = MediaItem.fromUri(mediaUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = position == currentPosition // Auto-play if current
            player.repeatMode = Player.REPEAT_MODE_ONE
            players[position] = player

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            startProgressUpdates(position)
                        }
                        Player.STATE_ENDED -> {
                            if (position == currentPosition) {
                                player.seekTo(0)
                                player.playWhenReady = true // Auto-play after ending
                            }
                        }
                    }
                }
            })
        }

        val player = players[position]
        holder.audioPlayerView.player = player
        holder.audioPlayerView.useController = false
        holder.audioPlayerView.setShowBuffering(androidx.media3.ui.PlayerView.SHOW_BUFFERING_NEVER)
    }

    private fun updateProgressUI(position: Int, player: ExoPlayer) {
        val currentPos = player.currentPosition
        val duration = player.duration

        if (duration > 0) {
            val progress = ((currentPos.toFloat() / duration.toFloat()) * 100).toInt()
            val remainingTime = duration - currentPos
            val timeText = formatTime(remainingTime)
            onProgressUpdate?.invoke(position, progress, timeText)
        }
    }

    private fun startProgressUpdates(position: Int) {
        stopProgressUpdates(position)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val player = players[position]
                if (player != null && player.duration > 0 && position == currentPosition) {
                    updateProgressUI(position, player)
                    handler.handleMessage(android.os.Message.obtain(handler, 0, this))
                    handler.postDelayed(this, 500)
                }
            }
        }

        progressHandlers[position] = handler
        progressRunnables[position] = runnable
        handler.post(runnable)
    }

    private fun stopProgressUpdates(position: Int) {
        progressHandlers[position]?.removeCallbacksAndMessages(null)
        progressHandlers.remove(position)
        progressRunnables.remove(position)
    }

    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("-%02d:%02d", minutes, seconds)
    }

    fun seekTo(positionMs: Long) {
        val player = players[currentPosition]
        player?.seekTo(positionMs)
    }

    fun isPlaying(): Boolean {
        val player = players[currentPosition]
        return player?.isPlaying ?: false
    }

    fun setCurrentPosition(position: Int) {
        if (position != currentPosition) {
            // Pause previous player
            players[currentPosition]?.playWhenReady = false
            stopProgressUpdates(currentPosition)
            // Update current position and start new player
            currentPosition = position
            players[currentPosition]?.playWhenReady = true
            startProgressUpdates(currentPosition)
        }
    }

    fun releasePlayer() {
        progressHandlers.keys.toList().forEach { position ->
            stopProgressUpdates(position)
        }

        players.values.forEach { player ->
            player.stop()
            player.release()
        }
        players.clear()
    }

    override fun getItemCount(): Int = mediaUrls.size

    private fun hideAllViews(holder: ViewHolder) {
        holder.imageView.visibility = View.GONE
        holder.playerView.visibility = View.GONE
        holder.audioContainer.visibility = View.GONE
        holder.documentContainer.visibility = View.GONE
        holder.mediaControlsContainer.visibility = View.GONE
    }

    private fun handleImageMedia(holder: ViewHolder, mediaUrl: String) {
        holder.imageView.visibility = View.VISIBLE

        Glide.with(context)
            .load(mediaUrl)
            .fitCenter()
            .placeholder(R.drawable.imageplaceholder)
            .error(R.drawable.imageplaceholder)
            .into(holder.imageView)
    }

    private fun handleDocumentMedia(holder: ViewHolder, mediaUrl: String) {
        holder.documentContainer.visibility = View.VISIBLE

        val fileName = getFileNameFromUrl(mediaUrl)
        val fileExtension = getFileExtension(mediaUrl)

        holder.documentIcon.setImageBitmap(generateDocumentThumbnail(createLocalCopyOfDocument(Uri.parse(mediaUrl), fileName), determineDocumentType(getMimeType(mediaUrl), fileName)))
        holder.documentName.text = fileName

        holder.documentContainer.setOnClickListener {
            openDocument(mediaUrl, fileName)
        }
    }

    private fun handleUnknownMedia(holder: ViewHolder, mediaUrl: String) {
        holder.documentContainer.visibility = View.VISIBLE

        val fileName = getFileNameFromUrl(mediaUrl)
        holder.documentIcon.setImageResource(R.drawable.text_placeholder)
        holder.documentName.text = fileName

        holder.documentContainer.setOnClickListener {
            openDocument(mediaUrl, fileName)
        }
    }

    enum class MediaType {
        IMAGE, VIDEO, AUDIO, DOCUMENT, UNKNOWN
    }

    private fun getMediaType(url: String): MediaType {
        val extension = getFileExtension(url).lowercase()
        return when (extension) {
            in imageExtensions -> MediaType.IMAGE
            in videoExtensions -> MediaType.VIDEO
            in audioExtensions -> MediaType.AUDIO
            in documentExtensions -> MediaType.DOCUMENT
            else -> MediaType.UNKNOWN
        }
    }

    private fun getFileExtension(url: String): String {
        return url.substringAfterLast('.', "")
    }

    private fun getFileNameFromUrl(url: String): String {
        return url.substringAfterLast('/').substringBefore('?')
    }

    private fun openDocument(url: String, fileName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse(url)
            val mimeType = getMimeType(url)
            intent.setDataAndType(uri, mimeType)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getMimeType(url: String): String {
        val extension = getFileExtension(url).lowercase()
        return when (extension) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "wma" -> "audio/x-ms-wma"
            "m4a" -> "audio/m4a"
            "opus" -> "audio/opus"
            else -> "*/*"
        }
    }

    companion object {
        private val imageExtensions = listOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "tiff", "ico"
        )
        private val videoExtensions = listOf(
            "mp4", "mkv", "webm", "avi", "mov", "wmv", "flv", "3gp", "m4v"
        )
        private val audioExtensions = listOf(
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "3gp", "amr"
        )
        private val documentExtensions = listOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "rtf", "odt", "ods", "odp", "zip", "rar", "7z"
        )
    }

    private fun createLocalCopyOfDocument(uri: Uri, fileName: String): java.io.File? {
        return try {
            val documentsDir = java.io.File(context.filesDir, "temp_documents")
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }

            val localFile = java.io.File(documentsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                localFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            localFile
        } catch (e: Exception) {
            null
        }
    }

    private fun determineDocumentType(mimeType: String, fileName: String): String {
        return when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("msword") || mimeType.contains("wordprocessingml") -> "word"
            mimeType.contains("excel") || mimeType.contains("spreadsheetml") -> "excel"
            mimeType.contains("powerpoint") || mimeType.contains("presentationml") -> "powerpoint"
            mimeType.contains("text/plain") -> "text"
            mimeType.contains("text/csv") -> "csv"
            mimeType.contains("application/rtf") -> "rtf"
            mimeType.contains("zip") -> "zip"
            mimeType.contains("rar") -> "rar"
            fileName.lowercase().endsWith(".pdf") -> "pdf"
            fileName.lowercase().endsWith(".doc") || fileName.lowercase().endsWith(".docx") -> "word"
            fileName.lowercase().endsWith(".xls") || fileName.lowercase().endsWith(".xlsx") -> "excel"
            fileName.lowercase().endsWith(".ppt") || fileName.lowercase().endsWith(".pptx") -> "powerpoint"
            fileName.lowercase().endsWith(".txt") -> "text"
            fileName.lowercase().endsWith(".csv") -> "csv"
            fileName.lowercase().endsWith(".rtf") -> "rtf"
            fileName.lowercase().endsWith(".zip") -> "zip"
            fileName.lowercase().endsWith(".rar") -> "rar"
            else -> "document"
        }
    }

    private fun generateDocumentThumbnail(file: java.io.File?, documentType: String): android.graphics.Bitmap? {
        if (file == null || !file.exists()) return null

        return when (documentType) {
            "pdf" -> generatePdfThumbnail(file)
            "word", "excel", "powerpoint" -> generateOfficeThumbnail(file, documentType)
            "text", "csv" -> generateTextThumbnail(file)
            else -> generateGenericThumbnail(documentType)
        }
    }

    private fun generatePdfThumbnail(file: java.io.File): android.graphics.Bitmap? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val fileDescriptor = android.os.ParcelFileDescriptor.open(file, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = android.graphics.pdf.PdfRenderer(fileDescriptor)

            if (pdfRenderer.pageCount > 0) {
                val page = pdfRenderer.openPage(0)
                val bitmap = android.graphics.Bitmap.createBitmap(page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                pdfRenderer.close()
                fileDescriptor.close()
                bitmap
            } else {
                generateGenericThumbnail("pdf")
            }
        } else {
            generateGenericThumbnail("pdf")
        }
    }

    private fun generateOfficeThumbnail(file: java.io.File, documentType: String): android.graphics.Bitmap? {
        return generateGenericThumbnail(documentType)
    }

    private fun generateTextThumbnail(file: java.io.File): android.graphics.Bitmap? {
        return try {
            val text = file.readText().take(200)
            createTextBitmap(text, "text")
        } catch (e: Exception) {
            generateGenericThumbnail("text")
        }
    }

    private fun generateGenericThumbnail(documentType: String): android.graphics.Bitmap? {
        val size = 200
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 24f
        }

        canvas.drawText(documentType.uppercase(), size / 2f, size / 2f, paint)
        return bitmap
    }

    private fun createTextBitmap(text: String, type: String): android.graphics.Bitmap? {
        val width = 200
        val height = 200
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }

        val lines = text.split("\n")
        var yPosition = 20f

        for (line in lines.take(10)) {
            if (yPosition > height - 20) break
            canvas.drawText(line.take(20), 10f, yPosition, paint)
            yPosition += 15f
        }

        return bitmap
    }

    private fun FullScreenAudioIcon(url: String): Int {
        val extension = getFileExtension(url).lowercase()
        return when (extension) {
            "mp3" -> R.drawable.ic_audio_white_icon
            "wav" -> R.drawable.ic_audio_white_icon
            "flac" -> R.drawable.ic_audio_white_icon
            "aac" -> R.drawable.ic_audio_white_icon
            "ogg" -> R.drawable.ic_audio_white_icon
            "wma" -> R.drawable.ic_audio_white_icon
            "m4a" -> R.drawable.ic_audio_white_icon
            "opus" -> R.drawable.ic_audio_white_icon
            else -> R.drawable.ic_audio_white_icon
        }
    }


}