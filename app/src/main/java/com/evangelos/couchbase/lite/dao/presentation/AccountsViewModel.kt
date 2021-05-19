package com.evangelos.couchbase.lite.dao.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.couchbase.lite.*
import com.evangelos.couchbase.lite.core.CouchbaseDao
import com.evangelos.couchbase.lite.core.TYPE
import com.evangelos.couchbase.lite.core.converters.ResultSetConverter
import com.evangelos.couchbase.lite.core.extensions.observeData
import com.evangelos.couchbase.lite.dao.data.AccountData
import com.evangelos.couchbase.lite.dao.data.AccountDto
import kotlinx.coroutines.launch
import java.util.*

class AccountsViewModel(
    private val database: Database,
    private val resultSetConverter: ResultSetConverter,
    private val accountDao: CouchbaseDao<AccountData>
) : ViewModel() {

    val accounts = accountDao.observeAll().asLiveData()

    // Projection example
    val accountDto: LiveData<List<AccountDto>> by lazy {
        val query = QueryBuilder
            .select(
                SelectResult.property("id"),
                SelectResult.property("name"),
                SelectResult.property("email")
            )
            .from(DataSource.database(database))
            .where(Expression.property(TYPE).equalTo(Expression.string("AccountDocument")))

        query.observeData(converter = resultSetConverter, clazz = AccountDto::class.java).asLiveData()
    }

    init {
        viewModelScope.launch {
            if (accountDao.count() == 0) accountDao.save(DUMMY_DATA)
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            accountDao.deleteById(id)
        }
    }

    fun restoreAccounts() {
        viewModelScope.launch {
            accountDao.replace(DUMMY_DATA)
        }
    }

    fun deleteAccounts() {
        viewModelScope.launch {
            accountDao.deleteAll()
        }
    }

    companion object {
        private val DUMMY_DATA = listOf(
            AccountData(UUID.randomUUID().toString(), "Gmail", "john.doe@gmail.com", "John Doe", "sdf12312"),
            AccountData(UUID.randomUUID().toString(), "Hotmail", "mercedez.borjas@hotmail.com", "Mercedez Borjas", "x23x232c"),
            AccountData(UUID.randomUUID().toString(), "MSN", "ok.behan@msn.com", "Ok Behan", "d21x1c"),
            AccountData(UUID.randomUUID().toString(), "Outlook", "carmelo.philpott@outlook.com", "Carmelo Philpott", "ec123"),
            AccountData(UUID.randomUUID().toString(), "ProtonMail", "korey.leisinger@protonmail.com", "Korey Leisinger", "j76dfbh"),
            AccountData(UUID.randomUUID().toString(), "AOL", "elane.barner@aol.com", "Elane Barner", "gh45yg"),
            AccountData(UUID.randomUUID().toString(), "ZOHO mail", "lurline.capella@zohomail.com", "Lurline Capella", "xczc"),
            AccountData(UUID.randomUUID().toString(), "iCloud Mail", "hoyt.naugle@icloud.com", "Hoyt Naugle", "h78l"),
            AccountData(UUID.randomUUID().toString(), "Yahoo! Mail", "clarice.buchholtz@yahoo.com", "Clarice Buchholtz", "vrcv3"),
            AccountData(UUID.randomUUID().toString(), "GMX", "sarita.veselka@gmx.com", "Sarita Veselka", "hdfgh"),
            AccountData(UUID.randomUUID().toString(), "Hey", "janeth.mott@hey.com", "Janeth Mott", "v34cr34v4"),
        )
    }

}