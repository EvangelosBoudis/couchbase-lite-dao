package com.evangelos.couchbase.lite.dao.ui.accountDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.evangelos.couchbase.lite.dao.R
import com.evangelos.couchbase.lite.dao.databinding.FragmentAccountDetailsBinding
import com.evangelos.couchbase.lite.dao.presentation.AccountDetailsViewModel
import com.evangelos.couchbase.lite.dao.presentation.ViewModelFactory
import com.evangelos.couchbase.lite.dao.util.viewModels

class AccountDetailsFragment : Fragment(R.layout.fragment_account_details) {

    private val viewModel: AccountDetailsViewModel by viewModels(::ViewModelFactory)

    private val args: AccountDetailsFragmentArgs by navArgs()
    private val itemId get() = args.itemId

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAccountDetailsBinding.bind(view)
        binding.apply {
            viewModel.findAccountById(itemId).observe(viewLifecycleOwner) { account ->
                nameField.setText(account?.name)
                emailField.setText(account?.email)
                usernameField.setText(account?.username)
                passwordField.setText(account?.password)
            }
            saveFab.setOnClickListener {
                viewModel.updateAccount(
                    itemId,
                    nameField.text?.toString(),
                    emailField.text?.toString(),
                    usernameField.text?.toString(),
                    passwordField.text?.toString()
                ).observe(viewLifecycleOwner) { result ->
                    result.fold(onSuccess = {
                        requireActivity().onBackPressed()
                    }, onFailure = {
                        Toast.makeText(requireActivity(), it.message, Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }

}