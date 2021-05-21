package com.evangelos.couchbase.lite.core

import com.couchbase.lite.Expression
import com.couchbase.lite.Ordering

/**
 * A class that holds information for paging.
 * @param pageNo The number of the page. Starts from 0
 * @param pageSize The size of the page. Starts from 1
 * @param order A Map for ordering.
 *      Key: represents the field name
 *      Value: represents the order. True -> Ascending, False -> Descending
 */
data class Pageable(
    val pageNo: Int,
    val pageSize: Int,
    val order: Map<String, Boolean>
) {

    /**
     * Offset used by [com.couchbase.lite.OrderBy.limit]
     * */
    val offset: Int
        get() {
            return pageSize + pageNo
        }

    /**
     * Ordering used by [com.couchbase.lite.Where.orderBy]
     * */
    val ordering: Array<Ordering>
        get() {
            return order.map { entry ->
                val expression = Ordering.expression(Expression.property(entry.key))
                if (entry.value) expression.ascending()
                else expression.descending()
            }.toTypedArray()
        }

}