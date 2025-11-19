package com.uyscuti.social.core.common.data.room.repository.calls

import com.uyscuti.social.core.common.data.room.dao.CallLogDao
import com.uyscuti.social.core.common.data.room.entity.CallLogEntity

class CallLogRepository(private val callLogDao: CallLogDao) {
    suspend fun insertCallLog(callLog: CallLogEntity) {
        callLogDao.insertCallLog(callLog)
    }
    suspend fun getAllCallLogs(): List<CallLogEntity> {
        return callLogDao.getAllCallLogs()
    }

    suspend fun clearAll(){
        callLogDao.deleteAll()
    }

}
