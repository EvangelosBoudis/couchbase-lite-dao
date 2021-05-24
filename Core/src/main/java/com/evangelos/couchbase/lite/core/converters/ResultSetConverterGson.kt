package com.evangelos.couchbase.lite.core.converters

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Implementation of [ResultSetConverter] using the Gson Library.
 */
open class ResultSetConverterGson(private val gson: Gson): ResultSetConverter {

    /**
     * Converts the given map to a specific single instance of the type [T], using [Gson] converter.
     * @param map key-value collection to deserialize.
     * @param clazz the class we want to deserialize the given collection.
     * @return instance of the type [T] or null if [JsonSyntaxException] occurred.
     */
    override suspend fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T? {
        return try {
            gson.fromJson(gson.toJson(map), clazz)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

}