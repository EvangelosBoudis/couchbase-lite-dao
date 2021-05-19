package com.evangelos.couchbase.lite.dao.ui.accounts

import android.view.View
import com.evangelos.couchbase.lite.dao.R
import com.evangelos.couchbase.lite.dao.data.AccountData
import com.evangelos.couchbase.lite.dao.util.RecyclerAdapter

class AccountsAdapter : RecyclerAdapter<AccountData, AccountsViewHolder>() {

    override fun getResId(viewType: Int) = R.layout.account_cell

    override fun getViewHolder(view: View, viewType: Int) = AccountsViewHolder(view)

}