package com.evangelos.couchbase.lite.dao

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.couchbase.lite.CouchbaseLite

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CouchbaseLite.init(this)
        setContentView(R.layout.activity_main)
    }

}