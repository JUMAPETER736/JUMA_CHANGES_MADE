package com.uyscuti.social.core.common.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity

@Dao
interface CallLogDao {
    @Insert
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("SELECT * FROM call_log")
    suspend fun getAllCallLogs(): List<CallLogEntity>

    @Query("SELECT * FROM call_log")
    fun getAllCallLiveData(): LiveData<List<CallLogEntity>>

    @Query("DELETE FROM call_log")
    suspend fun deleteAll()

    @Query("DELETE FROM call_log WHERE id IN (:callLogId)")
    suspend fun deleteCallLogs(callLogId: List<Long>)
}
