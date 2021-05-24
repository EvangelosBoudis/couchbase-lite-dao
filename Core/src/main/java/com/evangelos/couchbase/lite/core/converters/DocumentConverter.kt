package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder

/**
 * Interface used to serialize and deserialize [com.couchbase.lite.Document].
 * @see [DocumentConverterGson] for implementation.
 */
interface DocumentConverter: ResultSetConverter, IdentifierFinder {

    /**
     * Converts the given instance of type [T] to [Map].
     * @param data instance of type [T] to serialize.
     * @param documentType the type of Couchbase Document to apply into map.
     * @return map or null if [Exception] occurred.
     */
    fun <T> dataToMap(data: T, documentType: String): Map<String, Any>?

    /**
     * Converts the given instance of type [T] to [MutableDocument].
     * @param data instance of type [T] to convert.
     * @param documentType the type of Couchbase Document to apply into mutable document.
     * @param clazz the class we want to convert the given instances.
     * @return mutable document or null if [Exception] occurred.
     */
    fun <T> dataToMutableDocument(data: T, documentType: String, clazz: Class<T>): MutableDocument? {
        return dataToMap(data, documentType)?.let { map ->
            val id = findId(data, clazz)
            MutableDocument(id, map)
        }
    }

}