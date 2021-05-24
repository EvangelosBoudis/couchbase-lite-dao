package com.evangelos.couchbase.lite.core.converters

import com.evangelos.couchbase.lite.core.TYPE
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

/**
 * Implementation of [DocumentConverter] using the Gson Library.
 */
class DocumentConverterGson(
    private val gson: Gson,
    // private val identifierFinder: IdentifierFinder = IdentifierFinderImpl()
): ResultSetConverterGson(gson), DocumentConverter {

    /**
     * Converts the given instance of type [T] to [Map], using [Gson] converter and then applies the type of Couchbase Document into that Map.
     * @param data instance of type [T] to serialize.
     * @param documentType Couchbase document type.
     * @return map or null if [JsonSyntaxException] occurred.
     */
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

}