package com.uyscuti.sharedmodule.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {

    /**
     * Shows a delete confirmation dialog
     * @param context The context to show the dialog in
     * @param title The dialog title (default: "Delete Notification")
     * @param message The dialog message
     * @param onConfirm Callback when user confirms deletion
     * @param onCancel Optional callback when user cancels
     */

    fun showDeleteConfirmationDialog(
        context: Context,
        title: String = "Delete Notification",
        message: String = "Are you sure you want to delete this notification? This action cannot be undone.",
        positiveButtonText: String = "Delete",
        negativeButtonText: String = "Cancel",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onCancel?.invoke()
                dialog.dismiss()
            }
            .setCancelable(false)
            .setOnCancelListener {
                onCancel?.invoke()
            }
            .show()
    }

    /**
     * Shows a custom confirmation dialog
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                onCancel?.invoke()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
