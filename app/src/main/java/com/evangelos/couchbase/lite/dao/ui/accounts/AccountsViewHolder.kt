package com.evangelos.couchbase.lite.dao.ui.accounts

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.evangelos.couchbase.lite.dao.R
import com.evangelos.couchbase.lite.dao.data.AccountDto
import com.evangelos.couchbase.lite.dao.util.RecyclerViewHolder

class AccountsViewHolder(itemView: View) : RecyclerViewHolder<AccountDto>(itemView) {

    private val nameField = itemView.findViewById<TextView>(R.id.name_field)
    private val emailField = itemView.findViewById<TextView>(R.id.email_field)
    private val deleteBtn = itemView.findViewById<Button>(R.id.delete_btn)

    init {
        deleteBtn.setOnClickListener(this)
    }

    override fun bind(model: AccountDto) {
        nameField.text = model.name
        emailField.text = model.email
    }

}