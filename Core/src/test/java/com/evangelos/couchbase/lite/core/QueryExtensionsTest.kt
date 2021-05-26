package com.evangelos.couchbase.lite.core

import com.couchbase.lite.*
import com.evangelos.couchbase.lite.core.extensions.observeChange
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito
import java.util.concurrent.Executor

class QueryExtensionsTest {

    @Test
    fun `when query succeeds then result are emitted by the flow`() = runBlocking {
        val resultSet = Mockito.mock(ResultSet::class.java)
        val query = createMockQuery { listener ->
            val queryChange = createMockQueryChange(resultSet) // mock successful query change
            listener.changed(queryChange) // manual call
            object : ListenerToken { }
        }
        assertEquals(
            resultSet,
            query.observeChange().firstOrNull()?.results
        )
    }

    @Test
    fun `when query fails then the flow fails`() = runBlocking {
        val query = createMockQuery { listener ->
            val queryChange = createMockQueryChange(error = CouchbaseLiteException("Forced exception")) // mock unsuccessful query change
            listener.changed(queryChange)
            object : ListenerToken { }
        }
        query
            .observeChange()
            .catch { error ->
                assertTrue(error is CouchbaseLiteException)
            }
            .collect()
    }

    @Test
    fun `when the flow is cancelled then the query is stopped`() = runBlocking {
        val token = Mockito.mock(ListenerToken::class.java)
        val resultSet = Mockito.mock(ResultSet::class.java)
        val query = createMockQuery { listener ->
            val queryChange = createMockQueryChange(resultSet) // mock successful query change
            listener.changed(queryChange)
            token
        }
        query.observeChange().take(1).collect() // dispose after first emission
        Mockito.verify(query).removeChangeListener(token) // verify removeChangeListener fun called once
    }

    private fun createMockQuery(
        completion: (QueryChangeListener) -> ListenerToken
    ): Query {
        val query = Mockito.mock(Query::class.java)
        Mockito.`when`(
            query.addChangeListener(
                Mockito.any(Executor::class.java),
                Mockito.any(QueryChangeListener::class.java)
            )
        ).thenAnswer { invocation ->
            val funParameter = invocation.arguments[1] as QueryChangeListener
            completion(funParameter)
        }
        return query
    }

    private fun createMockQueryChange(
        resultSet: ResultSet = Mockito.mock(ResultSet::class.java),
        error: Throwable? = null
    ): QueryChange {
        val queryChange = Mockito.mock(QueryChange::class.java)
        Mockito.`when`(queryChange.results).thenReturn(resultSet)
        Mockito.`when`(queryChange.error).thenReturn(error)
        return queryChange
    }

}

