package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument
import com.evangelos.couchbase.lite.core.TYPE
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinderImpl
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class DocumentConverterGson(
    private val gson: Gson,
    private val identifierFinder: IdentifierFinder = IdentifierFinderImpl()
): ResultSetConverterGson(gson), DocumentConverter {

    override fun <T> findId(data: T, clazz: Class<T>) = identifierFinder.findId(data, clazz)

    override fun <T> findIds(data: List<T>, clazz: Class<T>) = identifierFinder.findIds(data, clazz)

    override fun <T> dataToMap(data: T, documentType: String): Map<String, Any>? {
        return try {
            val json = gson.toJson(data)
            val map = gson.fromJson<MutableMap<String, Any>>(json, object : TypeToken<HashMap<String, Any>>() {}.type)
            map[TYPE] = documentType
            map
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    override fun <T> dataToMutableDocument(
        data: T,
        documentType: String,
        clazz: Class<T>
    ): MutableDocument? {
        val id = identifierFinder.findId(data, clazz)
        return dataToMap(data, documentType)?.let { map ->
            MutableDocument(id, map)
        }
    }

}