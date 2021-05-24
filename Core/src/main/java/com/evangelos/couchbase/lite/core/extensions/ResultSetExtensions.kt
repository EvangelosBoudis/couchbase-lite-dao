package com.evangelos.couchbase.lite.core.extensions

import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter

/**
 * Converts [ResultSet] to a [List] of [T] instances using the [ResultSetConverter.resultSetToData].
 * @param converter converter to transform [ResultSet] to a [List] of [T].
 * @param clazz the class we want to convert the given results.
 * @return list of type [T]. The size can be equal or less than the number of given results.
 * */
suspend fun <T> ResultSet.toData(
    converter: ResultSetConverter,
    clazz: Class<T>
): List<T> = converter.resultSetToData(this, clazz)