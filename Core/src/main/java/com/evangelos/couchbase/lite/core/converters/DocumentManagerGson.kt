package com.evangelos.couchbase.lite.core.converters

import com.couchbase.lite.MutableDocument
import com.evangelos.couchbase.lite.core.Id
import com.evangelos.couchbase.lite.core.TYPE
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class DocumentManagerGson(private val gson: Gson): ResultSetConverterGson(gson), DocumentManager {

    override fun <T> dataToMutableDocument(
        data: T,
        documentType: String,
        clazz: Class<T>
    ): MutableDocument? {
        val map = dataToMap(data, documentType)
        val id = findId(data, clazz)
        return if (map != null) MutableDocument(id, map) else null
    }

    override fun <T> dataToMap(data: T, documentType: String): Map<String, Any>? {
        return try {
            val json = gson.toJson(data)
            val map = gson.fromJson<MutableMap<String, Any>>(json, object : TypeToken<HashMap<String, Any>>() {}.type)
            map[TYPE] = documentType
            map
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

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
        return data.map {
            findId(it, clazz)
        }
    }

}