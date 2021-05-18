package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.ResultSet
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

open class ResultSetConverterImpl: ResultSetConverter {

    protected val converer = Gson()

    override fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T? {
        return try {
            converer.fromJson(converer.toJson(map), clazz)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    override fun <T> resultSetToData(resultSet: ResultSet, clazz: Class<T>): List<T> {
        val list: MutableList<T> = ArrayList()
        for (result in resultSet.allResults()) {
            val map: Map<String, Any> = (if (result.count() > 0) result.getDictionary(0)?.toMap() ?: result.toMap() else null) ?: continue
            val data = mapToData(map, clazz) ?: continue
            list.add(data)
        }
        return list
    }

}