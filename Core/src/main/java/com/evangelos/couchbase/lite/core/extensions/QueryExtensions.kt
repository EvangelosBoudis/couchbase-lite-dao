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

/**
 * Returns a [List] of [T] instances using the [ResultSetConverter.resultSetToData] right after the query execution.
 * @param converter converter to transform [ResultSet] to a [List] of [T].
 * @param clazz the class we want to convert the given results.
 * @return list of type [T]. The size can be equal or less than the number of given results.
 * @throws CouchbaseLiteException
 * */
suspend fun <T> Query.toData(
    converter: ResultSetConverter,
    clazz: Class<T>
): List<T> = withContext(Dispatchers.IO) {
    execute().toData(converter, clazz)
}

/**
 * Returns a [Flow] that it is responsible to register and unregister a [com.couchbase.lite.QueryChangeListener].
 * The executor will post changes, that occur in the query results, to the listener and then listener will offer changes to the [Flow].
 * @param executor The executor object that calls listener.
 * @return A flow that emits changes as long as it is observed.
 * @see [com.couchbase.lite.Query.addChangeListener]
 * */
private fun Query.observeChange(
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

/**
 * Returns a [Flow] that triggers every time a change occur in the query results
 * and then converts [QueryChange] into [T].
 * @param executor The executor object that calls listener.
 * @param converter The converter that it is responsible to convert [QueryChange] into [T].
 * @param clazz the class we want to deserialize the offered [QueryChange].
 * @return A flow that emits changes as long as it is observed.
 * @see [Query.observeChange]
 * @see [com.couchbase.lite.Query.addChangeListener]
 * */
fun <T> Query.observeData(
    executor: Executor = Executors.newSingleThreadExecutor(),
    converter: ResultSetConverter,
    clazz: Class<T>
): Flow<List<T>> {
    return observeChange(executor)
        .map { change -> converter.resultSetToData(change.results, clazz) }
        .flowOn(Dispatchers.IO)
}

//////////////////////////////////////////////////////////////////////////////////////////
/// GSON
//////////////////////////////////////////////////////////////////////////////////////////

/**
 * Returns a [List] of [T] instances using the [ResultSetConverter.resultSetToData] right after the query execution.
 * @param converter converter to transform [ResultSet] to a [List] of [T].
 * @param clazz the class we want to convert the given results.
 * @return list of type [T]. The size can be equal or less than the number of given results.
 * @throws CouchbaseLiteException
 * */
suspend fun <T> Query.toData(
    converter: Gson = Gson(),
    clazz: Class<T>
): List<T> = toData(ResultSetConverterGson(converter), clazz)

/**
 * Returns a [Flow] that triggers every time a change occur in the query results
 * and then converts [QueryChange] into [T].
 * @param executor The executor object that calls listener.
 * @param converter The converter that it is responsible to convert [QueryChange] into [T].
 * @param clazz the class we want to deserialize the offered [QueryChange].
 * @return A flow that emits changes as long as it is observed.
 * @see [Query.observeChange]
 * @see [ResultSetConverterGson.resultSetToData]
 * @see [com.couchbase.lite.Query.addChangeListener]
 * */
fun <T> Query.observeData(
    executor: Executor = Executors.newSingleThreadExecutor(),
    converter: Gson = Gson(),
    clazz: Class<T>
): Flow<List<T>> = observeData(executor, ResultSetConverterGson(converter), clazz)
