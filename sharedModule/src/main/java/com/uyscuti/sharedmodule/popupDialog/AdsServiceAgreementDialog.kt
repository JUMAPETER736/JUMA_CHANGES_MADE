package com.uyscuti.sharedmodule.popupDialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import androidx.annotation.MainThread
import com.google.android.material.button.MaterialButton
import com.uyscuti.sharedmodule.R
import java.lang.ref.WeakReference


class AdsServiceAgreementDialog private constructor(
    context: Context,
    private val listener: AgreementListener?,
    private val isCancelable: Boolean
) {

    private var dialogRef: WeakReference<Dialog>? = null
    private val contextRef = WeakReference(context)

    companion object {
        private const val TAG = "AdsServiceDialog"
    }

    /**
     * Interface for handling dialog interactions
     */
    interface AgreementListener {
        /**
         * Called when user accepts the agreement
         * @param agreedToPayment confirms user acknowledged payment requirement
         */
        fun onAccepted(agreedToPayment: Boolean = true)

        /**
         * Called when user declines the agreement
         */
        fun onDeclined()

        /**
         * Called when dialog is dismissed (optional override)
         */
        fun onDismissed() {}
    }

    /**
     * Builder class for creating AdsServiceAgreementDialog instances
     */
    class Builder(private val context: Context) {
        private var listener: AgreementListener? = null
        private var isCancelable: Boolean = false
        private var onAcceptCallback: ((Boolean) -> Unit)? = null
        private var onDeclineCallback: (() -> Unit)? = null
        private var onDismissCallback: (() -> Unit)? = null

        /**
         * Set full agreement listener interface
         */
        fun setListener(listener: AgreementListener): Builder {
            this.listener = listener
            return this
        }

        /**
         * Set callback for when user accepts agreement
         */
        fun setOnAcceptListener(callback: (agreedToPayment: Boolean) -> Unit): Builder {
            this.onAcceptCallback = callback
            return this
        }

        /**
         * Set callback for when user declines agreement
         */
        fun setOnDeclineListener(callback: () -> Unit): Builder {
            this.onDeclineCallback = callback
            return this
        }

        /**
         * Set callback for when dialog is dismissed
         */
        fun setDismissListener(callback: () -> Unit): Builder {
            this.onDismissCallback = callback
            return this
        }

        /**
         * Set whether dialog can be cancelled by back button or outside touch
         * Default: false (user must explicitly accept or decline)
         */
        fun setCancelable(cancelable: Boolean): Builder {
            this.isCancelable = cancelable
            return this
        }

        /**
         * Build and show the dialog
         * @return AdsServiceAgreementDialog instance for further control
         */
        @MainThread
        fun show(): AdsServiceAgreementDialog {
            // Create combined listener if callbacks provided
            val combinedListener = when {
                listener != null -> listener
                onAcceptCallback != null || onDeclineCallback != null -> {
                    object : AgreementListener {
                        override fun onAccepted(agreedToPayment: Boolean) {
                            onAcceptCallback?.invoke(agreedToPayment)
                        }

                        override fun onDeclined() {
                            onDeclineCallback?.invoke()
                        }

                        override fun onDismissed() {
                            onDismissCallback?.invoke()
                        }
                    }
                }
                else -> null
            }

            val dialog = AdsServiceAgreementDialog(context, combinedListener, isCancelable)
            dialog.show()
            return dialog
        }

        /**
         * Build dialog without showing it
         */
        fun build(): AdsServiceAgreementDialog {
            val combinedListener = when {
                listener != null -> listener
                onAcceptCallback != null || onDeclineCallback != null -> {
                    object : AgreementListener {
                        override fun onAccepted(agreedToPayment: Boolean) {
                            onAcceptCallback?.invoke(agreedToPayment)
                        }

                        override fun onDeclined() {
                            onDeclineCallback?.invoke()
                        }

                        override fun onDismissed() {
                            onDismissCallback?.invoke()
                        }
                    }
                }
                else -> null
            }

            return AdsServiceAgreementDialog(context, combinedListener, isCancelable)
        }
    }

    /**
     * Show the dialog
     */
    @MainThread
    fun show() {
        val context = contextRef.get() ?: run {
            android.util.Log.e(TAG, "Context is null, cannot show dialog")
            return
        }

        try {
            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            // Inflate layout
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_ads_service_agreement, null)
            dialog.setContentView(view)

            dialog.setCancelable(isCancelable)
            dialog.setCanceledOnTouchOutside(isCancelable)

            // Make background transparent for rounded corners
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Initialize views
            setupDialogViews(dialog)

            // Set dismiss listener
            dialog.setOnDismissListener {
                listener?.onDismissed()
                cleanup()
            }

            dialogRef = WeakReference(dialog)
            dialog.show()

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error showing dialog", e)
            listener?.onDismissed()
        }
    }

    /**
     * Setup dialog views and listeners
     */
    private fun setupDialogViews(dialog: Dialog) {
        try {
            val checkboxAgree: CheckBox = dialog.findViewById(R.id.checkboxAgree)
                ?: throw IllegalStateException("Checkbox not found in layout")

            val btnAccept: Button = dialog.findViewById(R.id.btnAccept)
                ?: throw IllegalStateException("Accept button not found in layout")

            val btnDecline: MaterialButton = dialog.findViewById(R.id.btnDecline)
                ?: throw IllegalStateException("Decline button not found in layout")

            // Initially disable accept button
            btnAccept.isEnabled = false

            // Enable/disable accept button based on checkbox
            checkboxAgree.setOnCheckedChangeListener { _, isChecked ->
                btnAccept.isEnabled = isChecked

                // Optional: Add haptic feedback
                if (isChecked) {
                    btnAccept.performHapticFeedback(
                        android.view.HapticFeedbackConstants.CONTEXT_CLICK
                    )
                }
            }

            // Accept button click
            btnAccept.setOnClickListener {
                if (checkboxAgree.isChecked) {
                    handleAccept(dialog)
                }
            }

            // Decline button click
            btnDecline.setOnClickListener {
                handleDecline(dialog)
            }

        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error setting up dialog views", e)
            dialog.dismiss()
            throw e
        }
    }

    /**
     * Handle accept action
     */
    private fun handleAccept(dialog: Dialog) {
        try {
            listener?.onAccepted(agreedToPayment = true)
            dismiss()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in accept handler", e)
        }
    }

    /**
     * Handle decline action
     */
    private fun handleDecline(dialog: Dialog) {
        try {
            listener?.onDeclined()
            dismiss()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in decline handler", e)
        }
    }

    /**
     * Dismiss the dialog
     */
    @MainThread
    fun dismiss() {
        try {
            dialogRef?.get()?.let { dialog ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error dismissing dialog", e)
        } finally {
            cleanup()
        }
    }

    /**
     * Check if dialog is currently showing
     */
    fun isShowing(): Boolean {
        return dialogRef?.get()?.isShowing == true
    }

    /**
     * Cleanup resources
     */
    private fun cleanup() {
        dialogRef?.clear()
        dialogRef = null
    }

    /**
     * Force cleanup (call in onDestroy if needed)
     */
    fun destroy() {
        dismiss()
        contextRef.clear()
    }
}
