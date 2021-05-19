package com.evangelos.couchbase.lite.core.extensions

import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter

fun <T> ResultSet.toData(
    converter: ResultSetConverter,
    clazz: Class<T>
): List<T> = converter.resultSetToData(this, clazz)