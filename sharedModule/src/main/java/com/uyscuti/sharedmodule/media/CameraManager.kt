package com.uyscuti.sharedmodule.media

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera Manager for capturing photos and recording videos
 * Uses CameraX API for modern camera operations
 *
 * @author Godbless Mlenga
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var recording: Recording? = null

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    companion object {
        private const val TAG = "CameraManager"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    /**
     * Callbacks for camera operations
     */
    interface CameraCallback {
        fun onPhotoCapture(uri: Uri, file: File?)
        fun onPhotoCaptureError(exception: Exception)
        fun onVideoRecordingStart()
        fun onVideoRecordingStop(uri: Uri, file: File?)
        fun onVideoRecordingError(exception: Exception)
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Start the camera with preview, photo capture, and video recording capabilities
     */
    fun startCamera(callback: CameraCallback? = null) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(callback)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                callback?.onPhotoCaptureError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(callback: CameraCallback?) {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera provider is null")

        // Preview use case
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        // Video capture use case
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        // Camera selector
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )

        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
            callback?.onPhotoCaptureError(e)
        }
    }

    /**
     * Capture a photo and save it to MediaStore
     */
    fun capturePhoto(callback: CameraCallback) {
        val imageCapture = imageCapture ?: run {
            callback.onPhotoCaptureError(IllegalStateException("Image capture not initialized"))
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FlashApp")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return
                    Log.d(TAG, "Photo saved: $savedUri")

                    // Get file path if needed
                    val file = getFileFromUri(savedUri)
                    callback.onPhotoCapture(savedUri, file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exception)
                    callback.onPhotoCaptureError(exception)
                }
            }
        )
    }

    /**
     * Start video recording
     */
    fun startRecording(callback: CameraCallback) {
        val videoCapture = videoCapture ?: run {
            callback.onVideoRecordingError(IllegalStateException("Video capture not initialized"))
            return
        }

        if (recording != null) {
            callback.onVideoRecordingError(IllegalStateException("Recording already in progress"))
            return
        }

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/FlashApp")
            }
        }

        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .apply {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d(TAG, "Video recording started")
                        callback.onVideoRecordingStart()
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val uri = recordEvent.outputResults.outputUri
                            Log.d(TAG, "Video saved: $uri")
                            val file = getFileFromUri(uri)
                            callback.onVideoRecordingStop(uri, file)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video recording error: ${recordEvent.cause}")
                            callback.onVideoRecordingError(
                                (recordEvent.cause ?: Exception("Unknown recording error")) as Exception
                            )
                        }
                    }
                }
            }
    }

    /**
     * Stop video recording
     */
    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean = recording != null

    /**
     * Switch between front and back camera
     */
    fun switchCamera(callback: CameraCallback? = null) {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera(callback)
    }

    /**
     * Get the current camera's torch state
     */
    fun hasFlash(): Boolean = camera?.cameraInfo?.hasFlashUnit() ?: false

    /**
     * Enable or disable flash/torch
     */
    fun setFlashMode(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    /**
     * Helper to convert URI to File
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (columnIndex != -1) {
                        val path = it.getString(columnIndex)
                        File(path)
                    } else null
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file from URI", e)
            null
        }
    }

    /**
     * Clean up resources
     */
    fun shutdown() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}

// Extension function for Activity to request permissions
fun AppCompatActivity.createPermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            onGranted()
        } else {
            onDenied()
        }
    }
}