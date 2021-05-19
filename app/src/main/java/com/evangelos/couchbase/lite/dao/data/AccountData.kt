package com.evangelos.couchbase.lite.dao.data

import com.evangelos.couchbase.lite.core.CouchbaseDocument
import com.evangelos.couchbase.lite.core.Id

@CouchbaseDocument("AccountDocument")
data class AccountData(
    @Id val id: String,
    val name: String?,
    val email: String?,
    val username: String?,
    val password: String?
)