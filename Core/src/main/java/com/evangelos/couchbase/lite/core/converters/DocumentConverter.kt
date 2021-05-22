package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder

interface DocumentConverter: ResultSetConverter, IdentifierFinder {

    fun <T> dataToMap(data: T, documentType: String): Map<String, Any>?

    fun <T> dataToMutableDocument(data: T, documentType: String, clazz: Class<T>): MutableDocument?

}