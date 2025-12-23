package com.uyscuti.social.circuit.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.uyscuti.social.core.common.data.room.entity.DialogEntity
import com.uyscuti.social.core.common.data.room.repository.DialogRepository
import com.uyscuti.social.circuit.presentation.DialogViewModel
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatManager @Inject constructor(
    private val retrofitInstance: RetrofitInstance,
    private val dialogRepository: DialogRepository,
    private val viewModelFactory: ViewModelProvider.Factory, // Inject ViewModelProvider.Factory
    private val viewModelStoreOwner: ViewModelStoreOwner, // Inject ViewModelStoreOwner
    @ApplicationContext private val context: Context
) {
    private val TAG = "ChatManager"

//    private val dialogViewModel: DialogViewModel by viewModels()

    var listener: ChatManagerListener? = null


   private val dialogViewModel: DialogViewModel by lazy {
        val initializer: () -> DialogViewModel = {
            // Pass DialogRepository to DialogViewModel
            ViewModelProvider(viewModelStoreOwner, viewModelFactory)[DialogViewModel::class.java].apply {
              dialogRepository
            }
        }
        initializer.invoke()
    }


    init {
        CoroutineScope(Dispatchers.IO).launch {
            observeDialogs()
        }
    }

    private suspend fun observeAndCreateChats(dialogs: List<DialogEntity>) {
        for (dialogEntity in dialogs) {
            for (user in dialogEntity.users) {
                try {
                    val response = retrofitInstance.apiService.createUserChat(user.id)

                    if (response.isSuccessful) {
                        val newChatId = response.body()?.data?._id
                        Log.d(TAG, "Created Chat : ${response.body()?.data}")
                        Log.d(TAG, "Created Chat Id: $newChatId")
                        if (newChatId != null) {
                            modifyDialogEntityId(dialogEntity, newChatId)
                        } else {
                            // Handle invalid response
                        }
                    } else {
                        // Handle failed API request
                    }
                } catch (e: Exception) {
                    // Handle exception during API request
                }
            }
        }
    }

    private fun modifyDialogEntityId(dialogEntity: DialogEntity, newChatId: String) {
        // Modify the ID of the dialog entity in the local database
        GlobalScope.launch(Dispatchers.IO) {
            try {
                dialogRepository.updateDialogEntityId(dialogEntity.id, newChatId)
                listener?.onDialogUpdated(newChatId)
                // Notify the ViewModel about the update
//                dialogViewModel.updateDialogEntityId(dialogEntity.id, newChatId)
            } catch (e: Exception) {
                // Handle error updating dialog entity ID in the database
                Log.e(TAG, "Error updating dialog entity ID in the database ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun observeDialogs() {
        withContext(Dispatchers.Main) {
            dialogRepository.getTempDialogs().observeForever(Observer { dialogs ->
                // Handle the observed LiveData here
                // You can perform actions based on the updated dialogs
                Log.d(TAG, "observeDialogs: $dialogs")
                if (!dialogs.isNullOrEmpty()){
                    CoroutineScope(Dispatchers.IO).launch {
                        observeAndCreateChats(dialogs)
                    }
                }
            })
        }
    }

    interface ChatManagerListener{
        fun onDialogUpdated(newDialogId: String)
    }
}

