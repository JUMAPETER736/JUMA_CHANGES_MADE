package com.uyscuti.social.business.viewmodel.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uyscuti.social.business.repository.IFlashApiRepository

@Suppress("UNCHECKED_CAST")
class BusinessCatalogueViewModelFactory(
    private val repository: IFlashApiRepository
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessCatalogueViewModel::class.java)) {
            return BusinessCatalogueViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}