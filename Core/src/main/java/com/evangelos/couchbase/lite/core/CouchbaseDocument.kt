package com.evangelos.couchbase.lite.core

import java.lang.annotation.Inherited

/**
 * Marks a class as a CouchbaseLite document.
 * Each document must have 1 field annotated with [Id].
 */
@Inherited
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class CouchbaseDocument(
    /**
     * The type of the document in the CouchbaseLite database. If not set, defaults to the class name.
     */
    val type: String = ""
)