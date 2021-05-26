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

import com.couchbase.lite.ResultSet

/**
 * Interface used to convert [ResultSet].
 * @see [ResultSetConverterGson] for implementation.
 */
interface ResultSetConverter {

    /**
     * Converts the given map to a specific single instance of the type [T].
     * @param map key-value collection to deserialize.
     * @param clazz the class we want to deserialize the given collection.
     * @return instance of the type [T] or null if [Exception] occurred.
     * @see [ResultSetConverterGson.mapToData]
     */
    suspend fun <T> mapToData(map: Map<String, Any>, clazz: Class<T>): T?

    /**
     * Converts the given [ResultSet.allResults] to a list of type [T].
     * If some or all [com.couchbase.lite.Result] can not been deserialize, no instance of the type [T] are returned for these results.
     * @param resultSet CouchbaseLite results to deserialize.
     * @param clazz the class we want to deserialize the given results.
     * @return list of type [T]. The size can be equal or less than the number of given results.
     * @see [ResultSetConverterGson.resultSetToData]
     */
    suspend fun <T> resultSetToData(resultSet: ResultSet, clazz: Class<T>): List<T> {
        return resultSet.allResults().mapNotNull { result ->
            /*
            * We have two options in order to query data from CouchbaseLite:
            *
            *   1. Using `SelectResult.all()` expression:
            *   In this case the result contains dictionary entries in which every entry identified by a key with the database name as value.
            *
            *   2. Using Projection:
            *   In this case the result contains a single dictionary that contains every Document key we define in query.
            *
            * So with the implementation bellow, we cover both cases.
            * */
            val map = if (result.count() > 0) result.getDictionary(0)?.toMap() ?: result.toMap() else null
            map?.let { mapToData(it, clazz) }
        }
    }

}