package com.uyscuti.social.circuit.views

import android.content.Context
import android.graphics.Rect
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.uyscuti.social.circuit.R

class CustomEditText : AppCompatEditText {

    private val drawableBounds = Rect()

    private var isPasswordVisible = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        // Initialize the drawableBounds to the bounds of the right drawable
        val drawables = compoundDrawables
        val rightDrawable = drawables[2] // Assuming right drawable is at index 2
        rightDrawable?.copyBounds(drawableBounds)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        if (event.action == MotionEvent.ACTION_UP && drawableBounds.contains(x, y)) {
            // Handle the click event for the right drawable here
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_remove_red_eye_24, 0)
            } else {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_visibility_off_24, 0)
            }

            // Move the cursor to the end of the text
            text?.let { setSelection(it.length) }

            // Call performClick to handle the click event
            performClick()
            return true
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        // Handle the click event for the entire view here if needed
        return super.performClick()
    }
}
