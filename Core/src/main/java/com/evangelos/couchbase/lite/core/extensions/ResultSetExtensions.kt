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

package com.evangelos.couchbase.lite.core.extensions

import com.couchbase.lite.ResultSet
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter

/**
 * Converts [ResultSet] to a [List] of [T] instances using the [ResultSetConverter.resultSetToData].
 * @param converter converter to transform [ResultSet] to a [List] of [T].
 * @param clazz the class we want to convert the given results.
 * @return list of type [T]. The size can be equal or less than the number of given results.
 * */
suspend fun <T> ResultSet.toData(
    converter: ResultSetConverter,
    clazz: Class<T>
): List<T> = converter.resultSetToData(this, clazz)