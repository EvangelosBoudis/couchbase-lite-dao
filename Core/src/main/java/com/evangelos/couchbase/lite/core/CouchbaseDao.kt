package com.evangelos.couchbase.lite.core

import kotlinx.coroutines.flow.Flow

/**
 * Interface for generic CRUD operations in CouchbaseLite database.
 * @see [CouchbaseDaoImpl] for implementation.
 */
interface CouchbaseDao<T> {

    /**
     * Returns an Observable that monitors changes about Documents.
     * @return Documents Flow.
     * @see [CouchbaseDaoImpl.observeAll]
     * */
    fun observeAll(): Flow<List<T>>

    /**
     * Returns the number of documents available.
     * @return number of documents.
     * @see [CouchbaseDaoImpl.count]
     */
    suspend fun count(): Int

    /**
     * Returns a single instance of the type [T] or null if none was found.
     * @return optional document.
     * @see [CouchbaseDaoImpl.findOne]
     * */
    suspend fun findOne(): T?

    /**
     * Returns all instances of the type [T].
     * @return all documents.
     * @see [CouchbaseDaoImpl.findAll]
     */
    suspend fun findAll(): List<T>

    /**
     * Returns all instances of the type [T].
     * @param pageable object for Paging retrieve.
     * @return all documents.
     * @see [CouchbaseDaoImpl.findAll(Pageable)]
     */
    suspend fun findAll(pageable: Pageable): List<T>

    /**
     * Returns whether a document with the given id exists.
     * @param id unique key
     * @return true if a document with the given id exists, false otherwise.
     * @see [CouchbaseDaoImpl.existsById]
     */
    suspend fun existsById(id: String): Boolean

    /**
     * Retrieves a document by its id.
     * @param id unique key.
     * @return the document with the given id or null if none found.
     * @see [CouchbaseDaoImpl.findById]
     */
    suspend fun findById(id: String): T?

    /**
     *
     * Returns all instances of the type [T] with the given IDs.
     * If some or all ids are not found, no documents are returned for these IDs.
     * @param ids unique keys.
     * @return documents. The size can be equal or less than the number of given ids.
     * @see [CouchbaseDaoImpl.findAllById]
     */
    suspend fun findAllById(ids: List<String>): List<T>

    /**
     * Returns all ids of instances of the type [T].
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
     * Deletes all instances of the type [T] with the given IDs.
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
     * Deletes all documents managed by the dao.
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
     * Replaces all instances of the type [T] with the given.
     * @param data document to be saved.
     * @see [CouchbaseDaoImpl.replace]
     * */
    suspend fun replace(data: T)

    /**
     * Replaces all instances of the type [T] with the given.
     * @param data documents to be saved.
     * @see [CouchbaseDaoImpl.replaceAll]
     * */
    suspend fun replaceAll(data: List<T>, bulk: Boolean = true)

}