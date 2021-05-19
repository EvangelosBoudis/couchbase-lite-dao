package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.dao.data.AccountData
import java.util.*

class AccountConstructorViewModel(
    private val accountDao: CouchbaseDao<AccountData>
) : ViewModel() {

    fun saveAccount(
        name: String?,
        email: String?,
        username: String?,
        password: String?
    ) = liveData {
        val account = AccountData(UUID.randomUUID().toString(), name, email, username, password)
        emit(accountDao.save(account))
    }

}