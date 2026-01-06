package com.uyscuti.social.business.repository

import android.util.Log
import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@Suppress("UNCHECKED_CAST")
class IFlashApiRepositoryImplementation(
   private val retrofitInstance: RetrofitInstance
): IFlashApiRepository {


    override suspend fun getAllCatalogues(page: String): Result<List<Post>> {
        return try {

            // Add logging to verify the page parameter
            Log.d("Pagination", "Requesting page: $page")

           val response = retrofitInstance.apiService.getBusinessPost(page)

            val catalogueList = ArrayList<Post>()

            if(response.isSuccessful){

                val responseBody = response.body()


                responseBody?.let { body ->

                    val allData = body.data.posts

                    if(allData.isNotEmpty()) {

                      for(data in allData) {
                          catalogueList.add(data)
                      }
                    }
                }
                Result.success(catalogueList)
            } else {
                Log.e("Pagination", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to fetch catalogue items"))
            }

        } catch (e: Exception) {
            Result.failure<List<Post>>(e)
        }

    }

    override suspend fun getBusinessProfile(): Result<BusinessProfile> {
        return try {

            val profileResponse = retrofitInstance.apiService.getBusinessProfile()

            var businessProfile: BusinessProfile? = null

            if(profileResponse.isSuccessful) {
                val responseBody = profileResponse.body()

                if(responseBody != null) {
                    businessProfile = BusinessProfile(
                        _id = responseBody._id,
                        businessName = responseBody.businessName,
                        businessDescription = responseBody.businessDescription,
                        businessType = responseBody.businessType,
                        owner = responseBody.owner
                    )
                }

                Result.success(businessProfile)
            } else {
                Result.failure(Exception(profileResponse.toString()))
            }

        }catch (e: Exception) {
            Result.failure<BusinessProfile>(e)
        } as Result<BusinessProfile>
    }

    override suspend fun getUnreadNotifications(): Int {

        return try {
            var unreadNotificationCount = 0

            val allNotifications = retrofitInstance.apiService.getMyUnifiedNotifications("40")

            if(allNotifications.isSuccessful) {

                val notifications = allNotifications.body()?.data ?: emptyList()

                for(notification in notifications) {
                    if (!notification.read) {
                        unreadNotificationCount++
                    }
                }

                unreadNotificationCount
            } else {
                0
            }

        } catch (e: Exception) {
                0
        }

    }

     fun observeNewNotification(): Flow<Int> = callbackFlow {
        val job = launch {
            while(currentCoroutineContext().isActive) {
                trySend(getUnreadNotifications())
                delay(2000)
            }
        }

        awaitClose { job.cancel() }
    }

}