package com.evangelos.couchbase.lite.dao.ui.accounts

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.evangelos.couchbase.lite.dao.R
import com.evangelos.couchbase.lite.dao.data.AccountDto
import com.evangelos.couchbase.lite.dao.databinding.FragmentAccountsBinding
import com.evangelos.couchbase.lite.dao.presentation.AccountsViewModel
import com.evangelos.couchbase.lite.dao.presentation.ViewModelFactory
import com.evangelos.couchbase.lite.dao.util.AdapterClickListener
import com.evangelos.couchbase.lite.dao.util.viewModels

class AccountsFragment : Fragment(
    R.layout.fragment_accounts
), AdapterClickListener<AccountDto>, View.OnClickListener {

    private val viewModel: AccountsViewModel by viewModels(::ViewModelFactory)

    private val adapter = AccountsAdapter()
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        val binding = FragmentAccountsBinding.bind(view)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(view.context)
            recyclerView.adapter = adapter
            addFab.setOnClickListener(this@AccountsFragment)
            deleteBtn.setOnClickListener(this@AccountsFragment)
            restoreBtn.setOnClickListener(this@AccountsFragment)
        }
        adapter.adapterClickListener = this
        viewModel.accountDto.observe(viewLifecycleOwner) { accounts ->
            adapter.dataSet = accounts
        }
    }

    override fun onClick(view: View, model: AccountDto, position: Int) {
        if (view.id == R.id.delete_btn) {
            viewModel.deleteAccount(model.id).observe(viewLifecycleOwner) { result ->
                result.fold(onSuccess = {
                    Toast.makeText(requireActivity(), "Deleted!", Toast.LENGTH_SHORT).show()
                }, onFailure = {
                    Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                })
            }
        } else {
            val action = AccountsFragmentDirections.actionItemsToItemDetails(model.id)
            navController.navigate(action)
        }
    }

    override fun onClick(v: View?) {
        val view = v ?: return
        when (view.id) {
            R.id.add_fab -> {
                navController.navigate(R.id.action_items_to_itemConstructor)
            }
            R.id.restore_btn -> {
                viewModel.restoreAccounts().observe(viewLifecycleOwner) { result ->
                    result.fold(onSuccess = {
                        Toast.makeText(requireActivity(), "Restored!", Toast.LENGTH_SHORT).show()
                    }, onFailure = {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    })
                }
            }
            R.id.delete_btn -> {
                viewModel.deleteAccounts().observe(viewLifecycleOwner) { result ->
                    result.fold(onSuccess = {
                        Toast.makeText(requireActivity(), "Deleted!", Toast.LENGTH_SHORT).show()
                    }, onFailure = {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }

}