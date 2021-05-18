package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.ResultSet

interface ResultSetConverter {

    fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T?

    fun <T> resultSetToData(resultSet: ResultSet, clazz: Class<T>): List<T>

}