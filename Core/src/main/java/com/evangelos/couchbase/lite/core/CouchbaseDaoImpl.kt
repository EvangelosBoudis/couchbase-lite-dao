package com.evangelos.couchbase.lite.core

import com.couchbase.lite.*
import com.evangelos.couchbase.lite.core.converters.DocumentManager
import com.evangelos.couchbase.lite.core.converters.DocumentManagerGson
import com.evangelos.couchbase.lite.core.extensions.observeData
import com.evangelos.couchbase.lite.core.extensions.toData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// https://kotlinlang.org/docs/exceptions.html#checked-exceptions
// Documentation about Transactional with different DAO types

/**
 * Implementation of [CouchbaseDao].
 *
 * @param [T] The type of the domain object for which this instance is to be used.
 *
 */
open class CouchbaseDaoImpl<T>(
    private val database: Database,
    private val docManager: DocumentManager,
    private val clazz: Class<T>
): CouchbaseDao<T> {

    constructor(
        database: Database,
        gson: Gson = Gson(),
        clazz: Class<T>
    ) : this(database, DocumentManagerGson(gson), clazz)

    private val documentType: String by lazy {
        var type = clazz.simpleName
        if (clazz.isAnnotationPresent(CouchbaseDocument::class.java)) {
            clazz.getAnnotation(CouchbaseDocument::class.java)?.type?.let {
                if (it.isNotBlank()) type = it
            }
        }
        type
    }

    override fun observeAll(): Flow<List<T>> {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        return query.observeData(converter = docManager, clazz = clazz)
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Read
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of documents where the type they have, coincides with the [CouchbaseDocument.type].
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
     * Retrieves a single document where the type it has, coincides with the [CouchbaseDocument.type].
     * @return document or null if none was found.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findOne(): T? = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .limit(Expression.intValue(1))

        query.toData(docManager, clazz).firstOrNull()
    }

    /**
     * Retrieves all documents where the type they have, coincides with the [CouchbaseDocument.type].
     * @return all documents.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findAll(): List<T> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        query.toData(docManager, clazz)
    }

    override suspend fun findAll(
        limit: Int,
        skip: Int,
        orderBy: List<Pair<String, Boolean>>
    ): List<T> = withContext(Dispatchers.IO) {

        val ordering = orderBy.map { pair ->
            val expression = Ordering.expression(Expression.property(pair.first))
            if (pair.second) expression.ascending()
            else expression.descending()
        }.toTypedArray()

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .orderBy(*ordering)
            .limit(Expression.intValue(limit), Expression.intValue(skip))

        query.toData(docManager, clazz)
    }

    /**
     * Returns whether a document with the given id exists.
     * @param id document unique key.
     * @return true if a document with the given id exists, false otherwise.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun existsById(id: String): Boolean = withContext(Dispatchers.IO) {

        val query: Query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Meta.id.equalTo(Expression.string(id)))

        query.execute().allResults().size > 0
    }

    /**
     * Retrieves a document by its id.
     * @param id document unique key.
     * @return the document with the given id or null if none found.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun findById(id: String): T? = withContext(Dispatchers.IO) {

        val query: Query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Meta.id.equalTo(Expression.string(id)))

        query.toData(docManager, clazz).firstOrNull()
    }

    /**
     * Retrieves all documents identified by the given ids.
     * If some or all ids are not found, no documents are returned for these IDs.
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
            .where(Meta.id.`in`(*values))

        query.toData(docManager, clazz)
    }

    /**
     * Retrieves the ids of all documents where the type they have, coincides with the [CouchbaseDocument.type].
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

    private fun findDocumentById(id: String): Document {
        return database.getDocument(id) ?: throw CouchbaseLiteException("Document with id: $id does not exists.")
    }

    private fun findDocumentsByIds(ids: List<String>): List<Document> {
        return ids.map {
            findDocumentById(it)
        }
        /*return ids.mapNotNull {
            database.getDocument(it)
        }*/
    }

    private suspend fun findAllDocuments(): List<Document> {
        return findDocumentsByIds(findAllId())
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Create
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Saves the given document. If the identifier of the document already exists in database then it is replaced with the provided.
     * @param data document to be saved.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun save(data: T) = withContext(Dispatchers.IO) {
        saveAll(arrayListOf(data), bulk = false)
    }

    /**
     * Saves the given documents. If some identifiers already exists in database then the documents replaced with the provided.
     * @param bulk operation type. If it is true, save is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun saveAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val mutableDocs = data.mapNotNull {
            docManager.dataToMutableDocument(it, documentType, clazz)
        }
        if (bulk) {
            database.inBatch {
                saveMutableDocs(mutableDocs)
            }
        } else {
            saveMutableDocs(mutableDocs)
        }
    }

    private fun saveMutableDocs(mutableDocs: List<MutableDocument>) {
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
     * @throws [CouchbaseLiteException].
     */
    override suspend fun deleteById(id: String) = withContext(Dispatchers.IO) {
        deleteAllById(arrayListOf(id), bulk = false)
    }

    /**
     * Deletes the given document.
     * @param data document to be deleted.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun delete(data: T) = withContext(Dispatchers.IO) {
        deleteAll(arrayListOf(data), bulk = false)
    }

    /**
     * Deletes the documents identified by the given ids.
     * @param ids the unique keys of the documents to be deleted.
     * @param bulk operation type. If it is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun deleteAllById(ids: List<String>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val documents = findDocumentsByIds(ids)
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
     * @throws [CouchbaseLiteException].
     */
    override suspend fun deleteAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        deleteAllById(docManager.findIds(data, clazz), bulk)
    }

    /**
     * Deletes all existing documents where the type they have, coincides with the [CouchbaseDocument.type]
     * @param bulk operation type. If it is true the deletion is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun deleteAll(bulk: Boolean) = withContext(Dispatchers.IO) {
        deleteAllById(findAllId(), bulk)
    }

    private fun deleteDocuments(documents: List<Document>) {
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
        val ids = docManager.findIds(data, clazz)
        val documents = findDocumentsByIds(ids)
        val mutableDocs = documents.mapIndexedNotNull { index, document ->
            val map = docManager.dataToMap(data[index], documentType)
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
     * Deletes all existing documents where the type they have, coincides with the [CouchbaseDocument.type], and then saves the given document.
     * @param data document to be saved.
     * @throws [CouchbaseLiteException].
     */
    override suspend fun replace(data: T) = withContext(Dispatchers.IO) {
        replaceAll(arrayListOf(data), bulk = false)
    }

    /**
     * Deletes all existing documents where the type they have, coincides with the [CouchbaseDocument.type], and then saves the given documents.
     * @param data documents to be saved.
     * @param bulk operation type. If it is true the deletion and save is done by using [com.couchbase.lite.Database.inBatch].
     * @throws [CouchbaseLiteException].
     */
    override suspend fun replaceAll(data: List<T>, bulk: Boolean) = withContext(Dispatchers.IO) {
        val documents = findAllDocuments()
        val mutableDocs = data.mapNotNull {
            docManager.dataToMutableDocument(it, documentType, clazz)
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

/*    private fun printToConsole(funName: String, msg: String) {
        Log.i("CouchbaseDaoImpl", "$funName: $msg")
    }*/

}

/*
* TODO:
*  1. License + Documentation inside README (like MOLO)
*  2. Mockito Testing
*  3. Test/Mock with directly throws inside withContext() and in batch
*  4. test update not existing document
*  5. Icon of test pass
* */