package com.evangelos.couchbase.lite.core

import java.lang.annotation.Inherited

@Inherited
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class CouchbaseDocument(val type: String = "")