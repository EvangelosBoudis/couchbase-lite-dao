package com.evangelos.couchbase.lite.core.manager

import com.evangelos.couchbase.lite.core.converters.DataConverter
import com.evangelos.couchbase.lite.core.idFinder.IdentifierFinder
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter

interface DocumentManager: ResultSetConverter, IdentifierFinder, DataConverter