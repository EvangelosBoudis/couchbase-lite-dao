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
    suspend fun <T> dataToMap(data: T, documentType: String): Map<String, Any>?

    /**
     * Converts the given instance of type [T] to [MutableDocument].
     * @param data instance of type [T] to convert.
     * @param documentType the type of Couchbase Document to apply into mutable document.
     * @param clazz the class we want to convert the given instances.
     * @return mutable document or null if [Exception] occurred.
     */
    suspend fun <T> dataToMutableDocument(data: T, documentType: String, clazz: Class<T>): MutableDocument? {
        return dataToMap(data, documentType)?.let { map ->
            val id = findId(data, clazz)
            MutableDocument(id, map)
        }
    }

}