package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.dao.data.AccountData

class AccountDetailsViewModel(
    private val accountDao: CouchbaseDao<AccountData>
) : ViewModel() {

    fun findAccountById(id: String) = liveData {
        emit(accountDao.findById(id))
    }

    fun updateAccount(
        id: String,
        name: String?,
        email: String?,
        username: String?,
        password: String?
    ) = liveData {
        val account = AccountData(id, name, email, username, password)
        emit(accountDao.update(account))
    }

}