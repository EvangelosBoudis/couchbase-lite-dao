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

package com.evangelos.couchbase.lite.core.idFinder

import com.evangelos.couchbase.lite.core.Id

/**
 * Interface used to resolve the [com.evangelos.couchbase.lite.core.Id] of a [com.evangelos.couchbase.lite.core.Document].
 */
interface IdentifierFinder {

    /**
     * Returns the value of the field that [Id] is presented, in the given document.
     * @param data Couchbase document.
     * @param clazz class of document.
     * @return document unique key.
     * @throws [IllegalArgumentException] if none was found.
     */
    fun <T> findId(data: T, clazz: Class<T>): String {
        return try {
            clazz.declaredFields.filter { field ->
                field.isAccessible = true
                field.isAnnotationPresent(Id::class.java)
            }.map { field ->
                field[data] as String
            }.firstOrNull()
        } catch (e: Exception) {
            null
        } ?: throw IllegalArgumentException("@Id annotation was not found on $clazz")
    }

    /**
     * Returns the values of the fields that [Id] is presented, for each given document.
     * @param data Couchbase documents.
     * @param clazz class of documents.
     * @return document unique keys.
     * @throws [IllegalArgumentException] if some was not found.
     */
    fun <T> findAllId(data: List<T>, clazz: Class<T>): List<String> {
        return data.map {
            findId(it, clazz)
        }
    }

}