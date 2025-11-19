package com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uyscuti.social.circuit.R
import com.uyscuti.social.circuit.databinding.ActivityFeedAudioBinding
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.adapter.FeedAudioAdapter
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.adapter.SelectedFilesCount
import com.uyscuti.social.circuit.User_Interface.uploads.feed_uploads.models.AudioDataClass
import java.io.File
import java.io.FileOutputStream

private const val TAG = "FeedAudioActivity"
class FeedAudioActivity : AppCompatActivity(), SelectedFilesCount {

    private lateinit var binding: ActivityFeedAudioBinding
    private val REQUEST_PERMISSION = 158
    val audioList = mutableListOf<AudioDataClass>()
    val uriList = mutableListOf<Uri>()
    private lateinit var audioAdapter: FeedAudioAdapter
    private val selectedAudios: MutableList<String> = mutableListOf()
    private val selectedUris: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_feed_audio)
        binding = ActivityFeedAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.toolbar.backIcon.setOnClickListener {
            onBackPressed()
        }


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {
            loadAndDisplayAudio()
        }

        binding.toolbar.likeIcon.setOnClickListener {
            val selectedAudios = audioAdapter.getSelectedAudios()
            Log.d(TAG, "onCreate: selectedAudios size: ${selectedAudios.size} ")
            for(i in selectedAudios) {
                this.selectedAudios.add(i.audioList)
            }

            val arrayListToSend = ArrayList(this.selectedAudios)

            Log.d(TAG, "onCreate: arrayListToSend ${arrayListToSend.size}")
            val resultIntent = Intent()
            resultIntent.putStringArrayListExtra("audio_url", arrayListToSend)
            setResult(RESULT_OK, resultIntent)
            finish()

        }
    }
    private fun getAlbumArt(audioPath: String): Uri? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(audioPath)

            val albumArtBytes = mediaMetadataRetriever.embeddedPicture

            if (albumArtBytes != null) {
                val albumArtFile = saveAlbumArt(albumArtBytes)
                Uri.fromFile(albumArtFile)
            } else {
                null
            }
        } catch (e: Exception){
            e.printStackTrace()
            null
        } finally {
            // Make sure to release the resources when done
            mediaMetadataRetriever.release()
        }

    }
    private fun saveAlbumArt(albumArtBytes: ByteArray): File {
        val outputDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val outputFile = File.createTempFile("album_art", ".jpg", outputDir)

        val outputStream: FileOutputStream = FileOutputStream(outputFile)
        outputStream.write(albumArtBytes)
        outputStream.close()

        return outputFile
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAndDisplayAudio()
        }
    }


    private fun loadAndDisplayAudio() {
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {

                val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val audioPath = it.getString(dataIndex)
                    if (!audioPath.isNullOrBlank()) {
                        audioList.add(AudioDataClass(audioPath, artWork = ""))
                        val uri = ContentUris.withAppendedId(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            it.getLong(0)
                        )
                        uriList.add(uri)
                    }
                } while (it.moveToNext())

                val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                val layoutManager = LinearLayoutManager(this)
                recyclerView.layoutManager = layoutManager
                audioAdapter = FeedAudioAdapter(audioList, resources, this) {

                }
                recyclerView.adapter = audioAdapter
            } else {
                // Handle the case where there are no audio files
                Toast.makeText(this, "No audio files found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onFilesCount(count: Int) {
        binding.toolbar.count.text = "$count"
    }
}