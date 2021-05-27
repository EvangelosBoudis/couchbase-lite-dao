package com.evangelos.couchbase.lite.dao.data

import com.evangelos.couchbase.lite.core.Document
import com.evangelos.couchbase.lite.core.Id

@Document("Account")
data class AccountData(
    @Id val id: String,
    val name: String?,
    val email: String?,
    val username: String?,
    val password: String?
)

data class AccountDto(
    val id: String,
    val name: String?,
    val email: String?
)