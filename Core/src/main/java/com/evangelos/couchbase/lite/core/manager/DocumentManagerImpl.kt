package com.evangelos.couchbase.lite.core.manager

import com.couchbase.lite.MutableDocument
import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.*
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinderImpl
import com.google.gson.Gson

class DocumentManagerImpl(
    private val resultSetConverter: ResultSetConverter,
    private val identifierFinder: IdentifierFinder,
    private val documentConverter: DataConverter
): DocumentManager {

    override fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T? {
        return resultSetConverter.mapToData(map, clazz)
    }

    override fun <T> resultSetToData(resultSet: ResultSet, clazz: Class<T>): List<T> {
        return resultSetConverter.resultSetToData(resultSet, clazz)
    }

    override fun <T> findId(data: T, clazz: Class<T>): String {
        return identifierFinder.findId(data, clazz)
    }

    override fun <T> findIds(data: List<T>, clazz: Class<T>): List<String> {
        return identifierFinder.findIds(data, clazz)
    }

    override fun <T> dataToMutableDocument(
        data: T,
        documentType: String,
        clazz: Class<T>
    ): MutableDocument? {
        return documentConverter.dataToMutableDocument(data, documentType, clazz)
    }

    override fun <T> dataToMap(data: T, documentType: String): Map<String, Any>? {
        return documentConverter.dataToMap(data, documentType)
    }

    companion object {

        fun create(gson: Gson): DocumentManager {
            val resultSetConverter = ResultSetConverterGson(gson)
            val identifierFinder = IdentifierFinderImpl()
            val dataConverter = DataConverterGson(gson, identifierFinder)
            return DocumentManagerImpl(resultSetConverter, identifierFinder, dataConverter)
        }

    }

}