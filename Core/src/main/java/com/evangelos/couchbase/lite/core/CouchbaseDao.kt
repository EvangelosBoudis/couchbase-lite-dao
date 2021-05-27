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

package com.evangelos.couchbase.lite.core

import kotlinx.coroutines.flow.Flow

/**
 * Interface for generic CRUD operations in CouchbaseLite database.
 * @see [CouchbaseDaoImpl] for implementation.
 */
interface CouchbaseDao<T> {

    /**
     * Returns an Observable that monitors changes about [T] type documents.
     * @return Documents Flow.
     * @see [CouchbaseDaoImpl.observeAll]
     * */
    fun observeAll(): Flow<List<T>>

    /**
     * Returns the number of [T] type documents available.
     * @return number of documents.
     * @see [CouchbaseDaoImpl.count]
     */
    suspend fun count(): Int

    /**
     * Returns a single [T] type document or null if none was found.
     * @return optional document.
     * @see [CouchbaseDaoImpl.findOne]
     * */
    suspend fun findOne(): T?

    /**
     * Returns all [T] type documents.
     * @return all documents.
     * @see [CouchbaseDaoImpl.findAll]
     */
    suspend fun findAll(): List<T>

    /**
     * Returns all [T] type documents.
     * @param pageable object for Paging retrieve.
     * @return all documents.
     * @see [CouchbaseDaoImpl.findAll(Pageable)]
     *
     * Example:
     * val pageable = Pageable(0, 10, mapOf(
     *     "name" to false, // descending
     *     "description" to true // ascending
     * ))
     * findAll(pageable)
     */
    suspend fun findAll(pageable: Pageable): List<T>

    /**
     * Returns whether a [T] type document with the given id exists.
     * @param id unique key
     * @return true if a document with the given id exists, false otherwise.
     * @see [CouchbaseDaoImpl.existsById]
     */
    suspend fun existsById(id: String): Boolean

    /**
     * Retrieves a [T] type document by its id.
     * @param id unique key.
     * @return the document with the given id or null if none found.
     * @see [CouchbaseDaoImpl.findById]
     */
    suspend fun findById(id: String): T?

    /**
     * Returns all [T] type documents with the given ids.
     * If some or all ids are not found, no documents are returned for these ids.
     * Note that the order of elements in the result is not guaranteed.
     * @param ids unique keys.
     * @return documents. The size can be equal or less than the number of given ids.
     * @see [CouchbaseDaoImpl.findAllById]
     */
    suspend fun findAllById(ids: List<String>): List<T>

    /**
     * Returns all [T] type documents ids.
     * @return unique keys.
     * @see [CouchbaseDaoImpl.findAllId]
     */
    suspend fun findAllId(): List<String>

    /**
     * Saves or Updates the given document.
     * @param data document to be saved.
     * @see [CouchbaseDaoImpl.save]
     */
    suspend fun save(data: T)

    /**
     * Saves or Updates all given documents.
     * @param data documents to be saved.
     * @param bulk operation type. If bulk is true the save gonna be Transactional.
     * @see [CouchbaseDaoImpl.saveAll]
     */
    suspend fun saveAll(data: List<T>, bulk: Boolean = true)

    /**
     * Deletes the document with the given id.
     * @param id the unique key of the document to be deleted.
     * @see [CouchbaseDaoImpl.deleteById]
     */
    suspend fun deleteById(id: String)

    /**
     * Deletes a given document.
     * @param data document to be deleted.
     * @see [CouchbaseDaoImpl.delete]
     */
    suspend fun delete(data: T)

    /**
     * Deletes all [T] type documents with the given ids.
     * @param ids the unique keys of the documents to be deleted.
     * @param bulk operation type. If bulk is true the deletion gonna be Transactional.
     * @see [CouchbaseDaoImpl.deleteAllById]
     */
    suspend fun deleteAllById(ids: List<String>, bulk: Boolean = true)

    /**
     * Deletes the given documents.
     * @param data documents to be deleted.
     * @param bulk operation type. If bulk is true the deletion gonna be Transactional.
     * @see [CouchbaseDaoImpl.deleteAll(List<T>, Boolean)]
     */
    suspend fun deleteAll(data: List<T>, bulk: Boolean = true)

    /**
     * Deletes all [T] type documents.
     * @param bulk operation type. If bulk is true the deletion gonna be Transactional.
     * @see [CouchbaseDaoImpl.deleteAll(Boolean)]
     */
    suspend fun deleteAll(bulk: Boolean = true)

    /**
     * Updates a given document.
     * @param data document to be updated.
     * @see [CouchbaseDaoImpl.update]
     * */
    suspend fun update(data: T)

    /**
     * Updates the given documents.
     * @param data documents to be updated.
     * @param bulk operation type. If bulk is true the update gonna be Transactional.
     * @see [CouchbaseDaoImpl.updateAll]
     * */
    suspend fun updateAll(data: List<T>, bulk: Boolean = true)

    /**
     * Replaces all [T] type documents with the given.
     * @param data document to be saved.
     * @see [CouchbaseDaoImpl.replace]
     * */
    suspend fun replace(data: T)

    /**
     * Replaces all [T] type documents with the given.
     * @param data documents to be saved.
     * @see [CouchbaseDaoImpl.replaceAll]
     * */
    suspend fun replaceAll(data: List<T>, bulk: Boolean = true)

}