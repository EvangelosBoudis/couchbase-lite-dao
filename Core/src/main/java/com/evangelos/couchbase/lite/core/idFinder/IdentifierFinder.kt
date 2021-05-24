package com.evangelos.couchbase.lite.core.idFinder

import com.evangelos.couchbase.lite.core.Id

/**
 * Interface used to resolve the [com.evangelos.couchbase.lite.core.Id] of a [com.evangelos.couchbase.lite.core.CouchbaseDocument].
 */
interface IdentifierFinder {

    /**
     * Returns the value of the field that [Id] is presented, in the given document.
     * @param data Couchbase document.
     * @param clazz class of document.
     * @return document unique key.
     * @throws [IllegalArgumentException] if none was found.
     */
    fun <T> findId(data: T, clazz: Class<T>): String {
        try {
            for (field in clazz.declaredFields) {
                field.isAccessible = true
                if (!field.isAnnotationPresent(Id::class.java)) continue
                return field[data] as String
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw IllegalArgumentException("@Id annotation was not found on $clazz")
    }

    /**
     * Returns the values of the fields that [Id] is presented, for each given document.
     * @param data Couchbase documents.
     * @param clazz class of documents.
     * @return document unique keys.
     * @throws [IllegalArgumentException] if some was not found.
     */
    fun <T> findAllId(data: List<T>, clazz: Class<T>): List<String> {
        return data.map {
            findId(it, clazz)
        }
    }

}