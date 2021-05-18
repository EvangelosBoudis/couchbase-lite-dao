package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument

interface DataConverter {

    fun <T> dataToMutableDocument(data: T, documentType: String, clazz: Class<T>): MutableDocument?

    fun <T> dataToMap(data: T, documentType: String): Map<String, Any>?

}