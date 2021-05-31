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

import com.couchbase.lite.internal.utils.DateUtils
import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

/**
 * class used by [Gson] in order to serialize and deserialize [Date].
 * Couchbase Lite stores dates as strings in ISO-8601 format
 */
class DateConverterGson : JsonSerializer<Date>, JsonDeserializer<Date> {

    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(DateUtils.toJson(src))
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date {
        return DateUtils.fromJson(json?.asString)
    }

}