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

import com.couchbase.lite.*
import com.couchbase.lite.Document
import com.evangelos.couchbase.lite.core.extensions.observeData
import com.evangelos.couchbase.lite.core.extensions.toData
import com.evangelos.couchbase.lite.core.converters.DocumentConverter
import com.evangelos.couchbase.lite.core.converters.DocumentConverterGson
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Implementation of [CouchbaseDao].
 *
 * @param database The Couchbase Database instance in which the operations will be performed.
 * @param docConverter The converter that will be used for Documents manipulation.
 * @param clazz The type of the domain object for which this instance is to be used.
 *
 */
open class CouchbaseDaoImpl<T>(
    protected val database: Database,
    protected val docConverter: DocumentConverter,
    protected val clazz: Class<T>
): CouchbaseDao<T> {

    constructor(
        database: Database,
        gson: Gson = GsonBuilder()
            .setDateFormat(COUCHBASE_LITE_DATE_FORMAT)
            .create(),
        clazz: Class<T>
    ) : this(database, DocumentConverterGson(gson), clazz)

   /**
    * Gets the Document Type by accessing the [com.evangelos.couchbase.lite.core.Document] annotation of [T] class.
    * If not set, defaults to the class name.
    * */
    protected val documentType: String by lazy {
       val annotationType =
           if (clazz.isAnnotationPresent(com.evangelos.couchbase.lite.core.Document::class.java)) clazz.getAnnotation(com.evangelos.couchbase.lite.core.Document::class.java)!!.type
           else ""
       if (annotationType.isNotBlank()) annotationType else clazz.simpleName
    }

    /**
     * Adds a change listener that monitors changes that occur on Documents where the type-property they have,
     * coincides with the [documentType] and then converts it into [Flow].
     * @return Flow of documents.
     * @throws [CouchbaseLiteException].
     * */
    override fun observeAll(): Flow<List<T>> {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        return query.observeData(converter = docConverter, clazz = clazz)
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Read
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of documents where the type-property they have, coincides with the [documentType].
     * @return number of documents.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun count(): Int = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        query.execute().allResults().size
    }

    /**
     * Retrieves a single document where the type-property it has, coincides with the [documentType].
     * @return document or null if none was found.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findOne(): T? = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .limit(Expression.intValue(1))

        query.toData(docConverter, clazz).firstOrNull()
    }

    /**
     * Retrieves all documents where the type-property they have, coincides with the [documentType].
     * @return all documents.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findAll(): List<T> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        query.toData(docConverter, clazz)
    }

    /**
     * Retrieves all documents where the type-property they have, coincides with the [documentType], following the paging criteria.
     * @param pageable object that used for Paging implementation.
     * @return documents.
     * @throws [CouchbaseLiteException].
     *
     * Example:
     * val pageable = Pageable(0, 10, mapOf(
     *     "name" to false, // descending
     *     "description" to true // ascending
     * ))
     * findAll(pageable)
     * */
    override suspend fun findAll(
        pageable: Pageable
    ): List<T> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .orderBy(*pageable.ordering)
            .limit(Expression.intValue(pageable.pageSize), Expression.intValue(pageable.offset))

        query.toData(docConverter, clazz)
    }

    /**
     * Returns whether a document with the given id exists. The type-property of the document must coincides with the [documentType].
     * @param id document unique key.
     * @return true if a document with the given id exists, false otherwise.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun existsById(id: String): Boolean = withContext(Dispatchers.IO) {

        val query: Query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(
                Meta.id.equalTo(Expression.string(id))
                    .and(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            )

        query.execute().allResults().size > 0
    }

    /**
     * Retrieves a document by its id. The type-property of the document must coincides with the [documentType].
     * @param id document unique key.
     * @return the document with the given id or null if none found.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findById(id: String): T? = withContext(Dispatchers.IO) {

        val query: Query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(
                Meta.id.equalTo(Expression.string(id))
                    .and(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            )

        query.toData(docConverter, clazz).firstOrNull()
    }

    /**
     * Retrieves all documents identified by the given ids. The type-property of the documents must coincides with the [documentType].
     * If some or all ids are not found, no documents are returned for these IDs.
     * Note that the order of elements in the result is not guaranteed.
     * @param ids documents unique keys.
     * @return documents. The size can be equal or less than the number of given ids.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findAllById(ids: List<String>): List<T> = withContext(Dispatchers.IO) {

        val values = ids.map {
            Expression.string(it)
        }.toTypedArray()

        val query: Query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(
                Meta.id.`in`(*values)
                    .and(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            )

        query.toData(docConverter, clazz)
    }

    /**
     * Retrieves the ids of all documents where the type-property they have, coincides with the [documentType].
     * @return documents unique keys.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findAllId(): List<String> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        query.execute().allResults().mapNotNull { it.getString(ID) }
    }

    /**
     * Retrieves a CouchbaseLite Document by its id. If no document is found, an exception is thrown.
     * @param id document unique key.
     * @return CouchbaseLite Document.
     * @throws [CouchbaseLiteException].
     */
    protected fun findDocumentById(id: String): Document {
        return database.getDocument(id) ?: throw CouchbaseLiteException("Document with id: $id does not exists.")
    }

    /**
     * Retrieves all CouchbaseLite Documents identified by the given ids.
     * If some id not found, an exception is thrown.
     * @param ids documents unique keys.
     * @return CouchbaseLite Documents.
     * @throws [CouchbaseLiteException].
     */
    protected fun findAllDocumentsById(ids: List<String>): List<Document> {
        return ids.map {
            findDocumentById(it)
        }
        // return ids.mapNotNull { database.getDocument(it) }
    }

    /**
     *
     * Finds the ids of all documents where the type-property they have, coincides with the [documentType]
     * and then for each id that was found retrieves CouchbaseLite Documents from Database.
     * @return all CouchbaseLite Documents.
     * @throws [CouchbaseLiteException].
     */
    protected suspend fun findAllDocuments(): List<Document> {
        return findAllDocumentsById(findAllId())
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Create
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Saves the given document. If a document in the database has the same identifier as the one provided, it is updated.
     * @param data document to be saved.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun save(data: T) = withContext(Dispatchers.IO) {
        saveAll(arrayListOf(data), bulk = false)
    }

    /**
     * Saves the given documents. In case the documents in the database have the same identifier as some of the provided ones, they are updated.
     * @param bulk operation type. If it is true, save is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun saveAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val mutableDocs = data.mapNotNull {
            docConverter.dataToMutableDocument(it, documentType, clazz)
        }
        if (bulk) {
            database.inBatch {
                saveMutableDocs(mutableDocs)
            }
        } else {
            saveMutableDocs(mutableDocs)
        }
    }

    /**
     * Saves the given CouchbaseLite MutableDocuments to [Database].
     * @param mutableDocs CouchbaseLite MutableDocuments.
     * @throws [CouchbaseLiteException].
     */
    protected fun saveMutableDocs(mutableDocs: List<MutableDocument>) {
        mutableDocs.forEach { mutableDoc ->
            database.save(mutableDoc)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Delete
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Deletes the document identified by the given id.
     * @param id the unique key of the document to be deleted.
     * @throws [CouchbaseLiteException] if id does not exist in database.
     */
    override suspend fun deleteById(id: String) = withContext(Dispatchers.IO) {
        deleteAllById(arrayListOf(id), bulk = false)
    }

    /**
     * Deletes the given document.
     * @param data document to be deleted.
     * @throws [CouchbaseLiteException] if document does not exist in database.
     */
    override suspend fun delete(data: T) = withContext(Dispatchers.IO) {
        deleteAll(arrayListOf(data), bulk = false)
    }

    /**
     * Deletes the documents identified by the given ids.
     * @param ids the unique keys of the documents to be deleted.
     * @param bulk operation type. If it is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException] if some ids does not exist in database.
     */
    override suspend fun deleteAllById(ids: List<String>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val documents = findAllDocumentsById(ids)
        if (bulk) {
            database.inBatch {
                deleteDocuments(documents)
            }
        } else {
            deleteDocuments(documents)
        }
    }

    /**
     * Deletes the given documents.
     * @param data documents to be deleted.
     * @param bulk operation type. If it is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException] if some document does not exist in database.
     */
    override suspend fun deleteAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        deleteAllById(docConverter.findAllId(data, clazz), bulk)
    }

    /**
     * Deletes all existing documents where the type-property they have, coincides with the [documentType].
     * @param bulk operation type. If it is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun deleteAll(bulk: Boolean) = withContext(Dispatchers.IO) {
        deleteAllById(findAllId(), bulk)
    }

    /**
     * Deletes the given CouchbaseLite Documents from [Database].
     * @param documents CouchbaseLite Documents.
     * @throws [CouchbaseLiteException].
     */
    protected fun deleteDocuments(documents: List<Document>) {
        documents.forEach { document ->
            database.delete(document)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Update
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Updates the given document.
     * @param data document to be updated.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun update(data: T) = withContext(Dispatchers.IO) {
        updateAll(arrayListOf(data), bulk = false)
    }

    /**
     * Updates the given documents.
     * @param data documents to be updated.
     * @param bulk operation type. If it is true the update is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun updateAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val ids = docConverter.findAllId(data, clazz)
        val documents = findAllDocumentsById(ids)
        val mutableDocs = documents.mapIndexedNotNull { index, document ->
            val map = docConverter.dataToMap(data[index], documentType)
            if (map != null) document.toMutable().setData(map) else null
        }
        if (bulk) {
            database.inBatch {
                saveMutableDocs(mutableDocs)
            }
        } else {
            saveMutableDocs(mutableDocs)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Replace
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Deletes all existing documents where the type-property they have, coincides with the [documentType], and then saves the given document.
     * @param data document to be saved.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun replace(data: T) = withContext(Dispatchers.IO) {
        replaceAll(arrayListOf(data), bulk = false)
    }

    /**
     * Deletes all existing documents where the type-property they have, coincides with the [documentType], and then saves the given documents.
     * @param data documents to be saved.
     * @param bulk operation type. If it is true the deletion and save is done by using [Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun replaceAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val documents = findAllDocuments()
        val mutableDocs = data.mapNotNull {
            docConverter.dataToMutableDocument(it, documentType, clazz)
        }
        if (bulk) {
            database.inBatch {
                deleteDocuments(documents)
                saveMutableDocs(mutableDocs)
            }
        } else {
            deleteDocuments(documents)
            saveMutableDocs(mutableDocs)
        }
    }

}