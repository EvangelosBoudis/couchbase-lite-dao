package com.evangelos.couchbase.lite.core.extensions

import android.os.AsyncTask
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Query
import com.couchbase.lite.QueryChange
import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

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

@ExperimentalCoroutinesApi
fun Query.observeChange(
    executor: Executor
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

@ExperimentalCoroutinesApi
fun Query.observeResultSet(
    executor: Executor = AsyncTask.SERIAL_EXECUTOR,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Flow<ResultSet> {
    return observeChange(executor)
        .map { change -> change.results }
        .flowOn(dispatcher)
}

@ExperimentalCoroutinesApi
fun <T> Query.observeData(
    executor: Executor = AsyncTask.SERIAL_EXECUTOR,
    converter: ResultSetConverter,
    clazz: Class<T>
): Flow<List<T>> {
    return observeChange(executor)
        .map { change -> converter.resultSetToData(change.results, clazz) }
        .flowOn(Dispatchers.IO)
}