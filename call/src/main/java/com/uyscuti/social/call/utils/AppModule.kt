package com.uyscuti.social.call.utils

import android.content.Context
import android.os.PowerManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

//    @Provides
//    fun provideContext(@ApplicationContext context:Context) : Context = context.applicationContext

    @Provides
    fun provideGson():Gson = Gson()

    @Provides
    @Singleton
    fun providePowerManager(context: Context): PowerManager {
        return context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

//    @Provides
//    fun provideDataBaseInstance():FirebaseDatabase = FirebaseDatabase.getInstance()
//
//    @Provides
//    fun provideDatabaseReference(db:FirebaseDatabase): DatabaseReference = db.reference
}