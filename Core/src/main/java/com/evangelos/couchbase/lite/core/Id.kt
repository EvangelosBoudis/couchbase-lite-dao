package com.evangelos.couchbase.lite.core

/**
 * Marks a field in an [CouchbaseDocument] as the unique key.
 * This key needs to be any [String] with a length of maximum 250 characters (UUID, email, etc.)
 */
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Id