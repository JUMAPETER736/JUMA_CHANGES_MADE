package com.uyscuti.social.circuit.utils

import androidx.fragment.app.Fragment

interface ChatNavigationController {

    fun openChat(id: Long, prepopulateText: String?)

    fun currentFragment(id: String)

    fun unreadCount(id: Int,  count: Int)
}

fun Fragment.getChatNavigationController(): ChatNavigationController? {
    val parentFragment = parentFragment
    return if (parentFragment is ChatNavigationController) {
        parentFragment
    } else {
        null // Return null if the parent fragment does not implement the interface
    }
}
