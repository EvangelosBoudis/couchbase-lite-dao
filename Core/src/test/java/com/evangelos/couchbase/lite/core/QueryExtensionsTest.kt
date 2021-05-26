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
import org.mockito.Mockito.*
import java.util.concurrent.Executor

class QueryExtensionsTest {

    @Test
    fun `when query succeed, results emitted by the flow`() = runBlocking {
        val resultSet = mock(ResultSet::class.java)
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
    fun `when query fails, flow fails`() = runBlocking {
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
    fun `when flow cancelled, query listener stops`() = runBlocking {
        val token = mock(ListenerToken::class.java)
        val resultSet = mock(ResultSet::class.java)
        val query = createMockQuery { listener ->
            val queryChange = createMockQueryChange(resultSet) // mock successful query change
            listener.changed(queryChange)
            token
        }
        query.observeChange().take(1).collect() // dispose after first emission
        verify(query).removeChangeListener(token) // verify removeChangeListener fun called once
    }

    private fun createMockQuery(
        completion: (QueryChangeListener) -> ListenerToken
    ): Query {
        val query = mock(Query::class.java)
        `when`(
            query.addChangeListener(
                any(Executor::class.java),
                any(QueryChangeListener::class.java)
            )
        ).thenAnswer { invocation ->
            val funParameter = invocation.arguments[1] as QueryChangeListener
            completion(funParameter)
        }
        return query
    }

    private fun createMockQueryChange(
        resultSet: ResultSet = mock(ResultSet::class.java),
        error: Throwable? = null
    ): QueryChange {
        val queryChange = mock(QueryChange::class.java)
        `when`(queryChange.results).thenReturn(resultSet)
        `when`(queryChange.error).thenReturn(error)
        return queryChange
    }

}

