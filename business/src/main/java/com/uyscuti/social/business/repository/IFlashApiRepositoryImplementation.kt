package com.uyscuti.social.business.repository

import android.util.Log
import com.uyscuti.social.business.model.business.BusinessProfile
import com.uyscuti.social.network.api.response.business.response.post.BusinessPost
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.Response


@Suppress("UNCHECKED_CAST")
class IFlashApiRepositoryImplementation(
    private val retrofitInstance: RetrofitInstance
): IFlashApiRepository {


    override suspend fun getAllCatalogues(page: String): Result<BusinessPost> {
        return try {
            // Add logging to verify the page parameter
            Log.d("Pagination", "Requesting page: $page")
            val response = retrofitInstance.apiService.getBusinessPost(page)

            if(response.isSuccessful){

                val responseBody = response.body()

                Result.success(responseBody)
            } else {
                Log.e("Pagination", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to fetch catalogue items"))
            }

        } catch (e: Exception) {
            Result.failure<BusinessPost>(e)
        } as Result<BusinessPost>

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

    override suspend fun searchCatalogues(
        query: String,
        page: String
    ): Result<BusinessPost> {
        return try {
            val response = retrofitInstance.apiService.searchByCategory(query, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Search failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}