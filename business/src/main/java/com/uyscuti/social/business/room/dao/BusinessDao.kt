package com.uyscuti.social.business.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.entity.MyProductEntity


@Dao
interface BusinessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(businesses: BusinessEntity)

    @Query("SELECT * FROM business")
    suspend fun getBusinessProfile(): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMyProduct(myProduct: MyProductEntity)

    @Query("SELECT * FROM `my-products`")
    suspend fun getMyProducts(): List<MyProductEntity>

    @Query("DELETE FROM `my-products` WHERE _id = :productId")
    suspend fun deleteMyProduct(productId: String)



}