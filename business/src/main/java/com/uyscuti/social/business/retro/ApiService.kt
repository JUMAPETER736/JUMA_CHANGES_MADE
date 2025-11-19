package com.uyscuti.social.business.retro


//import com.uyscut.network.api.request.business.catalogue.GetCatalogueByUserId
//import com.uyscut.network.api.request.business.create.CreateBusinessProfile
//import com.uyscut.network.api.request.business.users.GetAvailableUsers
//import com.uyscut.network.api.request.business.users.GetBusinessProfileById
//import com.uyscuti.social.business.retro.response.background.BackgroundImageResponse
//import com.uyscut.network.api.response.business.response.background.BackgroundVideoResponse
//import com.uyscut.network.api.request.business.catalogue.GetMyCatalogueResponse
//import com.uyscut.network.api.request.business.create.CreateProfileResponse
//import com.uyscut.network.api.request.business.livelocation.LiveLocationResponse
//import com.uyscut.network.api.request.business.product.AddProductResponse
//import com.uyscut.network.api.request.business.product.DeleteProductResponse
//import com.uyscut.network.api.request.business.product.GetProductsResponse
//import com.uyscut.network.api.request.business.profile.ProfileResponse
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.DELETE
//import retrofit2.http.Field
//import retrofit2.http.FormUrlEncoded
//import retrofit2.http.GET
//import retrofit2.http.Multipart
//import retrofit2.http.PATCH
//import retrofit2.http.POST
//import retrofit2.http.Part
//import retrofit2.http.Path

//interface ApiService {
//    @FormUrlEncoded
//    @POST("users/login")
//    suspend fun login(
//        @Field("username") username: String,
//        @Field("password") password: String
//    ): com.uyscut.network.api.request.business.login.LoginResponse // Define LoginResponse based on your API response structure
//
//    // Define other API methods as needed
//
//    @FormUrlEncoded
//    @POST("users/register")
//    suspend fun register(
//        @Field("username") username: String,
//        @Field("password") password: String,
//        @Field("email") email: String
//    ): LoginResponse // Define LoginResponse based on your API response structure
//
//    @GET("business/profile")
//    suspend fun getBusinessProfile(): Response<ProfileResponse>
//
//    @POST("business/profile")
//    suspend fun createBusinessProfile(
//        @Body profile: CreateBusinessProfile
//    ): Response<CreateProfileResponse>
//
//    @GET("business/catalogue")
//    suspend fun getCatalogue(): Response<GetMyCatalogueResponse>
//
//    @Multipart
//    @PATCH("business/profile/background")
//    suspend fun updateBackground(@Part avatar: MultipartBody.Part): Response<BackgroundImageResponse>
//
//    @Multipart
//    @PATCH("business/profile/livelocation")
//    suspend fun updateLiveLocation(
//        @Part("latitude") latitude: RequestBody,
//        @Part("longitude") longitude: RequestBody,
//        @Part("accuracy") accuracy: RequestBody,
//        @Part("range") range: RequestBody,
//    ): Response<LiveLocationResponse>
//
//    @Multipart
//    @PATCH("business/profile/v")
//    suspend fun updateBackgroundVideo(@Part video: MultipartBody.Part, @Part thumbnail: MultipartBody.Part): Response<BackgroundVideoResponse>
//
//
//    @Multipart
//    @POST("business/catalogue/product")
//    suspend fun addProduct(
//        @Part("itemName") itemName: RequestBody,
//        @Part("description") description: RequestBody,
//        @Part("features") features: RequestBody,
//        @Part("price") price: RequestBody,
//        @Part images: List<MultipartBody.Part>
//    ): Response<AddProductResponse>
//
//    @GET("business/catalogue/m/products")
//    suspend fun getProducts(): Response<GetProductsResponse>
//
//    @DELETE("business/catalogue/products/{productId}")
//    suspend fun deleteProduct(@Path("productId") productId: String): Response<DeleteProductResponse>
//
//
//    @GET("chat-app/chats/users")
//    suspend fun getOtherUsers(): Response<GetAvailableUsers>
//
//    @GET("business/profile/{userId}")
//    suspend fun getUserBusinessProfile(@Path("userId") userId: String): Response<GetBusinessProfileById>
//
//    @GET ("business/catalogue/{userId}")
//    suspend fun getUserBusinessCatalogue(@Path("userId") userId: String): Response<GetCatalogueByUserId>
//
//}
