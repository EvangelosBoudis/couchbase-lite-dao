/*
 * Copyright (c) 2021 Evangelos Boudis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    override suspend fun <T> dataToMap(data: T, documentType: String): Map<String, Any>? {
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