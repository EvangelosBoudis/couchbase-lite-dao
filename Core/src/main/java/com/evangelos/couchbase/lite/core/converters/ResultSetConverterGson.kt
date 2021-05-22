package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.ResultSet
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

open class ResultSetConverterGson(private val gson: Gson): ResultSetConverter {

    override fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T? {
        return try {
            gson.fromJson(gson.toJson(map), clazz)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    override fun <T> resultSetToData(resultSet: ResultSet, clazz: Class<T>): List<T> {
        return resultSet.allResults().mapNotNull { result ->
            val map = if (result.count() > 0) result.getDictionary(0)?.toMap() ?: result.toMap() else null
            map?.let { mapToData(it, clazz) }
        }
    }

}