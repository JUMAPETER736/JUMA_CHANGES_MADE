package com.uyscuti.social.core.di

// CustomScopes.kt
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FlashWorkerScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ChatSocketWorkerScope
