package com.evangelos.couchbase.lite.core

import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering

data class Pageable(
    val pageNo: Int,
    val pageSize: Int,
    val order: Map<String, Boolean>
) {

    val offset: Int
        get() {
            return pageSize + pageNo
        }

    val ordering: Array<Ordering>
        get() {
            return order.map { entry ->
                val expression = Ordering.expression(Expression.property(entry.key))
                if (entry.value) expression.ascending()
                else expression.descending()
            }.toTypedArray()
        }

}