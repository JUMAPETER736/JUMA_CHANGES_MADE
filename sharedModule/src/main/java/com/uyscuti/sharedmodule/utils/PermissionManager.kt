package com.uyscuti.sharedmodule.utils

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class PermissionManager(private val activity: Activity) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestAllPermissions(onSuccess: () -> Unit, onError: (List<String>) -> Unit) {
        Dexter.withContext(activity)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_MEDIA_IMAGES,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        onSuccess()
                    } else {
                        val deniedPermissions = report.deniedPermissionResponses.map { it.permissionName }
                        onError(deniedPermissions)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permission: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }

            })
            .onSameThread()
            .check()
    }
}
