package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.*
import com.couchbase.lite.*
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.core.TYPE
import com.evangelos.couchbase.lite.core.extensions.observeData
import com.evangelos.couchbase.lite.dao.data.AccountData
import com.evangelos.couchbase.lite.dao.data.AccountDto
import com.evangelos.couchbase.lite.dao.data.DummyData
import kotlin.Result

class AccountsViewModel(
    private val database: Database,
    private val accountDao: CouchbaseDao<AccountData>
) : ViewModel() {

    val accounts = accountDao.observeAll().asLiveData()

    val accountDto: LiveData<List<AccountDto>> by lazy {
        QueryBuilder
            .select(SelectResult.property("id"), SelectResult.property("name"), SelectResult.property("email"))
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string("Account")))
            .observeData(clazz = AccountDto::class.java)
            .asLiveData()
    }

    fun deleteAccount(id: String) = liveData {
        try {
            emit(Result.success(accountDao.deleteById(id)))
        } catch (e: CouchbaseLiteException) {
            emit(Result.failure<Unit>(e))
        }
    }

    fun restoreAccounts() = liveData {
        try {
            emit(Result.success(accountDao.replaceAll(DummyData.available)))
        } catch (e: CouchbaseLiteException) {
            emit(Result.failure<Unit>(e))
        }
    }

    fun deleteAccounts() = liveData {
        try {
            emit(Result.success(accountDao.deleteAll()))
        } catch (e: CouchbaseLiteException) {
            emit(Result.failure<Unit>(e))
        }
    }

}