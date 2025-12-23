package com.uyscuti.social.circuit.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.uyscuti.social.circuit.R

class CustomAlertDialog(context: Context) {

    interface DialogCallback {
        fun onUserInputEntered(userInput: String)
        fun onDialogDismissed()
    }

    private var callback: DialogCallback? = null

    fun setDialogCallback(callback: DialogCallback) {
        this.callback = callback
    }

    private val alertDialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_alert_dialog, null)

        val editText = dialogView.findViewById<EditText>(R.id.editText)

        builder.setView(dialogView)
            .setTitle("Custom Alert Dialog")
            .setPositiveButton("OK") { dialog, _ ->
                val userInput = editText.text.toString()
                callback?.onUserInputEntered(userInput)
                callback?.onDialogDismissed()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                callback?.onDialogDismissed()
                dialog.cancel()
            }

        alertDialog = builder.create()
    }

    fun show() {
        alertDialog.show()
    }
}
