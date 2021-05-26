package com.evangelos.couchbase.lite.core

import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.evangelos.couchbase.lite.core.util.TestUtil
import com.evangelos.couchbase.lite.core.util.UserData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CouchbaseDaoTest {

    @JvmField @Rule val tempDir = TemporaryFolder()

    private val database: Database by lazy {
        val name = "test-database.db"
        val config = DatabaseConfiguration().setDirectory(tempDir.root.absolutePath)
        Database(name, config)
    }

    private val converter: Gson by lazy {
        GsonBuilder()
            .setDateFormat("dd/MM/yyyy")
            .create()
    }

    private val testUtil: TestUtil by lazy {
        TestUtil(converter)
    }

    private val dao: CouchbaseDao<UserData> by lazy {
        CouchbaseDaoImpl(database, converter, UserData::class.java)
    }

    @Before
    fun setUp() {
        CouchbaseLite.init()
    }

    @Test
    fun `save-delete all and observe all`() = runBlocking {
        val users = testUtil.users.subList(10, 20)
        dao.saveAll(users)
        assertEquals(dao.observeAll().firstOrNull(), users)
        dao.deleteAll()
        assertEquals(dao.observeAll().firstOrNull(), emptyList<UserData>())
    }

    @Test
    fun `save and count`() = runBlocking {
        assertEquals(dao.count(), 0)
        dao.save(testUtil.users.first())
        assertEquals(dao.count(), 1)
        val users = testUtil.users.subList(30, 100)
        dao.saveAll(users)
        assertEquals(dao.count(), 71)
    }

    @Test
    fun `save and find one`() = runBlocking {
        assertNull(dao.findOne())
        val user = testUtil.users[10]
        dao.save(user)
        assertEquals(dao.findOne(), user)
    }

    @Test
    fun `save and find all`() = runBlocking {
        assertTrue(dao.findAll().isEmpty())
        val users = testUtil.users.subList(0, 500)
        dao.saveAll(users, true)
        assertEquals(dao.findAll(), users)
    }

    @Test
    fun `save and find with paging`() = runBlocking {
        val users = testUtil.users.subList(0, 200)
        val sortedUsers = users.sortedBy { it.email }
        dao.saveAll(users)
        assertEquals(
            sortedUsers.subList(0, 20),
            dao.findAll(Pageable(0, 20, mapOf("email" to true)))
        )
        assertEquals(
            sortedUsers.subList(20, 40),
            dao.findAll(Pageable(1, 20, mapOf("email" to true)))
        )
        assertEquals(
            sortedUsers.subList(40, 60),
            dao.findAll(Pageable(2, 20, mapOf("email" to true)))
        )
    }

    @Test
    fun `save and find if exists by id`() = runBlocking {
        val user = testUtil.users[80]
        assertFalse(dao.existsById(user.email))
        dao.save(user)
        assertTrue(dao.existsById(user.email))
    }

    @Test
    fun `save and find by id`() = runBlocking {
        val user = testUtil.users[100]
        assertNull(dao.findById(user.email))
        dao.save(user)
        assertEquals(dao.findById(user.email), user)
    }

    @Test
    fun `save all and find all by id`() = runBlocking {
        val users = testUtil.users.subList(0, 300)
        val ids = users.map { it.email }
        assertTrue(dao.findAllById(ids).isEmpty())
        dao.saveAll(users, true)
        val dbUsers = dao.findAllById(ids)
        users.forEach { user ->
            assertTrue(dbUsers.contains(user))
        }
    }

    @Test
    fun `save all and find all ids`() = runBlocking {
        assertTrue(dao.findAllId().isEmpty())
        val users = testUtil.users.subList(200, 400)
        dao.saveAll(users, false)
        assertEquals(dao.findAllId(), users.map { it.email })
        val moreUsers = testUtil.users.subList(0, 100)
        dao.saveAll(moreUsers, true)
        val allIds = users.plus(moreUsers).map { it.email }
        assertEquals(dao.findAllId(), allIds)
    }

    @Test
    fun `save and delete by id`() = runBlocking {
        val user = testUtil.users[5]
        dao.save(user)
        assertEquals(dao.findById(user.email), user)
        dao.deleteById(user.email)
        assertNull(dao.findById(user.email))
    }

    @Test
    fun `save and delete`() = runBlocking {
        val user = testUtil.users[120]
        dao.save(user)
        assertEquals(dao.findById(user.email), user)
        dao.delete(user)
        assertNull(dao.findById(user.email))
    }

    @Test
    fun `save all and delete all by id`() = runBlocking {
        val users = testUtil.users.subList(20, 400)
        dao.saveAll(users, true)
        assertEquals(dao.findAll(), users)
        val usersSubset = users.filterIndexed { index, _ ->
            index %2 == 0
        }
        dao.deleteAllById(usersSubset.map { it.email }, false)
        assertEquals(dao.count(), users.size - usersSubset.size)
    }

    @Test
    fun `save all and delete all given`() = runBlocking {
        val users = testUtil.users.subList(10, 100)
        dao.saveAll(users, false)
        dao.deleteAll(users.subList(0, 40), false)
        assertEquals(dao.findAll(), users.subList(40, 90))
    }

    @Test
    fun `save all and delete all`() = runBlocking {
        val users = testUtil.users.subList(30, 400)
        dao.saveAll(users, true)
        assertEquals(dao.findAll(), users)
        dao.deleteAll()
        assertTrue(dao.findAll().isEmpty())
    }

    @Test
    fun `save and update`() = runBlocking {
        val user = testUtil.users.first()
        dao.save(user)
        assertEquals(dao.findOne(), user)
        val updatedUser = user.copy(age = 100, balance = 500.5f)
        dao.update(updatedUser)
        assertEquals(dao.findOne(), updatedUser)
    }

    @Test
    fun `save all and update all given`() = runBlocking {
        val users = testUtil.users.subList(0, 100)
        dao.saveAll(users, true)
        assertEquals(dao.findAll(), users)
        val updatedUsers = users.mapIndexed { index, user ->
            user.copy(age = user.age + index)
        }
        dao.updateAll(updatedUsers, true)
        assertEquals(dao.findAll(), updatedUsers)
    }

    @Test
    fun `save all and replace`() = runBlocking {
        val users = testUtil.users.subList(30, 50)
        val user = testUtil.users.first()
        dao.saveAll(users)
        assertEquals(dao.findAll(), users)
        dao.replace(user)
        assertEquals(dao.findAll(), listOf(user))
    }

    @Test
    fun `save all and replace all`() = runBlocking {
        val users = testUtil.users.subList(0, 400)
        val preferredUsers = testUtil.users.subList(200, 500)
        dao.saveAll(users)
        assertEquals(dao.findAll(), users)
        dao.replaceAll(preferredUsers)
        assertEquals(dao.findAll(), preferredUsers)
    }

}