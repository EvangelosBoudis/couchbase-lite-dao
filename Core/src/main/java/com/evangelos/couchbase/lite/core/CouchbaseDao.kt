package com.evangelos.couchbase.lite.core

import kotlinx.coroutines.flow.Flow

// Replace with "same type documents @see more details about implementation..."

/**
 * Interface for generic CRUD operations in CouchbaseLite database.
 * @see [CouchbaseDaoImpl] for implementation.
 */
interface CouchbaseDao<T> {

    /**
     * Returns an Observable that monitors changes about documents matching with the [CouchbaseDocument.type].
     * @return all documents as Flow.
     * */
    fun observeAll(): Flow<List<T>>

    /**
     * Returns the number of documents matching with the [CouchbaseDocument.type].
     * @return number of documents.
     */
    suspend fun count(): Int

    /**
     * Returns a single document matching with the [CouchbaseDocument.type] or null if none was found.
     * @return optional document.
     */
    suspend fun findOne(): T?

    /**
     * Returns all documents matching with the [CouchbaseDocument.type].
     * @return all documents.
     */
    suspend fun findAll(): List<T>

    // TODO: Documentation
    suspend fun findAll(limit: Int, skip: Int, orderBy: List<Pair<String, Boolean>>): List<T>

    /**
     * Returns whether a document with the given id exists.
     * @param id the id of the document to search for.
     * @return true if a document with the given id exists, false otherwise.
     */
    suspend fun existsById(id: String): Boolean

    /**
     * Retrieves a document identified by the given id or null if none found.
     * @param id the document's id.
     * @return optional document.
     */
    suspend fun findById(id: String): T?

    /**
     * Retrieves all documents identified by the given ids.
     * If some or all ids are not found, no documents are returned for these IDs.
     * @param ids the documents ids.
     * @return documents. The size can be equal or less than the number of given ids.
     */
    suspend fun findAllById(ids: List<String>): List<T>

    /**
     * Retrieves the ids of the documents that they are matching with the [CouchbaseDocument.type].
     * @return documents ids.
     */
    suspend fun findAllId(): List<String>

    /**
     * Saves the given document.
     * @param data document to be saved.
     */
    suspend fun save(data: T)

    /**
     * Saves all documents.
     * @param bulk operation type. If bulk is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     */
    suspend fun saveAll(data: List<T>, bulk: Boolean = true)

    /**
     * Deletes the document identified by the given id.
     * @param id the id of the document to be deleted.
     */
    suspend fun deleteById(id: String)

    /**
     * Deletes the given document.
     * @param data document to be deleted.
     */
    suspend fun delete(data: T)

    /**
     * Deletes the documents identified by the given ids.
     * @param ids the ids of the documents to be deleted.
     * @param bulk operation type. If bulk is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     */
    suspend fun deleteAllById(ids: List<String>, bulk: Boolean = true)

    /**
     * Deletes the given documents.
     * @param data documents to be deleted.
     * @param bulk operation type. If bulk is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     */
    suspend fun deleteAll(data: List<T>, bulk: Boolean = true)

    /**
     * Deletes all documents.
     * @param bulk operation type. If bulk is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     */
    suspend fun deleteAll(bulk: Boolean = true)

    suspend fun update(data: T)

    suspend fun updateAll(data: List<T>, bulk: Boolean = true)

    /**
     * Replaces all documents that matching with the [CouchbaseDocument.type], with the given.
     * */
    suspend fun replace(data: T)

    suspend fun replaceAll(data: List<T>, bulk: Boolean = true)

}