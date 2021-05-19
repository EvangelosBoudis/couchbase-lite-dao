package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.evangelos.couchbase.lite.dao.di.AppModule

/**
 * Factory for creating [ViewModel] instances.
 */
class ViewModelFactory : CommonViewModelFactory()

/**
 * Factory that contains the base definitions of the app's ViewModels.
 */
abstract class CommonViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AccountsViewModel::class.java -> AccountsViewModel(AppModule.accountDao)
            AccountConstructorViewModel::class.java -> AccountConstructorViewModel(AppModule.accountDao)
            AccountDetailsViewModel::class.java -> AccountDetailsViewModel(AppModule.accountDao)
            else -> error("Unhandled ViewModel of type ${modelClass.simpleName}")
        } as T
    }

}