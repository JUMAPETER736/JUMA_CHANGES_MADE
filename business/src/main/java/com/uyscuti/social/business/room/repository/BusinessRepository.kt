package com.uyscuti.social.business.room.repository


import com.uyscuti.social.business.room.dao.BusinessDao
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.entity.MyProductEntity

class BusinessRepository(private val businessDao: BusinessDao) {

    suspend fun insertBusiness(business: BusinessEntity) {
        businessDao.insertBusiness(business)
    }

    suspend fun getBusiness(): BusinessEntity? {
        return businessDao.getBusinessProfile()
    }

    suspend fun insertMyProduct(productEntity: MyProductEntity){
        businessDao.insertMyProduct(productEntity)
    }

    suspend fun getMyProducts(): List<MyProductEntity> {
        return businessDao.getMyProducts()
    }

    suspend fun deleteMyProduct(id: String) {
        businessDao.deleteMyProduct(id)
    }

}