package com.evangelos.couchbase.lite.core.extensions

import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Query
import com.couchbase.lite.QueryChange
import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter
import com.evangelos.couchbase.lite.core.converters.ResultSetConverterGson
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors

suspend fun <T> Query.toData(
    converter: ResultSetConverter,
    clazz: Class<T>
): List<T> = withContext(Dispatchers.IO) {
    return@withContext try {
        execute().toData(converter, clazz)
    } catch (e: CouchbaseLiteException) {
        ArrayList()
    }
}

fun Query.observeChange(
    executor: Executor = Executors.newSingleThreadExecutor()
): Flow<QueryChange> = callbackFlow {
    val token = addChangeListener(executor) { change ->
        if (change.error == null) {
            offer(change)
        } else {
            throw change.error
        }
    }
    execute()
    awaitClose {
        removeChangeListener(token)
    }
}.flowOn(Dispatchers.IO)

fun Query.observeResultSet(
    executor: Executor = Executors.newSingleThreadExecutor(),
): Flow<ResultSet> {
    return observeChange(executor)
        .map { change -> change.results }
        .flowOn(Dispatchers.IO)
}

fun <T> Query.observeData(
    executor: Executor = Executors.newSingleThreadExecutor(),
    converter: ResultSetConverter,
    clazz: Class<T>
): Flow<List<T>> {
    return observeChange(executor)
        .map { change -> converter.resultSetToData(change.results, clazz) }
        .flowOn(Dispatchers.IO)
}

fun <T> Query.observeData(
    executor: Executor = Executors.newSingleThreadExecutor(),
    converter: Gson = Gson(),
    clazz: Class<T>
): Flow<List<T>> {
    return observeChange(executor)
        .map { change -> ResultSetConverterGson(converter).resultSetToData(change.results, clazz) }
        .flowOn(Dispatchers.IO)
}
