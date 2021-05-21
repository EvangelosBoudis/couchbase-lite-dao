package com.evangelos.couchbase.lite.core.converters

interface IdentifierFinder {

    fun <T> findId(data: T, clazz: Class<T>): String

    fun <T> findIds(data: List<T>, clazz: Class<T>): List<String>

}