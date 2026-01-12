package com.uyscuti.sharedmodule.ui.fragments

import androidx.fragment.app.Fragment

object FragmentFactoryRegistry {
    private val factories = mutableMapOf<String, (String) -> Fragment>()

    fun registerFactory(key: String, factory: (String) -> Fragment) {
        factories[key] = factory
    }

    fun createFragment(key: String, username: String): Fragment? {
        return factories[key]?.invoke(username)
    }
}