package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.couchbase.lite.CouchbaseLiteException
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.dao.data.AccountData
import java.util.*

class AccountConstructorViewModel(
    private val accountDao: CouchbaseDao<AccountData>
) : ViewModel() {

    fun saveAccount(
        name: String?, email: String?, username: String?, password: String?
    ) = liveData {
        val account = AccountData(
            UUID.randomUUID().toString(), name, email, username, password
        )
        try {
            emit(Result.success(accountDao.save(account)))
        } catch (e: CouchbaseLiteException) {
            emit(Result.failure<Unit>(e))
        }
    }

}