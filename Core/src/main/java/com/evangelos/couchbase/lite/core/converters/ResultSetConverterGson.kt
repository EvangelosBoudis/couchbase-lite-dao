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

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Implementation of [ResultSetConverter] using the Gson Library.
 */
open class ResultSetConverterGson(private val gson: Gson): ResultSetConverter {

    /**
     * Converts the given map to a specific single instance of the type [T], using [Gson] converter.
     * @param map key-value collection to deserialize.
     * @param clazz the class we want to deserialize the given collection.
     * @return instance of the type [T] or null if [JsonSyntaxException] occurred.
     */
    override suspend fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T? {
        return try {
            gson.fromJson(gson.toJson(map), clazz)
        } catch (e: JsonSyntaxException) {
            null
        }
    }

}