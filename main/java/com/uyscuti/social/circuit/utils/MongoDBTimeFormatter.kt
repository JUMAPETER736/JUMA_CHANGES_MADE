package com.uyscuti.social.circuit.utils

import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


class MongoDBTimeFormatter {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentTimeRunnable: Runnable

    fun startUpdatingCurrentTime(
        timestamp: String,
        onUpdate: (formattedTime: String) -> Unit
    ) {
        currentTimeRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val formattedTime = formatMongoTimestamp(timestamp, currentTime)
                onUpdate(formattedTime)
                handler.postDelayed(this, 1000)
            }
        }

        // Start updating time every second
        handler.post(currentTimeRunnable)
    }

    fun stopUpdatingCurrentTime() {
        handler.removeCallbacks(currentTimeRunnable)
    }

    private fun formatMongoTimestamp(timestamp: String, currentTime: Long): String {
        try {
            val sdf = SimpleDateFormat(ISO_FORMAT, Locale.getDefault())
            val date = sdf.parse(timestamp)

            // Calculate the difference in milliseconds
            val diff = currentTime - date.time

            return when {
                diff < DateUtils.MINUTE_IN_MILLIS -> "just now"
                diff < DateUtils.HOUR_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
                    date.time,
                    currentTime,
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
                diff < DateUtils.DAY_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
                    date.time,
                    currentTime,
                    DateUtils.HOUR_IN_MILLIS
                ).toString()
                diff < 2 * DateUtils.DAY_IN_MILLIS -> "yesterday"
                diff < DateUtils.WEEK_IN_MILLIS -> {
                    val daysAgo = TimeUnit.MILLISECONDS.toDays(diff)
//                    if (daysAgo == 1L) {
//                        "1 day ago"
//                    } else {
//                        "$daysAgo days ago"
//                    }
                    "${daysAgo}d"

                }
                diff < DateUtils.DAY_IN_MILLIS * 30 -> {
                    val weeksAgo = TimeUnit.MILLISECONDS.toDays(diff) / 7
//                    if (weeksAgo == 1L) {
//                        "1 week ago"
//                    } else {
//                        "$weeksAgo weeks ago"
//                    }
                    "${weeksAgo}w"

                }
                diff < DateUtils.DAY_IN_MILLIS * 365 -> {
                    val monthsAgo = TimeUnit.MILLISECONDS.toDays(diff) / 30
//                    if (monthsAgo == 1L) {
//                        "1 month ago"
//                    } else {
//                        "$monthsAgo months ago"
//                    }
                    "${monthsAgo}m"

                }
                else -> {
                    val yearsAgo = TimeUnit.MILLISECONDS.toDays(diff) / 365
//                    if (yearsAgo == 1L) {
//                        "1 year ago"
//                    } else {
//                        "$yearsAgo years ago"
//                    }
                    "${yearsAgo}y"
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            return "error" // return an error message if parsing fails
        }
    }

    companion object {
        const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    }
}

//class MongoDBTimeFormatter {
//
//    private val handler = Handler(Looper.getMainLooper())
//    private lateinit var currentTimeRunnable: Runnable
//
//    fun startUpdatingCurrentTime(
//        onUpdate: (formattedTime: String) -> Unit
//    ) {
//        currentTimeRunnable = object : Runnable {
//            override fun run() {
//                val currentTime = System.currentTimeMillis()
//                val formattedTime = formatMongoTimestamp(currentTime)
//                onUpdate(formattedTime)
//                handler.postDelayed(this, 1000)
//            }
//        }
//
//        // Start updating time every second
//        handler.post(currentTimeRunnable)
//    }
//
//    fun stopUpdatingCurrentTime() {
//        handler.removeCallbacks(currentTimeRunnable)
//    }
//
//    private fun formatMongoTimestamp(timestamp: Long): String {
//        try {
//            val date = Date(timestamp)
//
//            val currentTime = System.currentTimeMillis()
//
//            // Calculate the difference in milliseconds
//            val diff = currentTime - date.time
//
//            return when {
//                diff < DateUtils.MINUTE_IN_MILLIS -> "just now"
//                diff < DateUtils.HOUR_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
//                    date.time,
//                    currentTime,
//                    DateUtils.MINUTE_IN_MILLIS
//                ).toString()
//                diff < DateUtils.DAY_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
//                    date.time,
//                    currentTime,
//                    DateUtils.HOUR_IN_MILLIS
//                ).toString()
//                diff < 2 * DateUtils.DAY_IN_MILLIS -> "yesterday"
//                diff < DateUtils.WEEK_IN_MILLIS -> {
//                    val daysAgo = TimeUnit.MILLISECONDS.toDays(diff)
//                    if (daysAgo == 1L) {
//                        "1 day ago"
//                    } else {
//                        "$daysAgo days ago"
//                    }
//                }
//                diff < DateUtils.DAY_IN_MILLIS * 30 -> {
//                    val weeksAgo = TimeUnit.MILLISECONDS.toDays(diff) / 7
//                    if (weeksAgo == 1L) {
//                        "1 week ago"
//                    } else {
//                        "$weeksAgo weeks ago"
//                    }
//                }
//                diff < DateUtils.DAY_IN_MILLIS * 365 -> {
//                    val monthsAgo = TimeUnit.MILLISECONDS.toDays(diff) / 30
//                    if (monthsAgo == 1L) {
//                        "1 month ago"
//                    } else {
//                        "$monthsAgo months ago"
//                    }
//                }
//                else -> {
//                    val yearsAgo = TimeUnit.MILLISECONDS.toDays(diff) / 365
//                    if (yearsAgo == 1L) {
//                        "1 year ago"
//                    } else {
//                        "$yearsAgo years ago"
//                    }
//                }
//            }
//        } catch (e: ParseException) {
//            e.printStackTrace()
//            return "error" // return an error message if parsing fails
//        }
//    }
//}
