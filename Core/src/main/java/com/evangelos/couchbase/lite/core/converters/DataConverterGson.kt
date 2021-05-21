package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument
import com.evangelos.couchbase.lite.core.TYPE
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class DataConverterGson(
    private val gson: Gson,
    private val identifierFinder: IdentifierFinder
): DataConverter {

    override fun <T> dataToMap(data: T, documentType: String): Map<String, Any>? {
        return try {
            val json = gson.toJson(data)
            val map = gson.fromJson<MutableMap<String, Any>>(json, object : TypeToken<HashMap<String, Any>>() {}.type)
            map[TYPE] = documentType
            map
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    override fun <T> dataToMutableDocument(
        data: T,
        documentType: String,
        clazz: Class<T>
    ): MutableDocument? {
        val map = dataToMap(data, documentType)
        val id = identifierFinder.findId(data, clazz)
        return if (map != null) MutableDocument(id, map) else null
    }

}