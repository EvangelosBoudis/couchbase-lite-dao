package com.evangelos.couchbase.lite.core

import kotlinx.coroutines.flow.Flow

interface CouchbaseDao<T> {

    fun observeAll(): Flow<List<T>>

    suspend fun count(): Int

    suspend fun findOne(): T?

    suspend fun findAll(): List<T>

    suspend fun findAll(limit: Int, skip: Int, orderBy: List<Pair<String, Boolean>>): List<T>

    suspend fun findById(id: String): T?

    suspend fun findByIds(ids: List<String>): List<T>

    suspend fun findAllIds(): List<String>

    suspend fun save(data: T): Boolean

    suspend fun save(data: List<T>, bulk: Boolean = true): Boolean

    suspend fun deleteById(id: String): Boolean

    suspend fun delete(data: T): Boolean

    suspend fun deleteByIds(ids: List<String>, bulk: Boolean = true): Boolean

    suspend fun delete(data: List<T>, bulk: Boolean = true): Boolean

    suspend fun deleteAll(bulk: Boolean = true): Boolean

    suspend fun update(data: T): Boolean

    suspend fun update(data: List<T>, bulk: Boolean = true): Boolean

    suspend fun replace(data: T): Boolean

    suspend fun replace(data: List<T>, bulk: Boolean = true): Boolean

}