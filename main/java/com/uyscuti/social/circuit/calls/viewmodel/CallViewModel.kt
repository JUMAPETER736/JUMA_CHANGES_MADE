package com.uyscuti.social.circuit.calls.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.core.common.data.room.database.ChatDatabase
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CallViewModel  @Inject constructor(context: Context) : ViewModel() {
    private val db = ChatDatabase.getInstance(context)
    private val callLogDao = db.callLogDao()


    fun insertCallLog(callLog: CallLogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            callLogDao.insertCallLog(callLog)
        }
    }

    suspend fun clearAll(){
        callLogDao.deleteAll()
    }

    fun getAllCallLogs(): LiveData<List<CallLogEntity>> {
        return callLogDao.getAllCallLiveData()
    }

    suspend fun deleteCallLogs(ids: List<Long>){
        try {
            callLogDao.deleteCallLogs(ids)
        } catch (e: Exception){
            e.printStackTrace()
        }
    }



}