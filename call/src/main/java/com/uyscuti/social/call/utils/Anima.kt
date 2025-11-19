package com.uyscuti.social.call.utils

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

class Anima {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
   fun isAnimationScaleEnabled(contentResolver: ContentResolver): Boolean {
        val animationScale = try {
            Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
        } catch (e: Settings.SettingNotFoundException) {
            // Handle the exception, e.g., animation scale setting not found.
            e.printStackTrace()
            return false
        }

        // If the animation scale is not equal to 0 (default), animations are enabled.
        return animationScale != 0f
    }
}