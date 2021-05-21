package com.evangelos.couchbase.lite.dao.di

import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.core.CouchbaseDaoImpl
import com.evangelos.couchbase.lite.dao.BuildConfig
import com.evangelos.couchbase.lite.dao.data.AccountData
import com.google.gson.Gson

object AppModule {

    val gson by lazy {
        Gson()
    }

    val database: Database by lazy {
        val config = DatabaseConfiguration()
        Database(BuildConfig.DATABASE_NAME, config)
    }

    val accountDao: CouchbaseDao<AccountData> by lazy {
        CouchbaseDaoImpl(database, gson, AccountData::class.java)
    }

}