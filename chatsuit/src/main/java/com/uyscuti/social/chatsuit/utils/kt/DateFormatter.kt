package com.uyscuti.social.chatsuit.utils.kt

import java.text.SimpleDateFormat
import java.util.*

object DateFormatter {
    fun format(date: Date?, template: Template): String {
        return date?.let { format(it, template.get()) } ?: ""
    }

    fun format(date: Date?, format: String): String {
        return date?.let { SimpleDateFormat(format, Locale.getDefault()).format(it) } ?: ""
    }

    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) {
            throw IllegalArgumentException("Dates must not be null")
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        if (cal1 == null || cal2 == null) {
            throw IllegalArgumentException("Dates must not be null")
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR))
    }

    fun isSameYear(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) {
            throw IllegalArgumentException("Dates must not be null")
        }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameYear(cal1, cal2)
    }

    fun isSameYear(cal1: Calendar, cal2: Calendar): Boolean {
        if (cal1 == null || cal2 == null) {
            throw IllegalArgumentException("Dates must not be null")
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
    }

    fun isToday(calendar: Calendar): Boolean {
        return isSameDay(calendar, Calendar.getInstance())
    }

    fun isToday(date: Date?): Boolean {
        date?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            return isSameDay(calendar, Calendar.getInstance())
        }
        return false
    }

    fun isYesterday(calendar: Calendar): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_MONTH, -1)
        return isSameDay(calendar, yesterday)
    }

    fun isYesterday(date: Date?): Boolean {
        date?.let {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_MONTH, -1)
            val calendar = Calendar.getInstance()
            calendar.time = it
            return isSameDay(calendar, yesterday)
        }
        return false
    }

    fun isCurrentYear(date: Date?): Boolean {
        date?.let {
            return isSameYear(it, Calendar.getInstance().time)
        }
        return false
    }

    fun isCurrentYear(calendar: Calendar): Boolean {
        return isSameYear(calendar, Calendar.getInstance())
    }

    interface Formatter {
        fun format(date: Date): String
    }

    enum class Template(val template: String) {
        STRING_DAY_MONTH_YEAR("d MMMM yyyy"),
        STRING_DAY_MONTH("d MMMM"),
        TIME("HH:mm");

        fun get(): String {
            return template
        }
    }
}
