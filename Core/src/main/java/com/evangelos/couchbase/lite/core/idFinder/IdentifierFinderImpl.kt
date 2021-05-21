package com.evangelos.couchbase.lite.core.idFinder

import com.evangelos.couchbase.lite.core.Id

class IdentifierFinderImpl: IdentifierFinder {

    override fun <T> findId(data: T, clazz: Class<T>): String {
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

    override fun <T> findIds(data: List<T>, clazz: Class<T>): List<String> {
        return data.map { findId(it, clazz) }
    }

}