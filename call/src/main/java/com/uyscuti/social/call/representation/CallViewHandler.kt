package com.uyscuti.social.call.representation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import kotlinx.coroutines.launch
import javax.inject.Inject



class CallViewHandler  @Inject constructor(context: Context) : ViewModel() {
    private val db = ChatDatabase.getInstance(context)
    private val callLogDao = db.callLogDao()

    fun insertCallLog(callLog: CallLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            callLogDao.insertCallLog(callLog)
        }
    }

    fun getAllCallLogs(): LiveData<List<CallLogEntity>> {
        return callLogDao.getAllCallLiveData()
    }

}