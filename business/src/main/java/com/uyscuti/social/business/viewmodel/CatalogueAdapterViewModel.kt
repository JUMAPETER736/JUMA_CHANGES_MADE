package com.uyscuti.social.business.viewmodel

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.uyscuti.sharedmodule.model.Catalogue
import com.uyscuti.social.business.room.database.BusinessDatabase
import com.uyscuti.social.business.room.repository.BusinessRepository
import androidx.lifecycle.viewModelScope
import com.uyscuti.social.business.room.entity.BusinessEntity
import com.uyscuti.social.business.room.entity.MyProductEntity
import com.uyscuti.social.network.api.response.business.response.post.Post
import com.uyscuti.social.network.api.response.business.response.post.UserDetails
import com.uyscuti.social.network.api.response.business.response.product.AddProductResponse
import com.uyscuti.social.network.api.retrofit.instance.RetrofitInstance
import com.uyscuti.social.network.utils.LocalStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class CatalogueAdapterViewModel @Inject constructor(
    application: Application,
    private val retrofitInstance: RetrofitInstance,
    private val localStorage: LocalStorage,
): AndroidViewModel(application) {

    private val context = getApplication<Application>()

    private val API_TAG = "ApiService"

    private val _catalogueItems = MutableLiveData<List<Catalogue>>()

    private val _isLoading = MutableLiveData<Boolean>()

    val catalogueItems: LiveData<List<Catalogue>> = _catalogueItems

    val isLoading: LiveData<Boolean> = _isLoading

    private val catalogueOriginalList = mutableListOf<Catalogue>()

    private var businessDatabase: BusinessDatabase = BusinessDatabase.getInstance(context)
    private var businessRepository: BusinessRepository = BusinessRepository(businessDatabase.businessDao())

    private val _cataloguePost: MutableLiveData<Post> = MutableLiveData()

    val cataloguePost = _cataloguePost

    private val settings =  context.getSharedPreferences("LocalSettings", MODE_PRIVATE)

    init {
        loadCatalogue()
    }

     fun addItems(item: Catalogue) {

        viewModelScope.launch {

            val imageFiles = arrayListOf<File>()
            for (image in item.images) {

                Log.d("ApiService", "image: $image")
                val imageFile = File(image)
                imageFiles.add(imageFile)
            }

            try {
                val createProductResponse = createProduct(
                    item.name,
                    item.description,
                    "none",
                    item.price,
                    imageFiles
                )

                if (createProductResponse.isSuccessful) {
                    Log.d(API_TAG, "Product created successfully")
                    Log.d(API_TAG, "Response: ${createProductResponse.body()}")


                    val product = createProductResponse.body()!!.data

                    val username = localStorage.getUsername()
                    val avatar = settings.getString("profile_pic", "").toString()
                    val userDetails = UserDetails(
                        avatar,
                        "",
                        "",
                        username
                    )

                    val post = Post(
                        product.__v,
                        product._id,
                        0,
                        product.catalogue,
                        0,
                        product.createdAt,
                        product.description,
                        product.features,
                        product.images,
                        false,
                        false,
                        false,
                        product.itemName,
                        0,
                        product.owner,
                        product.price,
                        product.updatedAt,
                        userDetails
                    )

                    _cataloguePost.postValue(post)


                    val myProduct = MyProductEntity(
                        product._id,
                        product.__v,
                        product.catalogue,
                        product.createdAt,
                        product.description,
                        product.features,
                        product.images,
                        product.itemName,
                        product.owner,
                        product.price,
                        product.updatedAt
                    )

                    insertMyProduct(myProduct)

                    // IMPORTANT: Manually add to list and update LiveData
                    val newCatalogue = Catalogue(
                        product._id,
                        product.itemName,
                        product.description,
                        product.price,
                        product.images
                    )

                    catalogueOriginalList.add(newCatalogue)

                    // Trigger observer notification by updating LiveData
                    _catalogueItems.value = catalogueOriginalList.toList()


                } else {
                    Log.d(API_TAG, "Error: ${createProductResponse.errorBody()?.string()}")
                }
            }catch (e:HttpException){
                val API_TAG = null
                Log.d(API_TAG, "Error: $e")
                Log.d(API_TAG, "Error: ${e.response()?.errorBody()?.string()}")
            }catch (e:IOException){
                Log.d(API_TAG, "Error: $e")
            }

        }
    }

    fun loadCatalogue() {
        viewModelScope.launch {
            try {

                _isLoading.value = true

                val catalogues = getMyProducts()
                // Clear the list before adding new items
                catalogueOriginalList.clear()


                if (catalogues.isNotEmpty()) {
                    for (product in catalogues) {
                        Log.d(API_TAG, "Room product: $product")
                        val catalogue = Catalogue(
                            product._id,
                            product.itemName,
                            product.description,
                            product.price,
                            product.images
                        )
                        catalogueOriginalList.add(catalogue)
                    }
                } else {
                    loadCatalogueDataFromServer()
                }

                // Update LiveData with new list
                _catalogueItems.value = catalogueOriginalList.toList()

                _isLoading.value = false

            } catch (e: Exception) {
                Log.e(API_TAG, "Error loading catalogue: $e")
            }
        }
    }

    private suspend fun getBusiness(): BusinessEntity? {
        return businessRepository.getBusiness()
    }

    private suspend fun getMyProducts(): List<MyProductEntity> {
        return businessRepository.getMyProducts()
    }

    private suspend fun insertMyProduct(productEntity: MyProductEntity){
        businessRepository.insertMyProduct(productEntity)
    }

    suspend fun deleteMyProduct(productId: String) {
        businessRepository.deleteMyProduct(productId)
    }

    private suspend fun createProduct(
        itemName: String,
        description: String,
        features: String,
        price: String,
        imageFiles: List<File>
    ): Response<AddProductResponse> {
        val itemNamePart = RequestBody.create("text/plain".toMediaTypeOrNull(), itemName)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val featuresPart = RequestBody.create("text/plain".toMediaTypeOrNull(), features)
        val pricePart = RequestBody.create("text/plain".toMediaTypeOrNull(), price)

        val imageParts = imageFiles.map {
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
            MultipartBody.Part.createFormData("product", it.name, requestFile)
        }

        return retrofitInstance.apiService.addProduct(itemNamePart, descriptionPart, featuresPart, pricePart, imageParts)
    }

    private suspend fun loadCatalogueDataFromServer() {

        // checking for new data from the server
        try {
            val response = retrofitInstance.apiService.getProducts()

            catalogueOriginalList.clear()

            if (response.isSuccessful) {
                val catalogueList = response.body()!!.data

                Log.d(API_TAG, "Catalogue: $catalogueList")

                for (catalogue in catalogueList) {
                    Log.d(API_TAG, "Catalogue: $catalogue")

                    val myProductEntity = MyProductEntity(
                        catalogue._id,
                        catalogue.__v,
                        catalogue.catalogue,
                        catalogue.createdAt,
                        catalogue.description,
                        catalogue.features,
                        catalogue.images,
                        catalogue.itemName,
                        catalogue.owner,
                        catalogue.price,
                        catalogue.updatedAt
                    )

                    val catalogue = Catalogue(
                        catalogue._id,
                        catalogue.itemName,
                        catalogue.description,
                        catalogue.price,
                        catalogue.images
                    )

                    catalogueOriginalList.add(catalogue)
                    insertMyProduct(myProductEntity)
                }
            }else{
                Log.d(API_TAG, "Error: ${response.errorBody()?.string()}")
            }

        }catch (e: HttpException){
            Log.d(API_TAG, "Error: $e")
            Log.d(API_TAG, "Error: ${e.response()?.errorBody()?.string()}")
        }catch (e:Throwable){
            Log.d(API_TAG, "Error: $e")
        }

    }
    suspend fun clearAll() {
        businessDatabase.businessDao().deleteAll()
        businessDatabase.businessDao().deleteAllProducts()
    }

}