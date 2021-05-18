package com.evangelos.couchbase.lite.core

import com.couchbase.lite.*
import com.evangelos.couchbase.lite.core.converters.DocumentManager
import com.evangelos.couchbase.lite.core.converters.DocumentManagerGson
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

open class CouchbaseDaoImpl<T>(
    private val database: Database,
    private val docManager: DocumentManager,
    private val clazz: Class<T>
): CouchbaseDao<T> {

    constructor(
        database: Database,
        gson: Gson,
        clazz: Class<T>
    ) : this(database, DocumentManagerGson(gson), clazz)

    // TODO: Second Constructor with kotlinx.serialization bean

    private val documentType: String by lazy {
        var type = clazz.simpleName
        if (clazz.isAnnotationPresent(CouchbaseDocument::class.java)) {
            val ann = clazz.getAnnotation(CouchbaseDocument::class.java)
            ann?.type?.let {
                if (it.isNotEmpty()) type = it
            }
        }
        type
    }

    @ExperimentalCoroutinesApi
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

    override suspend fun findOne(): T? = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .limit(Expression.intValue(1))

        return@withContext executeAndConvert(query).firstOrNull()
    }

    override suspend fun findAll(): List<T> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        return@withContext executeAndConvert(query)
    }

    // TODO: Replace with Order like Spring
    override suspend fun findAll(
        limit: Int,
        skip: Int,
        asc: Boolean,
        vararg orderBy: String
    ): List<T> = withContext(Dispatchers.IO) {

        val ordering = orderBy.map {
            val expression = Ordering.expression(Expression.property(it))
            if (asc) {
                expression.ascending()
            } else {
                expression.descending()
            }
        }.toTypedArray()

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))
            .orderBy(*ordering)
            .limit(Expression.intValue(limit), Expression.intValue(skip))

        return@withContext executeAndConvert(query)
    }

    override suspend fun findById(id: String): T? = withContext(Dispatchers.IO) {
        return@withContext findByIds(arrayListOf(id)).firstOrNull()
    }

    override suspend fun findByIds(ids: List<String>): List<T> = withContext(Dispatchers.IO) {

        val values = ids.map {
            Expression.string(it)
        }.toTypedArray()

        val query: Query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Meta.id.`in`(*values))

        return@withContext executeAndConvert(query)
    }

    override suspend fun findAllIds(): List<String> = withContext(Dispatchers.IO) {

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string(documentType)))

        val results: MutableList<Result> = ArrayList()
        try {
            results.addAll(query.execute().allResults())
        } catch (e: CouchbaseLiteException) {
            e.printStackTrace()
        }
        val ids: MutableList<String> = ArrayList()
        for (result in results) {
            val id = result.getString(ID) ?: continue
            ids.add(id)
        }
        return@withContext ids
    }

    private fun findDocumentsByIds(ids: List<String>): List<Document> {
        val documents: MutableList<Document> = ArrayList()
        for (id in ids) {
            val document = database.getDocument(id) ?: continue
            documents.add(document)
        }
        return documents
    }

    private suspend fun findAllDocuments(): List<Document> {
        return findDocumentsByIds(findAllIds())
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Create
    //////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun save(data: T): Boolean = withContext(Dispatchers.IO) {
        return@withContext save(arrayListOf(data), bulk = false)
    }

    override suspend fun save(data: List<T>, bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        val mutableDocs: MutableList<MutableDocument> = ArrayList()
        for (entry in data) {
            val mutableDoc = docManager.dataToMutableDocument(entry, documentType, clazz) ?: continue
            mutableDocs.add(mutableDoc)
        }
        var success = false
        if (bulk) {
            try {
                database.inBatch {
                    success = saveMutableDocs(mutableDocs)
                }
            } catch (e: CouchbaseLiteException) {
                e.printStackTrace()
            }
        } else {
            success = saveMutableDocs(mutableDocs)
        }
        return@withContext success
    }

    private fun saveMutableDocs(mutableDocs: List<MutableDocument>): Boolean {
        for (mutableDoc in mutableDocs) {
            try {
                database.save(mutableDoc)
            } catch (e: CouchbaseLiteException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Delete
    //////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun deleteById(id: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext deleteByIds(arrayListOf(id), bulk = false)
    }

    override suspend fun delete(data: T): Boolean = withContext(Dispatchers.IO) {
        return@withContext delete(arrayListOf(data), bulk = false)
    }

    override suspend fun deleteByIds(ids: List<String>, bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        val documents = findDocumentsByIds(ids)
        var success = false
        if (bulk) {
            try {
                database.inBatch {
                    success = deleteDocuments(documents)
                }
            } catch (e: CouchbaseLiteException) {
                e.printStackTrace()
            }
        } else {
            success = deleteDocuments(documents)
        }
        return@withContext success
    }

    override suspend fun delete(data: List<T>, bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext deleteByIds(docManager.findIds(data, clazz), bulk)
    }

    override suspend fun deleteAll(bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext deleteByIds(findAllIds(), bulk)
    }

    private fun deleteDocuments(documents: List<Document>): Boolean {
        for (document in documents) {
            try {
                database.delete(document)
            } catch (e: CouchbaseLiteException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Update
    //////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun update(data: T): Boolean = withContext(Dispatchers.IO) {
        return@withContext update(arrayListOf(data), bulk = false)
    }

    override suspend fun update(data: List<T>, bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val ids = docManager.findIds(data, clazz)
        val documents = findDocumentsByIds(ids)
        val mutableDocs: MutableList<MutableDocument> = ArrayList()
        if (data.size == documents.size) {
            for (i in documents.indices) {
                val map = docManager.dataToMap(data[i], documentType) ?: continue
                mutableDocs.add(documents[i].toMutable().setData(map))
            }
            if (bulk) {
                try {
                    database.inBatch {
                        success = saveMutableDocs(mutableDocs)
                    }
                } catch (e: CouchbaseLiteException) {
                    e.printStackTrace()
                }
            } else {
                success = saveMutableDocs(mutableDocs)
            }
        }
        return@withContext success
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Replace
    //////////////////////////////////////////////////////////////////////////////////////////

    override suspend fun replace(data: T): Boolean = withContext(Dispatchers.IO) {
        return@withContext replace(arrayListOf(data), bulk = false)
    }

    override suspend fun replace(data: List<T>, bulk: Boolean): Boolean = withContext(Dispatchers.IO) {
        val documents = findAllDocuments()
        val mutableDocs: MutableList<MutableDocument> = ArrayList()
        for (entry in data) {
            val mutableDoc = docManager.dataToMutableDocument(entry, documentType, clazz) ?: continue
            mutableDocs.add(mutableDoc)
        }
        var success = false
        if (bulk) {
            try {
                database.inBatch {
                    success = deleteDocuments(documents) && saveMutableDocs(mutableDocs)
                }
            } catch (e: CouchbaseLiteException) {
                e.printStackTrace()
            }
        } else {
            success = deleteDocuments(documents) && saveMutableDocs(mutableDocs)
        }
        return@withContext success
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    /// Tools
    //////////////////////////////////////////////////////////////////////////////////////////

    private suspend fun executeAndConvert(query: Query): List<T> = withContext(Dispatchers.IO) {
        val couchbaseDocs: MutableList<T> = ArrayList()
        try {
            couchbaseDocs.addAll(query.toData(docManager, clazz))
        } catch (e: CouchbaseLiteException) {
            e.printStackTrace()
        }
        return@withContext couchbaseDocs
    }

}