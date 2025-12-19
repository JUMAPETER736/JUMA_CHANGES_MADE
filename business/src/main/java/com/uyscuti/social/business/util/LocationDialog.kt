package com.uyscuti.social.business.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.uyscuti.social.business.R

class LocationDialog(context: Context) {

    var dialog: AlertDialog? = null
    val builder = AlertDialog.Builder(context)
    val inflater: LayoutInflater? = LayoutInflater.from(context)
    @SuppressLint("InflateParams")
    val view = inflater?.inflate(R.layout.location_dialog, null)

    init {
        builder.setView(view)
        dialog = builder.create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
    }


    fun show() {
        dialog?.show()
    }

    fun dismiss() {
        if (dialog!!.isShowing) {
            dialog?.dismiss()
        }
    }

    fun updateMessageStatus(message: String) {
        val messageTextView: TextView = dialog!!.findViewById(R.id.messageText)
        messageTextView.text = message
    }

    fun getAlertDialog(): AlertDialog {
        return dialog!!
    }
}