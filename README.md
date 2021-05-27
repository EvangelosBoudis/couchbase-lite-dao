# Couchbase Lite DAO

Kotlin Data Access Object for Couchbase Lite JVM and Android SDks.

![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
[![Coverage Status](https://img.shields.io/codecov/c/github/mockito/mockito.svg)](https://codecov.io/github/mockito/mockito)
[![MIT License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/Vaggelis95/couchbase-lite-dao/blob/master/LICENSE)

<div align="center">
  <sub>Built with ❤︎ by
  <a href="https://github.com/Vaggelis95">Evangelos Boudis</a>
</div>

## Installing

Add JitPack as repository for your project:

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

And then to your module `build.gradle` file:

```groovy
implementation "com.github.evangelos:couchbase-lite-dao:1.0.0"
```
  
## Features

Here are the main features that Couchbase Lite DAO provides for
boosting the integration of the Couchbase Lite SDK with Kotlin.

- [Document annotations](#document-annotations)
- [Non-blocking generic dao](#non-blocking-generic-dao)
- [Flow live queries](#flow-live-queries)
- [ResultSet serialization](#resultset-serialization)  
  
## Built With

- [Couchbase Lite Java Framework](https://docs.couchbase.com/couchbase-lite/2.7/java-platform.html) - Official Couchbase Lite Java SDK.
- [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - Concurrency design pattern that can be used to simplify asynchronous code execution.
- [GSON](https://github.com/google/gson) - Java serialization/deserialization library.
- [Mockito](https://github.com/mockito/mockito) - Mocking framework for unit tests written in Java.
  
  
### Document annotations

All documents should be annotated with the `@Document` annotation, but it is not a requirement.
<br/>
The `@Id` annotation needs to be present because every document in Couchbase Lite needs a unique key. Feel free to use whatever key fits your use case, be it a UUID, an email address or anything else.
<br/><br/>
The following code is an example of a simple document that defines a User with properties for id, name, favorite number, height, if he/she has a pet and date of birth:

```kotlin
@Document("user_doc")
data class UserData(
    @Id val id: String = UUID.randomUUID().toString(),
    val name: String,
    @SerializedName("favorite_number") val favoriteNumber: Int,
    val height: Float,
    @SerializedName("has_pet") val hasPet: Boolean,
    @SerializedName("date_of_birth") val dob: Date
)
```

As we mention we use GSON library as converter, so we are free to use any annotation that provides, for example `@SerializedName`. 
  
### Non-blocking generic dao

A `CouchbaseDao` requires a `Database` instance from Couchbase Lite SDK, a `Gson` instance from GSON Library and finally the class that represents the document inside the database.

```kotlin
val database = Database("my-database.db", DatabaseConfiguration())

val gson = GsonBuilder()
            .setDateFormat("dd/MM/yyyy")
            .create()
  
val userDao: CouchbaseDao<UserData> = CouchbaseDaoImpl(database, gson, UserData::class.java)
```

Methods supported by `CouchbaseDao`:

|Method|Return Type|Description|
|---|---|---|
|observeAll()|Flow<T>|Returns an Observable that monitors changes about T type documents|
|count()|Int|Returns the number of T type documents available|
|findOne()|T?|Returns a single T type document or null if none was found.|
|findAll()|List<T>|Returns all T type documents|
|findAll(pageable: Pageable)|List<T>|Returns all T type documents, using paging|
|existsById(id: String)|Boolean|Returns whether a T type document with the given id exists|
|findById(id: String)|T?|Retrieves a T type document by its id|
|findAllById(ids: List<String>)|List<T>|Returns all T type documents with the given ids|
|findAllId()|List<String>|Returns all T type documents ids|
|save(data: T)|Unit|Saves or Updates the given document|
|saveAll(data: List<T>, bulk: Boolean)|Unit|Saves or Updates all given documents|
|deleteById(id: String)|Unit|Deletes the document with the given id|
|delete(data: T)|Unit|Deletes a given document|
|deleteAllById(ids: List<String>, bulk: Boolean)|Unit|Deletes all T type documents with the given ids|
|deleteAll(data: List<T>, bulk: Boolean)|Unit|Deletes the given documents|
|deleteAll(bulk: Boolean)|Unit|Deletes all T type documents|
|update(data: T)|Unit|Updates a given document|
|updateAll(data: List<T>, bulk: Boolean)|Unit|Updates the given documents|
|replace(data: T)|Unit|Replaces all T type documents with the given|
|replaceAll(data: List<T>, bulk: Boolean)|Unit|Replaces all T type documents with the given|
  
If `CouchbaseDao` does not provide some of the methods you desire, you can define your own custom methods by creating a subclass.

```kotlin
data class UserDto(
    val id: String,
    val name: String
)

interface UserDao: CouchbaseDao<UserData> {

    suspend fun findAllWhereNameContains(searchKey: String): List<UserDto> // Projection

}

class UserDaoImpl(
    database: Database,
    gson: Gson
): CouchbaseDaoImpl<UserData>(database, gson, UserData::class.java), UserDao {

    override suspend fun findAllWhereNameContains(searchKey: String): List<UserDto> {
        val whereExpression =
            Expression
                .property(TYPE).equalTo(Expression.string(documentType)) // REQUIRED in order to fetch ONLY User Documents ("@type" == "user_doc")
                .and(Expression.property("name").like(Expression.string("%$searchKey%")))
  
        return QueryBuilder
            .select(SelectResult.property("id"), SelectResult.property("name")) // UserDto requires only id & name
            .from(DataSource.database(database))
            .where(whereExpression)
            .toData(docConverter, AccountDto::class.java)
    }

}

val userDao: UserDao = UserDaoImpl(database, gson)
```

### Flow live queries

The `Query.observeChange` extention replaces the `QueryChangeListener` callback with a Flow wich is 
responsible to emit changes, that occur in the query results and to unregister the callback when tears down.

```kotlin
QueryBuilder
    .select(SelectResult.expression(Meta.id))
    .from(DataSource.database(database))
    .observeChange()
    .collect { value: ResultSet -> 
        
    }
```

  
### ResultSet serialization

```kotlin
val projectionQuery = QueryBuilder
    .select(SelectResult.property("id"), SelectResult.property("name"))
    .from(DataSource.database(database))
    .where(Expression.property(TYPE).equalTo(Expression.string("user_doc")))

// Executes the query and then converts the corresponding ResultSet into a list of type AccountDto.
val users: List<UserDto> = projectionQuery.toData(clazz = UserDto::class.java) 

val liveUsers: Flow<List<UserDto>> = projectionQuery.observeData(clazz = UserDto::class.java)  
```
  
