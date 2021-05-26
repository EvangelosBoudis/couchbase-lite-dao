package com.evangelos.couchbase.lite.core.util

import com.evangelos.couchbase.lite.core.CouchbaseDocument
import com.evangelos.couchbase.lite.core.Id
import com.google.gson.annotations.SerializedName
import java.util.*

@CouchbaseDocument("user_doc")
data class UserData(
    @Id val email: String,
    val age: Int,
    val balance: Float,
    val enabled: Boolean,
    @SerializedName("join_date") val joinDate: Date
)