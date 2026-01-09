package com.uyscuti.social.business.popupDialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.uyscuti.social.business.R

class BusinessProfileDialogFragment : DialogFragment() {

    interface BusinessProfileDialogListener {
        fun onCreateProfile()
    }

    private var listener: BusinessProfileDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.TransparentDialog)
        val view = layoutInflater.inflate(R.layout.create_business_profile_popup_dialog, null)
        dialog.setContentView(view)

        setupViews(view)

        return dialog
    }

    private fun setupViews(view: View) {
        view.findViewById<ImageButton>(R.id.btn_close).setOnClickListener {
            dismiss()
        }


        view.findViewById<MaterialButton>(R.id.btn_create_profile).setOnClickListener {
            listener?.onCreateProfile()
            dismiss()
        }
    }

    fun setListener(listener: BusinessProfileDialogListener) {
        this.listener = listener
    }

    companion object {
        fun newInstance(): BusinessProfileDialogFragment {
            return BusinessProfileDialogFragment()
        }
    }
}