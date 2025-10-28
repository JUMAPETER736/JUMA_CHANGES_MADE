package com.uyscuti.social.circuit.User_Interface.shorts

import java.util.concurrent.atomic.AtomicLong

object UniqueIdGenerator {
    private val counter = AtomicLong()

    fun generateUniqueId(): String {
        val currentTimeInMillis = System.currentTimeMillis()
        val uniqueId = "$currentTimeInMillis-${counter.getAndIncrement()}-${generateRandomComponent()}"
        return uniqueId
    }

    private fun generateRandomComponent(): Int {
        // Customize this method based on your requirements for randomness
        return (Math.random() * 1000).toInt()
    }
}
